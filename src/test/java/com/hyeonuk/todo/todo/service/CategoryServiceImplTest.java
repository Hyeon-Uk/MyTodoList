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
import com.hyeonuk.todo.todo.entity.Todo;
import com.hyeonuk.todo.todo.exception.CategoryException;
import com.hyeonuk.todo.todo.repository.CategoryRepository;
import com.hyeonuk.todo.todo.repository.TodoRepository;
import org.apache.el.util.Validation;
import org.aspectj.util.Reflection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;


@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {
    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private CategoryRepository categoryRepository;

    private List<Todo> todoList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private List<Member> memberList = new ArrayList<>();

    //auto generated 를 위한 임시변수
    private Long cId = 1l;
    private Long tId = 1l;

    @BeforeEach
    public void init() {
        //memberRepository mocking
        lenient().when(memberRepository.findAll()).thenReturn(memberList);
        lenient().when(memberRepository.findById(anyString())).thenAnswer(invocation -> {
            String userId = invocation.getArgument(0, String.class);

            return memberList.stream()
                    .filter(m -> m.getId().equals(userId))
                    .findFirst();
        });
        lenient().when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0, Member.class);

            Member exist = memberList.stream()
                    .filter(m -> m.getId().equals(member.getId()))
                    .findAny().orElse(null);

            if (exist != null) {
                memberList.remove(exist);
            }

            Member newMember = Member.builder()
                    .id(member.getId())
                    .name(member.getName())
                    .email(member.getEmail())
                    .password(member.getPassword())
                    .tryCount(member.getTryCount())
                    .blockedTime(member.getBlockedTime())
                    .roles(member.getRoles())
                    .img(member.getImg())
                    .description(member.getDescription())
                    .build();

            memberList.add(newMember);
            return newMember;
        });

        //categoryRepository mocking
        lenient().when(categoryRepository.findAll()).thenReturn(categoryList);
        lenient().when(categoryRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long categoryId = invocation.getArgument(0, Long.class);

            return categoryList.stream()
                    .filter(c -> c.getId().equals(categoryId))
                    .findFirst();
        });
        lenient().when(categoryRepository.findByMember(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0, Member.class);

            return categoryList.stream()
                    .filter(c -> c.getMember().getId().equals(member.getId()))
                    .collect(Collectors.toList());
        });
        lenient().when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0, Category.class);
            Category exist = categoryList.stream()
                    .filter(c -> c.getId().equals(category.getId()))
                    .findAny().orElse(null);

            if (exist != null) {//존재한다면 먼저 삭제
                categoryList.remove(exist);
            }
            Field field = Category.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(category, exist != null ? exist.getId() : cId++);
            categoryList.add(category);
            return category;
        });
        lenient().doAnswer(invocation -> {
            Long id = invocation.getArgument(0,Long.class);
            categoryList = categoryList.stream()
                    .filter(c->!c.getId().equals(id))
                    .collect(Collectors.toList());

            return null;
        }).when(categoryRepository).deleteById(anyLong());

        //todoRepository mocking
        lenient().when(todoRepository.findAll()).thenReturn(todoList);

        lenient().doAnswer(invocation -> {
            Category category = invocation.getArgument(0, Category.class);
            todoList.stream()
                    .filter(t -> t.getCategory().getId().equals(category.getId()))
                    .collect(Collectors.toList());
            return null;
        }).when(todoRepository).deleteAllByCategory(any(Category.class));
        lenient().when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> {
            Todo todo = invocation.getArgument(0, Todo.class);

            Todo exist = todoList.stream()
                    .filter(t -> t.getId().equals(todo.getId()))
                    .findAny().orElse(null);

            if (exist != null) {
                todoList.remove(exist);
            }
            Field field = Todo.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(todo, exist != null ? exist.getId() : tId++);
            todoList.add(todo);
            return todo;
        });
        lenient().doAnswer(invocation -> {
            Category category = invocation.getArgument(0,Category.class);

            todoList = todoList.stream()
                    .filter(t->!t.getCategory().getId().equals(category.getId()))
                    .collect(Collectors.toList());

            return null;
        }).when(todoRepository).deleteAllByCategory(any(Category.class));
    }

    @BeforeEach
    public void insertDummies() {
        for (int i = 0; i < 15; i++) {
            Member member = Member.builder()
                    .id("tester".concat(Integer.toString(i)))
                    .email("tester".concat(Integer.toString(i)).concat("@gmail.com"))
                    .password("1111")
                    .name("tester".concat(Integer.toString(i)))
                    .build();

            memberRepository.save(member);
            categoryRepository.save(Category.builder()
                    .member(member)
                    .title("일반")
                    .build());
        }

        for (int i = 0; i < 100; i++) {
            Member member = memberList.get(i % memberList.size());
            Category category = Category.builder()
                    .title("title".concat(Integer.toString(i)))
                    .member(member)
                    .build();

            categoryRepository.save(category);
            for (int j = 0; j < 10; j++) {
                Todo todo = Todo.builder()
                        .category(category)
                        .member(member)
                        .content("content".concat(Integer.toString(j)))
                        .build();

                todoRepository.save(todo);
            }
        }
    }

    @Nested
    @DisplayName("categorySaveTest")
    public class CategorySaveTest {
        /**
         * 성공 케이스
         * 1. 카테고리 정상 저장 v
         */
        @Nested
        @DisplayName("success")
        public class Success {
            @Test
            @DisplayName("1. 카테고리 정상 저장")
            public void saveSuccessTest() throws ValidationException, CategoryException, UserInfoNotFoundException {
                //given
                Member member = memberList.get(0);

                CategorySaveDTO.Request dto = CategorySaveDTO.Request.builder()
                        .userId(member.getId())
                        .title("newTitle")
                        .build();

                int beforeSize = categoryRepository.findByMember(member).size();

                //when
                CategorySaveDTO.Response result = categoryService.save(dto);

                //then
                assertThat(result.getCategoryId()).isEqualTo(cId - 1);
                assertThat(result.getTitle()).isEqualTo(dto.getTitle());
                assertThat(categoryRepository.findByMember(member).size()).isEqualTo(beforeSize + 1);
            }
        }

        /**
         * 실패 케이스
         * 1. 유저 아이디 null v
         * 2. 유저 아이디 blank v
         * 3. 입력 타이틀 null v
         * 4. 입력 타이틀 blank v
         * 5. 찾을 수 없는 유저 v
         * 6. 카테고리 타이틀 길이 100 over v
         */
        @Nested
        @DisplayName("fail")
        public class Fail {
            @Test
            @DisplayName("1. 유저 아이디 null")
            public void userIdNullTest() {
                //given
                CategorySaveDTO.Request dto = CategorySaveDTO.Request.builder()
                        .title("new title!")
                        .userId(null)
                        .build();

                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.save(dto);
                }).getMessage();

                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }

            @Test
            @DisplayName("2. 유저 아이디 blank")
            public void userIdBlankTest() {
                //given
                CategorySaveDTO.Request dto = CategorySaveDTO.Request.builder()
                        .title("new title!")
                        .userId("          ")
                        .build();

                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.save(dto);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }

            @Test
            @DisplayName("3. 입력 타이틀 null")
            public void titleNullTest() {
                //given
                Member member = memberList.get(0);
                CategorySaveDTO.Request dto = CategorySaveDTO.Request.builder()
                        .title(null)
                        .userId(member.getId())
                        .build();

                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.save(dto);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }

            @Test
            @DisplayName("4. 입력 타이틀 blank")
            public void titleBlankTest() {
                //given
                Member member = memberList.get(0);
                CategorySaveDTO.Request dto = CategorySaveDTO.Request.builder()
                        .title("            ")
                        .userId(member.getId())
                        .build();

                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.save(dto);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }

            @Test
            @DisplayName("5. 찾을 수 없는 유저")
            public void canNotFindMember() {
                //given
                CategorySaveDTO.Request dto = CategorySaveDTO.Request.builder()
                        .title("newTitle!")
                        .userId("unknownUserId!")
                        .build();

                //when & then
                String message = assertThrows(UserInfoNotFoundException.class, () -> {
                    categoryService.save(dto);
                }).getMessage();
                assertThat(message).isEqualTo("사용자 정보가 일치하지 않습니다.");
            }

            @Test
            @DisplayName("6. 카테고리 타이틀 길이 100 over")
            public void titleOverLength() {
                //given
                Member member = memberList.get(0);
                CategorySaveDTO.Request dto = CategorySaveDTO.Request.builder()
                        .title(StringUtils.randomSting(101))
                        .userId(member.getId())
                        .build();

                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.save(dto);
                }).getMessage();
                assertThat(message).isEqualTo("카테고리 타이틀은 100자 이하입니다.");
            }
        }
    }

    @Nested
    @DisplayName("categoryDeleteTest")
    public class CategoryDeleteTest {
        /**
         * TODO
         * 성공 케이스
         * 1. 카테고리 정상 삭제. 하위 todo들 모두 삭제 v
         */

        @Nested
        @DisplayName("success")
        public class Success {
            @Test
            @DisplayName("1. 카테고리 정상 삭제. 하위 todo들 모두 삭제")
            public void deleteSuccess() throws ValidationException, CategoryException, UserInfoNotFoundException, NotFoundException {
                //given
                Member member = memberList.get(0);
                List<Category> categories = categoryRepository.findByMember(member);

                Category target = categories.get(0);
                CategoryDeleteDTO.Request dto = CategoryDeleteDTO.Request.builder()
                        .categoryId(target.getId())
                        .userId(member.getId())
                        .build();
                //when
                categoryService.delete(dto);

                //then
                assertThat(categoryRepository.findByMember(member).contains(target)).isFalse();
                List<Todo> todos = todoRepository.findTodosWithCategoriesByMemberId(member.getId());
                todos.stream().forEach(t->{
                    assertThat(t.getCategory().getId()!=target.getId());
                });
            }
        }

        /**
         * 실패 케이스
         * 1. 유저 아이디 null v
         * 2. 유저 아이디 blank v
         * 3. 삭제할 categoryid null v
         * 4. 찾을 수 없는 유저 v
         * 5. 찾을 수 없는 카테고리 v
         * 6. 다른사람의 카테고리 삭제하려함 v
         */
        @Nested
        @DisplayName("fail")
        public class Fail {
            @Test
            @DisplayName("1. 유저 아이디 null")
            public void memberIdNull(){
                //given
                Category category = categoryList.get(0);
                CategoryDeleteDTO.Request dto = CategoryDeleteDTO.Request.builder()
                        .userId(null)
                        .categoryId(category.getId())
                        .build();
                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.delete(dto);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }

            @Test
            @DisplayName("2. 유저 아이디 blank")
            public void memberIdBlank(){
                //given
                Category category = categoryList.get(0);
                CategoryDeleteDTO.Request dto = CategoryDeleteDTO.Request.builder()
                        .userId("          ")
                        .categoryId(category.getId())
                        .build();
                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.delete(dto);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }

            @Test
            @DisplayName("3. 삭제할 categoryid null")
            public void categoryIdNull(){
                //given
                Member member = memberList.get(0);
                CategoryDeleteDTO.Request dto = CategoryDeleteDTO.Request.builder()
                        .userId(member.getId())
                        .categoryId(null)
                        .build();
                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.delete(dto);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }

            @Test
            @DisplayName("4. 찾을 수 없는 유저")
            public void canNotFindUser(){
                //given
                CategoryDeleteDTO.Request dto = CategoryDeleteDTO.Request.builder()
                        .userId("unknownUserId")
                        .categoryId(categoryList.get(0).getId())
                        .build();
                //when & then
                String message = assertThrows(UserInfoNotFoundException.class, () -> {
                    categoryService.delete(dto);
                }).getMessage();
                assertThat(message).isEqualTo("사용자 정보가 일치하지 않습니다.");
            }

            @Test
            @DisplayName("5. 찾을 수 없는 카테고리")
            public void canNotFindCategory(){
                //given
                Member member = memberList.get(0);
                CategoryDeleteDTO.Request dto = CategoryDeleteDTO.Request.builder()
                        .userId(member.getId())
                        .categoryId(Long.MAX_VALUE)
                        .build();
                //when & then
                String message = assertThrows(NotFoundException.class, () -> {
                    categoryService.delete(dto);
                }).getMessage();
                assertThat(message).isEqualTo("해당 category를 찾을 수 없습니다.");
            }

            @Test
            @DisplayName("6. 다른사람의 카테고리 삭제하려함")
            public void canNotDeleteOtherCategory(){
                //given
                Member member = memberList.get(0);
                Category other = categoryList.stream()
                        .filter(c->!c.getMember().getId().equals(member.getId()))
                        .findFirst().get();
                CategoryDeleteDTO.Request dto = CategoryDeleteDTO.Request.builder()
                        .userId(member.getId())
                        .categoryId(other.getId())
                        .build();
                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.delete(dto);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }
        }
    }

    @Nested
    @DisplayName("categoryUpdateTest")
    public class CategoryUpdateTest {
        /**
         * 성공 케이스
         * 1. 카테고리 정상 업데이트 v
         */
        @Nested
        @DisplayName("success")
        public class Success {
            @Test
            @DisplayName("1. 카테고리 정상 업데이트")
            public void categoryUpdateSuccess() throws ValidationException, CategoryException, UserInfoNotFoundException, NotFoundException {
                //given
                Member member = memberList.get(0);
                Category category = categoryList.stream()
                        .filter(c->c.getMember().getId().equals(member.getId()))
                        .findAny().get();
                String exchangeTitle = "updated Title!";

                //when
                CategoryUpdateDTO.Request dto = CategoryUpdateDTO.Request.builder()
                        .userId(member.getId())
                        .categoryId(category.getId())
                        .title(exchangeTitle)
                        .build();

                CategoryUpdateDTO.Response result = categoryService.update(dto);

                //then
                assertThat(result.getTitle()).isEqualTo(exchangeTitle);
                assertThat(result.getCategoryId()).isEqualTo(category.getId());
            }
        }

        /**
         * 실패 케이스
         * 1. 유저 아이디 null v
         * 2. 유저 아이디 blank v
         * 3. 삭제할 categoryid null v
         * 4. 바꿀 타이틀 null v
         * 5. 바꿀 타이틀 공백 v
         * 6. 바꿀 타이틀이 100자 초과 v
         * 7. 찾을 수 없는 유저 v
         * 8. 찾을 수 없는 카테고리 v
         * 9. 다른사람의 카테고리를 업데이트 하려함 v
         */
        @Nested
        @DisplayName("fail")
        public class Fail {
            @Test
            @DisplayName("1. 유저 아이디 null")
            public void memberIdNull(){
                String exchangeTitle = "updated Title!";
                //given
                CategoryUpdateDTO.Request dto = CategoryUpdateDTO.Request.builder()
                        .title(exchangeTitle)
                        .userId(null)
                        .categoryId(categoryList.get(0).getId())
                        .build();

                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.update(dto);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }
            @Test
            @DisplayName("2. 유저 아이디 blank")
            public void memberIdBlank(){
                String exchangeTitle = "updated Title!";
                //given
                CategoryUpdateDTO.Request dto = CategoryUpdateDTO.Request.builder()
                        .title(exchangeTitle)
                        .userId("               ")
                        .categoryId(categoryList.get(0).getId())
                        .build();

                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.update(dto);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }
            @Test
            @DisplayName("3. 삭제할 categoryid null")
            public void categoryIdNull(){
                String exchangeTitle = "updated Title!";
                Member member = memberList.get(0);
                //given
                CategoryUpdateDTO.Request dto = CategoryUpdateDTO.Request.builder()
                        .title(exchangeTitle)
                        .userId(member.getId())
                        .categoryId(null)
                        .build();

                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.update(dto);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }
            @Test
            @DisplayName("4. 바꿀 타이틀 null")
            public void titleNull(){
                Member member = memberList.get(0);
                Category category = categoryList.stream()
                        .filter(c->c.getMember().getId().equals(member.getId()))
                        .findAny().get();

                //given
                CategoryUpdateDTO.Request dto = CategoryUpdateDTO.Request.builder()
                        .title(null)
                        .userId(member.getId())
                        .categoryId(category.getId())
                        .build();

                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.update(dto);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }
            @Test
            @DisplayName("5. 바꿀 타이틀 공백")
            public void titleBlank() {
                Member member = memberList.get(0);
                Category category = categoryList.stream()
                        .filter(c -> c.getMember().getId().equals(member.getId()))
                        .findAny().get();

                //given
                CategoryUpdateDTO.Request dto = CategoryUpdateDTO.Request.builder()
                        .title("              ")
                        .userId(member.getId())
                        .categoryId(category.getId())
                        .build();

                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.update(dto);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }
            @Test
            @DisplayName("6. 바꿀 타이틀이 100자 초과")
            public void titleOverLength(){
                Member member = memberList.get(0);
                Category category = categoryList.stream()
                        .filter(c -> c.getMember().getId().equals(member.getId()))
                        .findAny().get();
                String overLengthTitle = StringUtils.randomSting(101);
                //given
                CategoryUpdateDTO.Request dto = CategoryUpdateDTO.Request.builder()
                        .title(overLengthTitle)
                        .userId(member.getId())
                        .categoryId(category.getId())
                        .build();

                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.update(dto);
                }).getMessage();
                assertThat(message).isEqualTo("카테고리 타이틀은 100자 이하입니다.");
            }
            @Test
            @DisplayName("7. 찾을 수 없는 유저")
            public void canNotFindMember(){
                Member unknownMember = Member.builder()
                        .id("unknownMember")
                        .build();
                Category category = categoryList.get(0);
                String exchangeTitle = "updated title!";
                //given
                CategoryUpdateDTO.Request dto = CategoryUpdateDTO.Request.builder()
                        .title(exchangeTitle)
                        .userId(unknownMember.getId())
                        .categoryId(category.getId())
                        .build();

                //when & then
                String message = assertThrows(UserInfoNotFoundException.class, () -> {
                    categoryService.update(dto);
                }).getMessage();
                assertThat(message).isEqualTo("사용자 정보가 일치하지 않습니다.");
            }
            @Test
            @DisplayName("8. 찾을 수 없는 카테고리")
            public void canNotFindCategory(){
                Member member = memberList.get(0);
                Category unknownCategory = Category.builder()
                        .id(Long.MAX_VALUE)
                        .build();
                String exchangeTitle = "updated title!";
                //given
                CategoryUpdateDTO.Request dto = CategoryUpdateDTO.Request.builder()
                        .title(exchangeTitle)
                        .userId(member.getId())
                        .categoryId(unknownCategory.getId())
                        .build();

                //when & then
                String message = assertThrows(NotFoundException.class, () -> {
                    categoryService.update(dto);
                }).getMessage();
                assertThat(message).isEqualTo("해당 category를 찾을 수 없습니다.");
            }
            @Test
            @DisplayName("9. 다른사람의 카테고리를 삭제하려함")
            public void canNotUpdateOthersCategory(){
                Member member = memberList.get(0);
                Category otherCategory = categoryList.stream()
                        .filter(c->!c.getMember().getId().equals(member.getId()))
                        .findAny().get();
                String exchangeTitle = "updated title!";
                //given
                CategoryUpdateDTO.Request dto = CategoryUpdateDTO.Request.builder()
                        .title(exchangeTitle)
                        .userId(member.getId())
                        .categoryId(otherCategory.getId())
                        .build();

                //when & then
                String message = assertThrows(ValidationException.class, () -> {
                    categoryService.update(dto);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 확인해주세요");
            }
        }
    }
}
