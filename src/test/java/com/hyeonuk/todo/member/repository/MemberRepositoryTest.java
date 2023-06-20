package com.hyeonuk.todo.member.repository;

import com.hyeonuk.todo.MyTodoListApplication;
import com.hyeonuk.todo.integ.data.MEMBER_MAX_LENGTH;
import com.hyeonuk.todo.member.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(MyTodoListApplication.class)
class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    private List<Member> memberList = new ArrayList<>();


    @BeforeEach
    public void insertDummies() {
        IntStream.rangeClosed(1, 200).forEach(i -> {
            Member member = Member.builder()
                    .id("Tester".concat(Integer.toString(i)))
                    .email("tester".concat(Integer.toString(i)).concat("@gmail.com"))
                    .name("tester".concat(Integer.toString(i)))
                    .password("1111")
                    .build();
            memberList.add(member);
        });

        memberRepository.saveAllAndFlush(memberList);
    }

    /**
     * save
     * - 성공테스트 v
     * - 실패 테스트
     * - 이메일 중복 검사 v
     * - 아이디 null v
     * - 비밀번호 null v
     * - 이름 null v
     * - 아이디 길이 초과 v
     * - 이메일 길이 초과 v
     * - 비밀번호 길이 초과 v
     * - 이미지 길이 초과 v
     * - 설명 길이 초과 v
     */
    @Nested
    @DisplayName("save test")//세이브를 테스트할 클래스
    public class SaveTest {
        @Nested
        @DisplayName("success")
        public class Success {
            @Test
            @DisplayName("success")
            public void saveSuccess() {
                long beforeCount = memberRepository.findAll().size();

                //given
                Member member = Member.builder()
                        .id("ForSave")
                        .name("HyeonUk")
                        .password("1111")
                        .email("notExistEmail@gmail.com")
                        .build();


                //when
                Member saved = memberRepository.save(member);

                //then
                long afterCount = memberRepository.findAll().size();
                assertThat(afterCount - beforeCount).isEqualTo(1);

                //member 엔티티 체크
                assertThat(saved.getId()).isEqualTo(member.getId());
                assertThat(saved.getEmail()).isEqualTo(member.getEmail());
                assertThat(saved.getPassword()).isEqualTo(member.getPassword());
                assertThat(saved.getName()).isEqualTo(member.getName());
                assertThat(saved.getImg()).isEqualTo(member.getImg());
                assertThat(saved.getDescription()).isEqualTo(member.getDescription());
                assertThat(saved.getCreatedAt()).isNotNull();
                assertThat(saved.getUpdatedAt()).isNotNull();

                assertThat(saved.getBlockedTime()).isNull();
                assertThat(saved.getTryCount()).isEqualTo(0);
            }
        }

        @Nested
        @DisplayName("fail")
        public class Fail {
            @Test
            @DisplayName("이메일 중복")
            public void duplicatedId() {
                //given
                String alreadyUserEmail = memberList.get(0).getEmail();
                Member duplicatedMember = Member.builder()
                        .id("notExistUserId")
                        .email(alreadyUserEmail)
                        .password("1111")
                        .name("hyeonuk")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class, () -> {
                    memberRepository.saveAndFlush(duplicatedMember);
                });
            }

            @Test
            @DisplayName("id null")
            public void idNull() {
                //given
                Member member = Member.builder()
                        .email("email@gmail.com")
                        .name("name")
                        .password("1111")
                        .build();

                //when & then
                assertThrows(JpaSystemException.class, () -> {
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("email null")
            public void emailNull() {
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .name("name")
                        .password("1111")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class, () -> {
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("password null")
            public void pwNull() {
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email("email@gmail.com")
                        .name("name")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class, () -> {
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("name null")
            public void nameNull() {
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email("email@gmail.com")
                        .password("1111")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class, () -> {
                    memberRepository.saveAndFlush(member);
                });
            }

            //해당 길이만큼의 더미 스트링을 반환해주는 메서드
            private String makeLengthString(int length) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    sb.append('1');
                }
                return sb.toString();
            }

            @Test
            @DisplayName("아이디 길이 초과")
            public void longId() {
                //given
                Member member = Member.builder()
                        .id(makeLengthString(MEMBER_MAX_LENGTH.ID.getValue() + 1))
                        .email("email@gmail.com")
                        .password("1111")
                        .name("hyeonuk")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class, () -> {
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("이메일 길이 초과")
            public void longEmail() {
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email(makeLengthString(MEMBER_MAX_LENGTH.EMAIL.getValue() + 1))
                        .password("1111")
                        .name("hyeonuk")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class, () -> {
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("비밀번호 길이 초과")
            public void longPassword() {
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email("email@gmail.com")
                        .password(makeLengthString(MEMBER_MAX_LENGTH.PASSWORD.getValue() + 1))
                        .name("hyeonuk")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class, () -> {
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("이름 길이 초과")
            public void longName() {
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email("email@gmail.com")
                        .password("1111")
                        .name(makeLengthString(MEMBER_MAX_LENGTH.NAME.getValue()) + 1)
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class, () -> {
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("이미지 길이 초과")
            public void longImg() {
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email("email@gmail.com")
                        .password("1111")
                        .name("hyeonuk")
                        .img(makeLengthString(MEMBER_MAX_LENGTH.IMG.getValue() + 1))
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class, () -> {
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("설명 길이 초과")
            public void longDesc() {
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email("email@gmail.com")
                        .password("1111")
                        .name("hyeonuk")
                        .description(makeLengthString(MEMBER_MAX_LENGTH.DESC.getValue() + 1))
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class, () -> {
                    memberRepository.saveAndFlush(member);
                });
            }
        }
    }

    /**
     * update
     * - 성공테스트
     * - 이름 변경 테스트, 중복 가능 v
     * - 이메일 변경 테스트 v
     * - 비밀번호 변경 테스트 v
     * - tryCount 업데이트 v
     * - blockedTime 업데이트 v
     * - 업데이트 후 updated_at 체크 v
     * - 실패테스트
     * - 변경 이메일 중복v
     */
    @Nested
    @DisplayName("update test")
    public class UpdateTest {
        @Nested
        @DisplayName("success")
        public class Success {
            @Test
            @DisplayName("이름 변경 테스트, 중복 가능")
            public void nameUpdateTest() {
                //given
                String targetId = memberList.get(0).getId();
                Member target = memberRepository.findById(targetId).get();
                LocalDateTime beforeUpdate = target.getUpdatedAt();
                String changeName = memberList.get(1).getName();


                //when
                Member saved = Member.builder()
                        .name(changeName)
                        .id(target.getId())
                        .password(target.getPassword())
                        .email(target.getEmail())
                        .roles(target.getRoles())
                        .tryCount(target.getTryCount())
                        .blockedTime(target.getBlockedTime())
                        .build();

                memberRepository.saveAndFlush(saved);
                Member member = memberRepository.findById(targetId).get();
                //then
                assertAll("member update assert",
                        () -> assertThat(member.getName()).isEqualTo(changeName),
                        () -> assertThat(member.getEmail()).isEqualTo(target.getEmail()),
                        () -> assertThat(member.getId()).isEqualTo(target.getId()),
                        () -> assertThat(member.getImg()).isEqualTo(target.getImg()),
                        () -> assertThat(member.getPassword()).isEqualTo(target.getPassword()),
                        () -> assertThat(member.getUpdatedAt()).isNotEqualTo(beforeUpdate));
            }

            @Test
            @DisplayName("이메일 변경 테스트")
            public void emailUpdateTest() {
                //given
                String targetId = memberList.get(0).getId();
                Member target = memberRepository.findById(targetId).get();
                LocalDateTime beforeUpdate = target.getUpdatedAt();
                String changeEmail = "changeEmail@notExist.com";


                //when
                Member saved = Member.builder()
                        .name(target.getName())
                        .id(target.getId())
                        .password(target.getPassword())
                        .email(changeEmail)
                        .roles(target.getRoles())
                        .tryCount(target.getTryCount())
                        .blockedTime(target.getBlockedTime())
                        .build();

                memberRepository.saveAndFlush(saved);
                Member member = memberRepository.findById(targetId).get();
                //then
                assertAll("member update assert",
                        () -> assertThat(member.getName()).isEqualTo(target.getName()),
                        () -> assertThat(member.getEmail()).isEqualTo(changeEmail),
                        () -> assertThat(member.getId()).isEqualTo(target.getId()),
                        () -> assertThat(member.getImg()).isEqualTo(target.getImg()),
                        () -> assertThat(member.getPassword()).isEqualTo(target.getPassword()),
                        () -> assertThat(member.getUpdatedAt()).isNotEqualTo(beforeUpdate));
            }

            @Test
            @DisplayName("비밀번호 변경 테스트")
            public void passwordUpdateTest() {
                //given
                String targetId = memberList.get(0).getId();
                Member target = memberRepository.findById(targetId).get();
                LocalDateTime beforeUpdate = target.getUpdatedAt();
                String changePassword = "changeEmail@notExist.com";


                //when
                Member saved = Member.builder()
                        .name(target.getName())
                        .id(target.getId())
                        .password(changePassword)
                        .email(target.getEmail())
                        .roles(target.getRoles())
                        .tryCount(target.getTryCount())
                        .blockedTime(target.getBlockedTime())
                        .build();

                memberRepository.saveAndFlush(saved);
                Member member = memberRepository.findById(targetId).get();
                //then
                assertAll("member update assert",
                        () -> assertThat(member.getName()).isEqualTo(target.getName()),
                        () -> assertThat(member.getEmail()).isEqualTo(target.getEmail()),
                        () -> assertThat(member.getId()).isEqualTo(target.getId()),
                        () -> assertThat(member.getImg()).isEqualTo(target.getImg()),
                        () -> assertThat(member.getPassword()).isEqualTo(changePassword),
                        () -> assertThat(member.getUpdatedAt()).isNotEqualTo(beforeUpdate));
            }

            @Test
            @DisplayName("tryCount 변경 테스트")
            public void tryCountUpdateTest() {
                //given
                String targetId = memberList.get(0).getId();
                Member target = memberRepository.findById(targetId).get();
                LocalDateTime beforeUpdate = target.getUpdatedAt();
                int beforeTryCount = target.getTryCount();
                //when

                Member saved = Member.builder()
                        .name(target.getName())
                        .id(target.getId())
                        .password(target.getPassword())
                        .email(target.getEmail())
                        .roles(target.getRoles())
                        .tryCount(target.getTryCount() + 1)
                        .blockedTime(target.getBlockedTime())
                        .build();

                memberRepository.saveAndFlush(saved);
                Member member = memberRepository.findById(targetId).get();

                //then
                assertAll("member update assert",
                        () -> assertThat(member.getName()).isEqualTo(target.getName()),
                        () -> assertThat(member.getEmail()).isEqualTo(target.getEmail()),
                        () -> assertThat(member.getId()).isEqualTo(target.getId()),
                        () -> assertThat(member.getImg()).isEqualTo(target.getImg()),
                        () -> assertThat(member.getPassword()).isEqualTo(target.getPassword()),
                        () -> assertThat(member.getTryCount()).isEqualTo(beforeTryCount + 1),
                        () -> assertThat(member.getUpdatedAt()).isNotEqualTo(beforeUpdate));
            }

            @Test
            @DisplayName("blockedTime 업데이트")
            public void blockedTimeUpdateTest() {
                //given
                String targetId = memberList.get(0).getId();
                Member target = memberRepository.findById(targetId).get();
                LocalDateTime beforeUpdate = target.getUpdatedAt();


                //when
                LocalDateTime nowDateTime = LocalDateTime.now();
                Member saved = Member.builder()
                        .name(target.getName())
                        .id(target.getId())
                        .password(target.getPassword())
                        .email(target.getEmail())
                        .roles(target.getRoles())
                        .tryCount(target.getTryCount())
                        .blockedTime(nowDateTime.plusSeconds(60 * 3))
                        .build();

                memberRepository.saveAndFlush(saved);
                Member member = memberRepository.findById(targetId).get();

                //then
                assertAll("member update assert",
                        () -> assertThat(member.getName()).isEqualTo(target.getName()),
                        () -> assertThat(member.getEmail()).isEqualTo(target.getEmail()),
                        () -> assertThat(member.getId()).isEqualTo(target.getId()),
                        () -> assertThat(member.getImg()).isEqualTo(target.getImg()),
                        () -> assertThat(member.getPassword()).isEqualTo(target.getPassword()),
                        () -> assertThat(member.getTryCount()).isEqualTo(target.getTryCount()),
                        () -> assertThat(Duration.between(nowDateTime, member.getBlockedTime()).getSeconds()).isEqualTo(60 * 3),
                        () -> assertThat(member.getUpdatedAt()).isNotEqualTo(beforeUpdate));
            }
        }

        @Nested
        @DisplayName("fail")
        public class Fail {
            @Test
            @DisplayName("변경 이메일 중복")
            public void emailDuplicateTest() {
                Member target = memberList.get(0);
                String changeEmail = memberList.get(1).getEmail();//중복되는 이메일 가져오기

                Member save = Member.builder()
                        .id(target.getId())
                        .name(target.getName())
                        .email(changeEmail)
                        .password(target.getPassword())
                        .img(target.getImg())
                        .tryCount(target.getTryCount())
                        .blockedTime(target.getBlockedTime())
                        .roles(target.getRoles())
                        .build();

                assertThrows(DataIntegrityViolationException.class, () -> {
                    memberRepository.saveAndFlush(save);
                });
            }
        }
    }

    /**
     * findByEmail
     * - 성공테스트
     *      - 이메일 찾기 성공 v
     * - 실패테스트
     *      - 없는 이메일 조회시 Optional.ofNullable(null) 반환 v
     */
    @Nested
    @DisplayName("findByEmail")
    public class findByEmailTest {
        @Nested
        @DisplayName("success")
        public class Success{
            @Test
            @DisplayName("이메일 찾기 성공")
            public void success(){
                Member target = memberList.get(0);
                String targetEmail = target.getEmail();

                Optional<Member> result = memberRepository.findByEmail(targetEmail);

                assertThat(result).isNotEmpty();

                Member member = result.get();

                assertAll("findByEmail",
                        ()->assertThat(member.getEmail()).isEqualTo(target.getEmail()),
                        ()->assertThat(member.getId()).isEqualTo(target.getId()),
                        ()->assertThat(member.getPassword()).isEqualTo(target.getPassword()),
                        ()->assertThat(member.getName()).isEqualTo(target.getName()),
                        ()->assertThat(member.getTryCount()).isEqualTo(target.getTryCount()),
                        ()->assertThat(member.getBlockedTime()).isEqualTo(target.getBlockedTime())
                );
            }
        }
        @Nested
        @DisplayName("fail")
        public class Fail{
            @Test
            @DisplayName("없는 이메일 조회시 Optional.ofNullable(null) 반환")
            public void notExistEmailTest(){
                String notExistEmail = "notExistUser@gmail.com";

                Optional<Member> result = memberRepository.findByEmail(notExistEmail);

                assertThat(result).isEmpty();
            }
        }
    }
}