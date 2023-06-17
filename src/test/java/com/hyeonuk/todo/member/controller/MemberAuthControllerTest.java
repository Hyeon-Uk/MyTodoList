package com.hyeonuk.todo.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyeonuk.todo.integ.data.MEMBER_MAX_LENGTH;
import com.hyeonuk.todo.member.dto.SaveDTO;
import com.hyeonuk.todo.member.repository.MemberRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberAuthControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper mapper;


    /**
     * TODO
     * -----------------
     *      성공케이스 (DB에 저장 잘 됐는지, DB에 잘 암호화 됐는지 확인) V
     *      * 1. 정상 요청 v
     *      * 2. 이름이 같은 요청이라도 ok v
     *
     *
     *      실패 케이스
     *      * 2. 아이디의 길이가 5 미만 v
     *      * 3. 아이디의 길이가 20초과 v
     *      * 4. 아이디의 조합 중 특수문자가 들어간 경우 v
     *      * 5. 50자 초과의 이름을 입력한 경우 v
     *      * 6. 이메일형식이 아닌것을 입력한 경우 v
     *      * 7. 8자 미만의 비밀번호 입력 v
     *      * 8. 16자 초과의 비밀번호 입력 v
     *      * 9. 소문자가 들어가지 않은 비밀번호 입력 v
     *      * 10. 대문자가 들어가지 않은 비밀번호 입력 v
     *      * 11. 숫자가 들어가지 않은 비밀번호 입력 v
     *      * 12. 특수문자가 들어가지 않은 비밀번호 입력
     *      * 13. 약관에 동의하지 않은 경우 v
     *      * 14. 비밀번호 확인이 일치하지 않는경우 v
     *      * 15. 아이디가 공백이면서 길이 조건을 만족하는 경우 or null인 경우 v
     *      * 16. 이름이 null or 공백이면서 길이조건을 만족하는 경우 v
     *      * 17. 이메일이 null or 공백인 경우 v
     *      * 18. 비밀번호가 null or 공백이면서 길이조건을 만족하는 경우 v
     *      * 19. 비밀번호 확인이 null or 공백인 경우 v
     *      * 20. 아이디 중복인 경우 v
     *      * 21. 이메일이 중복인 경우 v
     * */
    @Nested
    @DisplayName("save test")
    public class SaveTest{
        //아이디 : 5~20자의 영문 소문자, 대문자, 숫자만 사용이 가능합니다.
        //이름 : 50자 이하의 이름을 입력해야함
        //이메일 : 이메일 형식
        //비밀번호 : 8~16자의 소문자, 대문자, 숫자, 특수문자로 구성되어야 합니다.
        //비밀번호 확인 : 위에 입력한 비밀번호와 일치해야함
        String registRequestURL = "/auth/regist";
        @Nested
        @DisplayName("success Test")
        class Success{
            @Test
            @DisplayName("1. 정상 요청")
            public void saveSuccessTest() throws Exception {
                //given
                int beforeMemberSize = memberRepository.findAll().size();

                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();

                String stringify = mapper.writeValueAsString(request);


                //1. Response 기댓값 검증
                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id",is(request.getId())))
                        .andExpect(jsonPath("$.email",is(request.getEmail())))
                        .andExpect(jsonPath("$.name",is(request.getName())));

                //2. DB에 저장됐는지 검증
                assertThat(memberRepository.findAll().size()).isEqualTo(beforeMemberSize+1);

                //3. DB에 암호화가 잘 됐는지 검증
                assertThat(memberRepository.findById(request.getId()).get().getPassword()).isNotEqualTo(request.getPassword());
            }

            @Test
            @DisplayName("2. 이름이 같은 요청이라도 ok")
            public void sameNameOkTest() throws Exception{
                //given
                String sameName = "sameName";
                SaveDTO.Request already = SaveDTO.Request.builder()
                        .id("tester1")
                        .email("test1@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .name(sameName)
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(already);
                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))
                        .andExpect(status().isCreated());

                //when
                SaveDTO.Request sameNameUser = SaveDTO.Request.builder()
                        .id("tester2")
                        .email("tester2@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .name(sameName)
                        .agree(true)
                        .build();
                stringify = mapper.writeValueAsString(sameNameUser);

                //when & then
                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id",is(sameNameUser.getId())))
                        .andExpect(jsonPath("$.email",is(sameNameUser.getEmail())))
                        .andExpect(jsonPath("$.name",is(sameNameUser.getName())));
            }
        }


        @DisplayName("failure test")
        @Nested
        class Failure{
            int beforeSize;
            int baseSize;//각 테스트마다 기본 셋팅할 때 추가한 인원
            @BeforeEach
            public void setBeforeSize(){
                this.beforeSize = memberRepository.findAll().size();
            }
            @AfterEach
            public void notSaveInDB(){
                assertThat(memberRepository.findAll().size()).isEqualTo(this.beforeSize+this.baseSize);
            }


            @Test
            @DisplayName("2. 아이디의 길이가 5 미만")
            public void saveIdLengthLessTest() throws Exception {
                //given
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("tes")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);



                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("아이디는 5~20자의 영문 대소문자, 숫자로 이루어져야 합니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("3. 아이디의 길이가 20초과")
            public void saveIdLengthOverTest() throws Exception {
                //given
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("testtesttesttesttesttest")//20자 초과
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);



                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("아이디는 5~20자의 영문 대소문자, 숫자로 이루어져야 합니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));;
            }

            @Test
            @DisplayName("4. 아이디의 조합 중 특수문자가 들어간 경우")
            public void saveIdIncludeSpecialCharacter() throws Exception {
                //given
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test123!")//특수문자 입력
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);



                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("아이디는 5~20자의 영문 대소문자, 숫자로 이루어져야 합니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));;
            }

            @Test
            @DisplayName("5. 50자 초과의 이름을 입력한 경우")
            public void saveNameLengthOverTest() throws Exception {
                //given
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test123")
                        .name("KimHyeonukKimHyeonukKimHyeonukKimHyeonukKimHyeonukKimHyeonukKimHyeonukKimHyeonukKimHyeonukKimHyeonukKimHyeonukKimHyeonuk")//50자 이상 입력
                        .email("rlagusdnr120@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);



                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("이름은 50자 이하여야 합니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("6. 이메일형식이 아닌것을 입력한 경우")
            public void saveNotEmailTypeTest() throws Exception {
                //given
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120")//이메일 타입이 아님
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("유효한 이메일 형식이 아닙니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("7. 8자 미만의 비밀번호 입력")
            public void savePasswordLengthLessTest() throws Exception {
                //given
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("Ab123!")//비밀번호가 8자 미만
                        .passwordCheck("Ab123!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("비밀번호는 8~16자의 소문자, 대문자, 숫자, 특수문자로 이루어져야 합니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("8. 16자 초과의 비밀번호 입력")
            public void savePasswordLengthOverTest() throws Exception {
                //given
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("Ab12fasdfasdfsafsdfasfasdfsafasdfsdfasdffasdfasdfa3!")//비밀번호가 16자 초과
                        .passwordCheck("Ab12fasdfasdfsafsdfasfasdfsafasdfsdfasdffasdfasdfa3!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("비밀번호는 8~16자의 소문자, 대문자, 숫자, 특수문자로 이루어져야 합니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("9. 소문자가 들어가지 않은 비밀번호 입력")
            public void savePasswordNotIncludeSmallLetterTest() throws Exception {
                //given
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("ABCVDSA123!")//소문자 입력x
                        .passwordCheck("ABCVDSA123!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("비밀번호는 8~16자의 소문자, 대문자, 숫자, 특수문자로 이루어져야 합니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("10. 대문자가 들어가지 않은 비밀번호 입력")
            public void savePasswordNotIncludeBigLetterTest() throws Exception {
                //given
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("abcdwasdf123!")//대문자 입력x
                        .passwordCheck("abcdwasdf123!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("비밀번호는 8~16자의 소문자, 대문자, 숫자, 특수문자로 이루어져야 합니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("11. 숫자가 들어가지 않은 비밀번호 입력")
            public void savePasswordNotIncludeNumberTest() throws Exception {
                //given
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("abcdwasABAD!")//숫자 입력x
                        .passwordCheck("abcdwasABAD!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("비밀번호는 8~16자의 소문자, 대문자, 숫자, 특수문자로 이루어져야 합니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("12. 특수문자가 들어가지 않은 비밀번호 입력")
            public void savePasswordNotIncludeSpecialCharTest() throws Exception {
                //given
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("abcdwasABAD12")//특수문자 입력 x
                        .passwordCheck("abcdwasABAD12")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("비밀번호는 8~16자의 소문자, 대문자, 숫자, 특수문자로 이루어져야 합니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("13. 약관에 동의하지 않은 경우")
            public void isNotAgreeTest() throws Exception {
                //given
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .agree(false)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("약관에 동의해주세요")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("14. 비밀번호 확인이 일치하지 않는경우")
            public void passwordNotMatchException() throws Exception {
                //given
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("!321gfedcbA")//비밀번호 확인이 일치하지 않음
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("비밀번호가 일치하지 않습니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("15. 아이디가 공백이면서 길이 조건을 만족하는 경우 or null인 경우")
            public void idIsNullOrBlankExceptionTest() throws Exception{
                //given
                //id가 null인 경우
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("입력값을 다시 확인해주세요")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));

                //아이디가 공백이면서 길이 조건을 만족하는 경우
                request = SaveDTO.Request.builder()
                        .id("            ")//공백이지만 길이를 만족함
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();
                stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("입력값을 다시 확인해주세요")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("16. 이름이 null or 공백이면서 길이조건을 만족하는 경우")
            public void nameIsNullOrBlankExceptionTest() throws Exception{
                //given
                //name이 null인 경우
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .email("rlagusdnr120@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("입력값을 다시 확인해주세요")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));

                //name이 공백이면서 길이 조건을 만족하는 경우
                request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("        ")
                        .email("rlagusdnr120@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();
                stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("입력값을 다시 확인해주세요")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("17. 이메일이 null or 공백인 경우")
            public void emailIsNullOrBlankExceptionTest() throws Exception{
                //given
                //email이 null인 경우
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("입력값을 다시 확인해주세요")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));

                //email이 공백이면서 길이 조건을 만족하는 경우
                request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("                ")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();
                stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("입력값을 다시 확인해주세요")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("18. 비밀번호가 null or 공백이면서 길이조건을 만족하는 경우")
            public void pwIsNullOrBlankExceptionTest() throws Exception{
                //given
                //pw가 null인 경우
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("입력값을 다시 확인해주세요")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));

                //pw가 공백이면서 길이 조건을 만족하는 경우
                request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("            ")
                        .passwordCheck("Abcdefg123!")
                        .agree(true)
                        .build();
                stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("입력값을 다시 확인해주세요")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("19. 비밀번호 확인이 null or 공백인 경우")
            public void pwCheckIsNullOrBlankExceptionTest() throws Exception{
                //given
                //pwCheck가 null인 경우
                SaveDTO.Request request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("Abcdefg123!")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("입력값을 다시 확인해주세요")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));

                //pwCheck가 공백이면서 길이 조건을 만족하는 경우
                request = SaveDTO.Request.builder()
                        .id("test1")
                        .name("KimHyeonuk")
                        .email("rlagusdnr120@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("                 ")
                        .agree(true)
                        .build();
                stringify = mapper.writeValueAsString(request);


                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))//when
                        //then
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("입력값을 다시 확인해주세요")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("20. 아이디 중복인 경우")
            public void idDuplicateExceptionTest() throws Exception{
                //given
                String sameId = "sameId123";
                SaveDTO.Request already = SaveDTO.Request.builder()
                        .id(sameId)
                        .email("test1@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .name("tester1")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(already);
                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))
                        .andExpect(status().isCreated());
                this.baseSize = 1;

                //when
                SaveDTO.Request duplicatedUser = SaveDTO.Request.builder()
                        .id(sameId)
                        .email("another@gmail.com")
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .name("tester2")
                        .agree(true)
                        .build();
                stringify = mapper.writeValueAsString(duplicatedUser);

                //when & then
                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("존재하는 아이디입니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }

            @Test
            @DisplayName("21. 이메일이 중복인 경우")
            public void emailDuplicateExceptionTest() throws Exception{
                //given
                String sameEmail = "sameId123@gmail.com";
                SaveDTO.Request already = SaveDTO.Request.builder()
                        .id("tester1")
                        .email(sameEmail)
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .name("tester1")
                        .agree(true)
                        .build();
                String stringify = mapper.writeValueAsString(already);
                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))
                        .andExpect(status().isCreated());
                this.baseSize = 1;

                //when
                SaveDTO.Request duplicatedUser = SaveDTO.Request.builder()
                        .id("tester2")
                        .email(sameEmail)
                        .password("Abcdefg123!")
                        .passwordCheck("Abcdefg123!")
                        .name("tester2")
                        .agree(true)
                        .build();
                stringify = mapper.writeValueAsString(duplicatedUser);

                //when & then
                mvc.perform(post(registRequestURL).content(stringify).contentType("application/json;charset=utf-8"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",is("존재하는 이메일입니다.")))
                        .andExpect(jsonPath("$.status",is(HttpStatus.BAD_REQUEST.value())));
            }
        }
    }
}
