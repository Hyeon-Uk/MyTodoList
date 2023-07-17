package com.hyeonuk.todo.todo.service;

import com.hyeonuk.todo.integ.exception.NotFoundException;
import com.hyeonuk.todo.integ.exception.ValidationException;
import com.hyeonuk.todo.integ.util.StringUtils;
import com.hyeonuk.todo.member.entity.Member;
import com.hyeonuk.todo.member.exception.UserInfoNotFoundException;
import com.hyeonuk.todo.member.repository.MemberRepository;
import com.hyeonuk.todo.todo.dto.TodoDeleteDTO;
import com.hyeonuk.todo.todo.dto.TodoSaveDTO;
import com.hyeonuk.todo.todo.dto.TodoToggleDTO;
import com.hyeonuk.todo.todo.dto.TodoUpdateDTO;
import com.hyeonuk.todo.todo.entity.Category;
import com.hyeonuk.todo.todo.entity.Todo;
import com.hyeonuk.todo.todo.exception.TodoException;
import com.hyeonuk.todo.todo.repository.CategoryRepository;
import com.hyeonuk.todo.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.el.util.Validation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {
    private final TodoRepository todoRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TodoSaveDTO.Response save(TodoSaveDTO.Request dto) throws TodoException, ValidationException, UserInfoNotFoundException, NotFoundException {
        try {
            String userId = dto.getUserId();
            Long categoryId = dto.getCategoryId();
            String content = dto.getContent();

            if (userId == null || StringUtils.isBlank(userId) || categoryId == null || content == null || StringUtils.isBlank(content)) {
                throw new ValidationException("입력값을 확인해주세요");
            }

            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new UserInfoNotFoundException("사용자 정보가 일치하지 않습니다."));

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("카테고리가 존재하지 않습니다."));

            Todo todo = Todo.builder()
                    .member(member)
                    .category(category)
                    .content(content)
                    .complete(false)
                    .build();
            todoRepository.save(todo);
            return TodoSaveDTO.Response.builder()
                    .todoId(todo.getId())
                    .content(todo.getContent())
                    .build();
        } catch (ValidationException | UserInfoNotFoundException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new TodoException("todo 저장 오류");
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TodoUpdateDTO.Response update(TodoUpdateDTO.Request dto) throws ValidationException, UserInfoNotFoundException, TodoException, NotFoundException {
        try {
            String userId = dto.getUserId();
            Long todoId = dto.getTodoId();
            String updated = dto.getContent();

            if (userId == null || StringUtils.isBlank(userId) || todoId == null) {
                throw new ValidationException("입력값을 확인해주세요");
            }

            if (updated == null || StringUtils.isBlank(updated)) {
                throw new ValidationException("변경할 Todo의 내용을 입력해주세요");
            }

            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new UserInfoNotFoundException("사용자 정보가 일치하지 않습니다."));

            Todo todo = todoRepository.findById(todoId)
                    .orElseThrow(() -> new NotFoundException("todo가 존재하지 않습니다."));

            if (!todo.getMember().getId().equals(member.getId())) {
                throw new ValidationException("타인의 Todo는 업데이트가 불가능합니다.");
            }

            todo.updateContent(updated);

            return TodoUpdateDTO.Response.builder()
                    .todoId(todo.getId())
                    .content(todo.getContent())
                    .build();
        } catch (ValidationException | UserInfoNotFoundException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new TodoException("todo 업데이트 오류");
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TodoToggleDTO.Response toggle(TodoToggleDTO.Request dto) throws TodoException, ValidationException, UserInfoNotFoundException, NotFoundException {
        try {
            Long todoId = dto.getTodoId();
            String userId = dto.getUserId();

            if (userId == null || StringUtils.isBlank(userId) || todoId == null) {
                throw new ValidationException("입력값을 확인해주세요");
            }

            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new UserInfoNotFoundException("사용자 정보가 일치하지 않습니다."));

            Todo todo = todoRepository.findById(todoId)
                    .orElseThrow(() -> new NotFoundException("해당 todo를 찾을 수 없습니다."));

            if (!todo.getMember().getId().equals(member.getId())) {
                throw new ValidationException("타인의 Todo는 업데이트가 불가능합니다.");
            }

            boolean result = todo.toggleComplete();
            return TodoToggleDTO.Response.builder()
                    .result(result)
                    .build();
        }catch(ValidationException | NotFoundException | UserInfoNotFoundException e){
            throw e;
        } catch (Exception e) {
            throw new TodoException("toggle 오류");
        }
    }

    @Override
    @Transactional
    public TodoDeleteDTO.Response delete(TodoDeleteDTO.Request dto) throws ValidationException, UserInfoNotFoundException, NotFoundException, TodoException {
        try {
            Long todoId = dto.getTodoId();
            String userId = dto.getUserId();

            if (userId == null || StringUtils.isBlank(userId) || todoId == null) {
                throw new ValidationException("입력값을 확인해주세요");
            }

            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new UserInfoNotFoundException("사용자 정보가 일치하지 않습니다."));
            Todo todo = todoRepository.findById(todoId)
                    .orElseThrow(() -> new NotFoundException("해당 todo를 찾을 수 없습니다."));

            if (!todo.getMember().getId().equals(member.getId())) {
                throw new ValidationException("타인의 Todo를 삭제할 수 없습니다.");
            }

            todoRepository.delete(todo);
            return TodoDeleteDTO.Response.builder()
                    .todoId(todo.getId())
                    .build();
        } catch (ValidationException | UserInfoNotFoundException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new TodoException("Todo삭제 오류");
        }
    }
}
