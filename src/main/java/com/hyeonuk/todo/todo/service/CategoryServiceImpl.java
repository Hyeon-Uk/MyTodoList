package com.hyeonuk.todo.todo.service;

import com.hyeonuk.todo.integ.exception.NotFoundException;
import com.hyeonuk.todo.integ.exception.ValidationException;
import com.hyeonuk.todo.integ.util.StringUtils;
import com.hyeonuk.todo.member.entity.Member;
import com.hyeonuk.todo.member.exception.UserInfoNotFoundException;
import com.hyeonuk.todo.member.repository.MemberRepository;
import com.hyeonuk.todo.todo.dto.CategoryDeleteDTO;
import com.hyeonuk.todo.todo.dto.CategorySaveDTO;
import com.hyeonuk.todo.todo.dto.CategoryUpdateDTO;
import com.hyeonuk.todo.todo.entity.Category;
import com.hyeonuk.todo.todo.exception.CategoryException;
import com.hyeonuk.todo.todo.repository.CategoryRepository;
import com.hyeonuk.todo.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.el.util.Validation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final TodoRepository todoRepository;

    @Override
    public CategorySaveDTO.Response save(CategorySaveDTO.Request dto) throws UserInfoNotFoundException, ValidationException, CategoryException {
        try {
            if (dto.getUserId() == null || StringUtils.isBlank(dto.getUserId()) ||
                    dto.getTitle() == null || StringUtils.isBlank(dto.getTitle())) {
                throw new ValidationException("입력값을 확인해주세요");
            }

            if(dto.getTitle().length() > 100){
                throw new ValidationException("카테고리 타이틀은 100자 이하입니다.");
            }

            String userId = dto.getUserId();
            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new UserInfoNotFoundException("사용자 정보가 일치하지 않습니다."));

            Category category = Category.builder()
                    .member(member)
                    .title(dto.getTitle())
                    .build();

            categoryRepository.save(category);

            return CategorySaveDTO.Response.builder()
                    .categoryId(category.getId())
                    .title(category.getTitle())
                    .build();
        }catch(UserInfoNotFoundException | ValidationException e){
            throw e;
        }catch(Exception e){
            throw new CategoryException("카테고리 저장 오류");
        }
    }

    @Override
    @Transactional
    public CategoryDeleteDTO.Response delete(CategoryDeleteDTO.Request dto) throws UserInfoNotFoundException, NotFoundException, ValidationException, CategoryException {
        try {
            if (dto.getUserId() == null || StringUtils.isBlank(dto.getUserId())) {
                throw new ValidationException("입력값을 확인해주세요");
            }
            if(dto.getCategoryId() == null){
                throw new ValidationException("기본 카테고리는 제거할 수 없습니다.");
            }

            String userId = dto.getUserId();
            Long categoryId = dto.getCategoryId();

            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new UserInfoNotFoundException("사용자 정보가 일치하지 않습니다."));

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("해당 category를 찾을 수 없습니다."));

            if (!category.getMember().getId().equals(member.getId())) {
                throw new ValidationException("입력값을 확인해주세요");
            }

            todoRepository.deleteAllByCategory(category);
            categoryRepository.deleteById(category.getId());

            return CategoryDeleteDTO.Response.builder()
                    .categoryId(categoryId)
                    .result(true)
                    .build();
        }catch(UserInfoNotFoundException | ValidationException | NotFoundException e){
            throw e;
        }catch(Exception e){
            throw new CategoryException("카테고리 삭제 오류");
        }
    }

    @Override
    public CategoryUpdateDTO.Response update(CategoryUpdateDTO.Request dto) throws ValidationException, UserInfoNotFoundException, NotFoundException, CategoryException {
        try {
            if (dto.getUserId() == null || StringUtils.isBlank(dto.getUserId()) ||
                    dto.getTitle() == null || StringUtils.isBlank(dto.getTitle())) {
                throw new ValidationException("입력값을 확인해주세요");
            }
            if(dto.getCategoryId() == null){
                throw new ValidationException("기본 카테고리의 타이틀은 변경할 수 없습니다.");
            }

            if(dto.getTitle().length() > 100){
                throw new ValidationException("카테고리 타이틀은 100자 이하입니다.");
            }

            String userId = dto.getUserId();
            Long categoryId = dto.getCategoryId();
            String title = dto.getTitle();

            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new UserInfoNotFoundException("사용자 정보가 일치하지 않습니다."));

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("해당 category를 찾을 수 없습니다."));

            if (!category.getMember().getId().equals(member.getId())) {
                throw new ValidationException("입력값을 확인해주세요");
            }

            category.updateTitle(title);

            categoryRepository.save(category);

            return CategoryUpdateDTO.Response.builder()
                    .categoryId(categoryId)
                    .title(title)
                    .build();
        }catch(ValidationException | UserInfoNotFoundException | NotFoundException e){
            throw e;
        }catch(Exception e){
            throw new CategoryException("카테고리 수정 오류");
        }
    }
}
