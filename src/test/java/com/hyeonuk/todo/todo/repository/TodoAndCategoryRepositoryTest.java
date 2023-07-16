package com.hyeonuk.todo.todo.repository;

import com.hyeonuk.todo.integ.util.StringUtils;
import com.hyeonuk.todo.member.entity.Member;
import com.hyeonuk.todo.member.repository.MemberRepository;
import com.hyeonuk.todo.todo.entity.Category;
import com.hyeonuk.todo.todo.entity.Todo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class TodoAndCategoryRepositoryTest {
    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager em;


    private List<Member> memberList;
    private List<Todo>[] todoList;
    private List<Category>[] categoryList;

    private final int MEMBER_SIZE = 15;


    @BeforeEach
    public void init() {
        memberList = new ArrayList<>();
        todoList = new ArrayList[MEMBER_SIZE];
        categoryList = new ArrayList[MEMBER_SIZE];

        for (int i = 0; i < MEMBER_SIZE; i++) {
            todoList[i] = new ArrayList<>();
            categoryList[i] = new ArrayList<>();
        }

        for (int i = 0; i < MEMBER_SIZE; i++) {
            Member member = Member.builder()
                    .id("tester" + i)
                    .name("tester" + i)
                    .email("tester" + i + "@gmail.com")
                    .password("1111")
                    .build();

            memberList.add(member);
        }
        memberRepository.saveAll(memberList);

        for (int i = 0; i < 100; i++) {
            int mIndex = i % memberList.size();
            Member member = memberList.get(mIndex);

            Category category = Category.builder()
                    .title("title" + i)
                    .member(member)
                    .build();
            Todo todo = Todo.builder()
                    .category(todoList[mIndex].size() < 2 ? null : category)//카테고리가 null인 todo를 넣기위해서
                    .content(member.getName().concat("의 ").concat(Integer.toString(i).concat("번째 todo")))
                    .member(member)
                    .complete(todoList[mIndex].size() % 2 == 0 ? false : true)
                    .build();

            if (category != null) categoryList[mIndex].add(category);
            todoList[mIndex].add(todo);
        }
        for (int i = 0; i < MEMBER_SIZE; i++) {
            categoryRepository.saveAll(categoryList[i]);
            todoRepository.saveAll(todoList[i]);
        }
    }

    /**
     *
     * update 테스트
     *
     * */
    @Nested
    @DisplayName("UpdateTest")
    public class UpdateTest{
        /**
         *
         * 성공 케이스
         * 1. 내용 변경 성공 v
         * 2. 카테고리 변경 성공 v
         * 3. 완료 여부 변경 성공 v
         *
         * */
        @Nested
        @DisplayName("success")
        public class Success{
            @Test
            @DisplayName("1. 내용 변경 성공")
            public void updateContentSuccess(){
                //given
                int mIndex = 0;
                Todo todo = todoList[mIndex].get(0);

                String updateContent = "update Content!";

                //when
                todo.updateContent(updateContent);
                todoRepository.saveAndFlush(todo);

                //then
                Todo updated = todoRepository.findById(todo.getId()).get();
                assertThat(updated.getContent()).isEqualTo(updateContent);
            }

            @Test
            @DisplayName("2. 카테고리 변경 성공")
            public void updateCategorySuccess(){
                //given
                int mIndex = 0;
                Todo todo = todoList[mIndex].get(0);
                Category target = categoryList[mIndex].stream()
                        .filter(c->{
                            return (todo.getCategory() == null && c!=null) ||
                            c.getId()!=todo.getCategory().getId();
                        })
                        .findAny()
                        .get();
                //when
                todo.updateCategory(target);
                todoRepository.save(todo);

                //then
                Todo result = todoRepository.findById(todo.getId()).get();
                assertThat(result.getCategory().getId()).isEqualTo(target.getId());
            }

            @Test
            @DisplayName("3. 완료 여부 변경 성공")
            public void updateCompleteSuccess(){
                //given
                int mIndex = 0;
                Todo todo = todoList[mIndex].get(0);
                boolean before = todo.isComplete();

                //when
                todo.toggleComplete();
                todoRepository.save(todo);

                //then
                Todo result = todoRepository.findById(todo.getId()).get();
                assertThat(result.isComplete()).isEqualTo(!before);
            }
        }

        /**
         *
         * 실패 케이스
         * 1. 내용 변경 실패 (길이 초과) v
         * 2. 내용 변경 실패 (null) v
         * 3. 내용 변경 실패 (공백) v
         * 4. 카테고리 변경 실패 (존재하지 않는 카테고리) v
         * 5. 카테고리 변경 실패 (자신의 카테고리가 아닌 카테고리로 변경) v
         *
         * */
        @Nested
        @DisplayName("fail")
        public class Fail{
            @Test
            @DisplayName("1. 내용 변경 실패 (길이 초과)")
            public void updateContentFailOverLength(){
                //given
                int mIndex = 0;
                Todo todo = todoList[mIndex].get(0);

                String updateContent = StringUtils.randomSting(250);//길이가 250인 string

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                    todo.updateContent(updateContent);
                    todoRepository.saveAndFlush(todo);
                });
            }

            @Test
            @DisplayName("2. 내용 변경 실패 (null)")
            public void updateContentFailNull(){
                //given
                int mIndex = 0;
                Todo todo = todoList[mIndex].get(0);
                String beforeContent =todo.getContent();
                String updateContent = null;//null

                //when
                todo.updateContent(updateContent);
                todoRepository.save(todo);

                //then
                Todo result = todoRepository.findById(todo.getId()).get();
                assertThat(result.getContent()).isEqualTo(beforeContent);//바뀌기 이전과 같음
            }

            @Test
            @DisplayName("3. 내용 변경 실패 (공백)")
            public void updateContentFailBlank(){
                //given
                int mIndex = 0;
                Todo todo = todoList[mIndex].get(0);
                String beforeContent =todo.getContent();
                String updateContent = "                         ";//공백

                //when
                todo.updateContent(updateContent);
                todoRepository.save(todo);

                //then
                Todo result = todoRepository.findById(todo.getId()).get();
                assertThat(result.getContent()).isEqualTo(beforeContent);//바뀌기 이전과 같음
            }

            @Test
            @DisplayName("4. 카테고리 변경 실패 (존재하지 않는 카테고리)")
            public void updateCategoryNotExist(){
                //given
                int mIndex = 0;
                Todo todo = todoList[mIndex].get(0);
                //아직 존재하지 않는 카테고리
                Category category = Category.builder()
                        .title("notExist")
                        .member(memberList.get(mIndex))
                        .build();

                //when & then
                assertThrows(InvalidDataAccessApiUsageException.class,()->{
                    todo.updateCategory(category);
                    todoRepository.saveAndFlush(todo);
                });
            }

            @Test
            @DisplayName("5. 카테고리 변경 실패 (자신의 카테고리가 아닌 카테고리로 변경)")
            public void updateCategoryNotMine(){
                //given
                int mIndex = 0;
                Todo todo = todoList[mIndex].get(0);
                Category before = todo.getCategory();
                //다른사람의 카테고리를 적용하기 위해 가져옴
                Category category = categoryList[mIndex+1].stream().filter(c->c!=null).findFirst().get();

                //when
                todo.updateCategory(category);
                todoRepository.saveAndFlush(todo);

                //then
                //바뀌지 않음
                Todo result = todoRepository.findById(todo.getId()).get();
                assertThat(result.getCategory()).isEqualTo(before);
            }
        }
    }

    /**
     *
     * delete 테스트
     *
     * */
    @Nested
    @DisplayName("Delete Test")
    public class DeleteTest{
        /**
         *
         * 1. todo삭제 v
         * 2. 카테고리 삭제 v
         *
         */
        @Nested
        @DisplayName("success")
        public class Success{
            @Test
            @DisplayName("1. todo삭제")
            public void todoDeleteSuccess(){
                //given
                int mIndex = 0;
                Member member = memberList.get(mIndex);
                String memberId = member.getId();
                int beforeSize = todoList[mIndex].size();

                //when
                todoRepository.deleteById(todoList[mIndex].get(0).getId());

                //then
                List<Todo> todos = todoRepository.findTodosWithCategoriesByMemberId(memberId);

                assertThat(todos.size()).isEqualTo(beforeSize-1);
            }
            @Test
            @DisplayName("2. 카테고리 삭제")
            public void categoryDeleteSuccess(){
                //given
                int mIndex = 0;
                Member member = memberList.get(mIndex);
                String memberId = member.getId();
                int beforeSize = todoList[mIndex].size();

                //when
                todoRepository.deleteById(todoList[mIndex].get(0).getId());

                //then
                List<Todo> todos = todoRepository.findTodosWithCategoriesByMemberId(memberId);

                assertThat(todos.size()).isEqualTo(beforeSize-1);
            }
        }
        @Nested
        @DisplayName("fail")
        public class Fail{

        }
    }


    /**
     * save 테스트
     */
    @Nested
    @DisplayName("Save Test")
    public class SaveTest {

        /**
         * 성공케이스
         * 1. category가 null인 todo v
         * 2. category가 있는 todo v
         * 3. category 추가 v
         */
        @Nested
        @DisplayName("success")
        public class Success {
            @Test
            @DisplayName("1. category가 null인 todo save 성공")
            public void nullCategoryTodoSaveTest() {
                long beforeSize = todoRepository.count();
                int mIndex = 0;
                Member member = memberList.get(mIndex);

                Todo todo = Todo.builder()
                        .member(member)
                        .category(null)
                        .content("null category")
                        .build();

                todoRepository.save(todo);

                List<Todo> all = todoRepository.findAll();

                assertThat(all.size()).isEqualTo(beforeSize + 1);
                assertThat(all.contains(todo)).isTrue();

                //제대로 저장됐는지 확인
                Optional<Todo> result = todoRepository.findById(todo.getId());
                assertThat(result).isNotEmpty();

                Todo findById = result.get();
                assertAll("Assert Object",
                        () -> assertThat(findById.getContent()).isEqualTo(todo.getContent()),
                        () -> assertThat(findById.getCategory()).isNull(),
                        () -> assertThat(findById.getMember().getId()).isEqualTo(member.getId())
                );
            }

            @Test
            @DisplayName("2. category가 있는 todo")
            public void notNullCategoryTodoSaveTest() {
                long beforeSize = todoRepository.count();
                int mIndex = 0;
                Member member = memberList.get(mIndex);

                Category category = categoryList[mIndex].stream()
                        .findAny()
                        .get();//해당 유저의 카테고리중 null이 아닌것중 아무거나 하나
                Todo todo = Todo.builder()
                        .member(member)
                        .category(category)
                        .content("null category")
                        .build();

                todoRepository.save(todo);

                List<Todo> all = todoRepository.findAll();

                assertThat(all.size()).isEqualTo(beforeSize + 1);
                assertThat(all.contains(todo)).isTrue();

                //제대로 저장됐는지 확인
                Optional<Todo> result = todoRepository.findById(todo.getId());
                assertThat(result).isNotEmpty();

                Todo findById = result.get();
                assertAll("Assert Object",
                        () -> assertThat(findById.getContent()).isEqualTo(todo.getContent()),
                        () -> assertThat(findById.getCategory()).isEqualTo(category),
                        () -> assertThat(findById.getMember().getId()).isEqualTo(member.getId())
                );
            }

            @Test
            @DisplayName("3. category 추가")
            public void categorySaveTest() {
                //given
                int mIndex = 0;
                long beforeSize = categoryList[mIndex].size();
                Member member = memberList.get(mIndex);

                Category category = Category.builder()
                        .member(member)
                        .title("new Title!")
                        .build();

                //when
                categoryRepository.saveAndFlush(category);

                //then
                assertThat(categoryRepository.findByMember(member).size()).isEqualTo(beforeSize+1);
            }
        }


        /**
         * 실패케이스
         * 1. 존재하지 않는 category인 todo v
         * 2. 존재하지 않는 member인 todo v
         * 3. 빈 todo v
         * 4. 글자수 200자 초과 v
         * 5. category title overlength v
         * 6. 존재하지 않는 member category v
         */
        @Nested
        @DisplayName("fail")
        public class Fail {
            @Test
            @DisplayName("1. 존재하지 않는 category인 todo")
            public void notExistCategoryTodoTest() {
                //given
                int mIndex = 0;
                Member member = memberList.get(mIndex);//멤버는 존재하는걸로

                Category notExistCategory = Category.builder()
                        .id(categoryList[mIndex].size() + 1000l)//존재하지않는 카테고리id
                        .title("notExistCategory")
                        .build();//id값이 없는 카테고리 (존재하지 않는 카테고리)

                Todo todo = Todo.builder()
                        .category(notExistCategory)
                        .member(member)
                        .content("존재하지않는 투두!")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class, () -> {
                    todoRepository.saveAndFlush(todo);
                });
            }

            @Test
            @DisplayName("2. 존재하지 않는 member인 todo")
            public void notExistMemberTodoTest() {
                //given
                Member notExistMember = Member.builder()
                        .id("notExistMember")
                        .build();

                Category category = categoryList[0].get(0);//카테고리는 존재하는걸로

                Todo todo = Todo.builder()
                        .category(category)
                        .member(notExistMember)
                        .content("존재하지않는 사용자!")
                        .build();

                //when & then
                assertThrows(InvalidDataAccessApiUsageException.class, () -> {
                    todoRepository.saveAndFlush(todo);
                });
            }

            @Test
            @DisplayName("3. 빈 todo")
            public void notExistTodoContentTest(){
                //given
                int mIndex = 0;
                Todo todo = Todo.builder()
                        .content(null)
                        .member(memberList.get(mIndex))
                        .category(categoryList[mIndex].get(0))
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                    todoRepository.saveAndFlush(todo);
                });
            }

            @Test
            @DisplayName("4. 글자수 200자 초과")
            public void todoContentOverLengthTest(){
                //given
                int mIndex = 0;
                Todo todo = Todo.builder()
                        .content(StringUtils.randomSting(250))
                        .member(memberList.get(mIndex))
                        .category(categoryList[mIndex].get(0))
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                    todoRepository.saveAndFlush(todo);
                });
            }

            @Test
            @DisplayName("5. category title overlength")
            public void categoryTitleOverlength(){
                //given
                int mIndex = 0;
                Member member = memberList.get(mIndex);

                Category category = Category.builder()
                        .member(member)
                        .title(StringUtils.randomSting(150))
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                    categoryRepository.saveAndFlush(category);
                });
            }

            @Test
            @DisplayName("6. 존재하지 않는 member category")
            public void categoryMemberNotExist(){
                //given
                Member member = Member.builder()
                        .id("notExist")
                        .build();

                Category category = Category.builder()
                        .member(member)
                        .title(StringUtils.randomSting(150))
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                    categoryRepository.saveAndFlush(category);
                });
            }
        }
    }


    /**
     * memberId를 이용하여 todos를 불러옴. complete asc, createdAt desc
     */
    @Nested
    @DisplayName("findTodosWithCategoriesByMemberId")
    public class FindTodosWithCategoriesByMemberIdTest {

        /**
         * 성공케이스
         * 1. 저장된 모든 Todo들을 불러옴 v
         */
        @Nested
        @DisplayName("success")
        public class Success {
            @Test
            @DisplayName("1. 저장된 모든 Todo들을 불러옴")
            public void findAllSuccess() {
                for (int i = 0; i < MEMBER_SIZE; i++) {
                    Member member = memberList.get(i);

                    List<Todo> todos = todoRepository.findTodosWithCategoriesByMemberId(member.getId());

                    assertThat(todos.size()).isEqualTo(todoList[i].size());//사이즈가 일치해야함

                    for (int j = 0; j < todos.size(); j++) {
                        Todo todo = todos.get(j);
                        assertThat(todoList[i].contains(todo)).isTrue();
                    }
                }
            }
        }

        /**
         * 실패 케이스
         * 1. 없는 Id로 불러옴 v
         */
        @Nested
        @DisplayName("failure")
        public class Failure {
            @Test
            @DisplayName("1. 없는 Id로 불러옴")
            public void notExistMember() {
                Member notExistMember = Member.builder()
                        .id("notExistUser")
                        .build();

                List<Todo> todos = todoRepository.findTodosWithCategoriesByMemberId(notExistMember.getId());

                assertThat(todos.isEmpty()).isTrue();
            }
        }
    }

}