package com.hyeonuk.todo.member.repository;

import com.hyeonuk.todo.integ.data.MEMBER_MAX_LENGTH;
import com.hyeonuk.todo.member.entity.Member;
import com.hyeonuk.todo.member.service.MemberAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * 체크해야할것
 * save
 *  - 성공테스트 v
 *  - 실패 테스트
 *      - 이메일 중복 검사 v
 *      - 아이디 null v
 *      - 비밀번호 null v
 *      - 이름 null v
 *      - 아이디 길이 초과 v
 *      - 이메일 길이 초과 v
 *      - 비밀번호 길이 초과 v
 *      - 이미지 길이 초과 v
 *      - 설명 길이 초과 v
 *
 * update
 *  - 성공테스트
 *      - tryCount 업데이트
 *      - blockedTime 업데이트
 *      - 업데이트 후 updated_at 체크
 *  - 실패테스트
 *    - 변경 이메일 중복
 *
 * findByEmail
 *  - 성공테스트
 *  - 존재하지 않는 테스트
 */

@DataJpaTest
class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    private List<Member> memberList = new ArrayList<>();


    @BeforeEach
    public void insertDummies(){
        IntStream.rangeClosed(1,200).forEach(i->{
            Member member = Member.builder()
                    .id("Tester".concat(Integer.toString(i)))
                    .email("tester".concat(Integer.toString(i)).concat("@gmail.com"))
                    .name("tester".concat(Integer.toString(i)))
                    .password("1111")
                    .build();
            memberList.add(member);
        });

        memberRepository.saveAll(memberList);
    }

    @Nested
    @DisplayName("save test")//세이브를 테스트할 클래스
    public class SaveTest{
        @Nested
        @DisplayName("success")
        public class Success{
            @Test
            @DisplayName("success")
            public void saveSuccess(){
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
                assertThat(afterCount-beforeCount).isEqualTo(1);

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
        public class Fail{
            @Test
            @DisplayName("이메일 중복")
            public void duplicatedId(){
                //given
                String alreadyUserEmail = memberList.get(0).getEmail();
                Member duplicatedMember = Member.builder()
                        .id("notExistUserId")
                        .email(alreadyUserEmail)
                        .password("1111")
                        .name("hyeonuk")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                   memberRepository.saveAndFlush(duplicatedMember);
                });
            }

            @Test
            @DisplayName("id null")
            public void idNull(){
                //given
                Member member = Member.builder()
                        .email("email@gmail.com")
                        .name("name")
                        .password("1111")
                        .build();

                //when & then
                assertThrows(JpaSystemException.class,()->{
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("email null")
            public void emailNull(){
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .name("name")
                        .password("1111")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("password null")
            public void pwNull(){
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email("email@gmail.com")
                        .name("name")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("name null")
            public void nameNull(){
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email("email@gmail.com")
                        .password("1111")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                    memberRepository.saveAndFlush(member);
                });
            }

            //해당 길이만큼의 더미 스트링을 반환해주는 메서드
            private String makeLengthString(int length){
                StringBuilder sb = new StringBuilder();
                for(int i=0;i<length;i++){
                    sb.append('1');
                }
                return sb.toString();
            }

            @Test
            @DisplayName("아이디 길이 초과")
            public void longId(){
                //given
                Member member = Member.builder()
                        .id(makeLengthString(MEMBER_MAX_LENGTH.ID.getValue()+1))
                        .email("email@gmail.com")
                        .password("1111")
                        .name("hyeonuk")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                   memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("이메일 길이 초과")
            public void longEmail(){
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email(makeLengthString(MEMBER_MAX_LENGTH.EMAIL.getValue()+1))
                        .password("1111")
                        .name("hyeonuk")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("비밀번호 길이 초과")
            public void longPassword(){
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email("email@gmail.com")
                        .password(makeLengthString(MEMBER_MAX_LENGTH.PASSWORD.getValue()+1))
                        .name("hyeonuk")
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("이름 길이 초과")
            public void longName(){
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email("email@gmail.com")
                        .password("1111")
                        .name(makeLengthString(MEMBER_MAX_LENGTH.NAME.getValue())+1)
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("이미지 길이 초과")
            public void longImg(){
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email("email@gmail.com")
                        .password("1111")
                        .name("hyeonuk")
                        .img(makeLengthString(MEMBER_MAX_LENGTH.IMG.getValue()+1))
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                    memberRepository.saveAndFlush(member);
                });
            }

            @Test
            @DisplayName("설명 길이 초과")
            public void longDesc(){
                //given
                Member member = Member.builder()
                        .id("notExistUserId")
                        .email("email@gmail.com")
                        .password("1111")
                        .name("hyeonuk")
                        .description(makeLengthString(MEMBER_MAX_LENGTH.DESC.getValue()+1))
                        .build();

                //when & then
                assertThrows(DataIntegrityViolationException.class,()->{
                    memberRepository.saveAndFlush(member);
                });
            }
        }
    }

}