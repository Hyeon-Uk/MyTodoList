package com.hyeonuk.todo.member.service;

import com.hyeonuk.todo.email.dto.EmailAuthCheckDTO;
import com.hyeonuk.todo.email.dto.EmailAuthDTO;
import com.hyeonuk.todo.email.exception.EmailAuthException;
import com.hyeonuk.todo.email.service.EmailAuthService;
import com.hyeonuk.todo.integ.exception.AlreadyExistException;
import com.hyeonuk.todo.member.entity.Authority;
import com.hyeonuk.todo.member.exception.UserInfoNotFoundException;
import com.hyeonuk.todo.integ.exception.ValidationException;
import com.hyeonuk.todo.security.service.JwtProvider;
import com.hyeonuk.todo.member.dto.LoginDTO;
import com.hyeonuk.todo.member.dto.SaveDTO;
import com.hyeonuk.todo.member.entity.Member;
import com.hyeonuk.todo.member.exception.LoginException;
import com.hyeonuk.todo.member.exception.SaveException;
import com.hyeonuk.todo.member.repository.MemberRepository;
import com.hyeonuk.todo.todo.entity.Category;
import com.hyeonuk.todo.todo.repository.CategoryRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MemberAuthServiceTests {
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberAuthServiceImpl memberAuthService;

    @Mock
    private EmailAuthService emailAuthService;
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    private List<Member> memberList = new ArrayList<>();
    private Map<String,List<Category>> categoryList = new HashMap<>();
    private Map<String,String> emailAuthCode = new HashMap<>();//이메일 인증번호를 저장할 변수
    private String rightPassword = "Abcdefg123!";

    @BeforeEach
    public void mockSetting() throws EmailAuthException{
        //jwt provider
        //토큰은 id.roles.date 형식으로 이어붙인 임시토큰으로 발급
        lenient().when(jwtProvider.createToken(anyString(),any(List.class))).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            List<Authority> roles = invocation.getArgument(1,List.class);

            StringBuilder sb = new StringBuilder();
            sb.append(id).append('_')
                    .append(String.join("/",roles
                            .stream()
                            .map(i->i.getName())
                            .collect(Collectors.toList())))
                    .append('_')
                    .append(LocalDateTime.now().toString());
            String token =  sb.toString();
            return token;
        });

        //provider.getId()
        lenient().when(jwtProvider.getId(anyString())).thenAnswer(invocation -> {
            String token = invocation.getArgument(0);
            return token.split("_")[0];
        });


        //emailAuthService
        lenient().when(emailAuthService.emailAuthCheck(any(EmailAuthCheckDTO.Request.class))).thenAnswer(invocation -> {
            EmailAuthCheckDTO.Request request = invocation.getArgument(0,EmailAuthCheckDTO.Request.class);

            String code = request.getCode();
            String email = request.getEmail();
            return EmailAuthCheckDTO.Response.builder()
                    .result(code.equals(emailAuthCode.get(email)))
                    .build();
        });
        lenient().when(emailAuthService.emailAuthSend(any(EmailAuthDTO.Request.class))).thenAnswer(invocation -> {
            EmailAuthDTO.Request dto = invocation.getArgument(0,EmailAuthDTO.Request.class);
            String email = dto.getEmail();
            emailAuthCode.put(email,"randomCode");

            return EmailAuthDTO.Response.builder()
                    .result(true)
                    .build();
        });


        //memberRepository 셋팅
        lenient().when(memberRepository.findAll()).thenReturn(memberList);
        lenient().when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0,Member.class);

            memberList = memberList.stream().filter(mem -> !mem.getId().equals(member.getId())).collect(Collectors.toList());
            memberList.add(member);
            return member;
        });
        lenient().when(memberRepository.findById(anyString())).thenAnswer(invocation->{
            String id = invocation.getArgument(0);
            return memberList.stream()
                    .filter(mem->mem.getId().equals(id))
                    .findAny();
        });
        lenient().when(memberRepository.findByEmail(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);

            return memberList.stream()
                    .filter(mem->mem.getEmail().equals(email))
                    .findAny();
        });

        //categoryRepository 셋팅
        lenient().doAnswer(invocation -> {
            Category category = invocation.getArgument(0,Category.class);
            Member member = category.getMember();

            if(categoryList.get(member.getId()) == null) categoryList.put(member.getId(),new ArrayList<>());

            categoryList.get(member.getId()).add(category);
            return null;
        }).when(categoryRepository).save(any(Category.class));

        //passwordEncoder.encode와 match를 설정해줌.
        //passwordEncoder.encode = 임시방편으로 입력값의 역순으로 암호화해줌
        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(invocation->{
            String input = invocation.getArgument(0);
            StringBuilder sb = new StringBuilder(input);
            return sb.reverse().toString();
        });

        //passwordEncoder.matches
        lenient().when(passwordEncoder.matches(anyString(),anyString())).thenAnswer(invocation->{
            String raw = invocation.getArgument(0);
            String encoded = invocation.getArgument(1);
            StringBuilder sb = new StringBuilder(raw);

            return  sb.reverse().toString().equals(encoded);
        });
    }
    @BeforeEach
    public void init(){
        IntStream.rangeClosed(1,1000).forEach(i -> {
            Member member = Member.builder()
                    .id("Tester".concat(Integer.toString(i)))
                    .email("tester".concat(Integer.toString(i)).concat("@gmail.com"))
                    .name("tester".concat(Integer.toString(i)))
                    .password(passwordEncoder.encode(rightPassword))
                    .build();
            memberList.add(member);
        });
    }

    /**
     * 성공 케이스
     * - 로그인 성공 케이스 v
     * - 아이디 끝에 공백이 들어간 경우 -> 성공으로 간주 v
     * 실패 케이스
     * - 아이디 공백 v
     * - 아이디 null v
     * - 비밀번호 공백 v
     * - 비밀번호 null v
     * - 존재하지 않는 아이디 테스트 v
     * - 비밀번호 불일치 테스트 V
     * - 로그인 시도 3회시 block, block시간이 지난 후 로그인 가능 v
     */
    @Nested
    @DisplayName("login")
    public class Login {
        Member dummy;

        @BeforeEach
        public void setDummy() {
            this.dummy = memberList.get(0);
        }

        @Nested
        @DisplayName("success")
        public class Success {
            @Test
            @DisplayName("로그인 성공 케이스")
            public void loginSuccessTest() throws ValidationException, UserInfoNotFoundException, LoginException {
                LoginDTO.Request request = LoginDTO.Request.builder()
                        .id(dummy.getId())
                        .password(rightPassword)
                        .build();

                LoginDTO.Response user = memberAuthService.login(request);

                assertAll("login",
                        () -> assertThat(user.getAccessToken()).isNotNull(),
                        () -> assertThat(jwtProvider.getId(user.getAccessToken())).isEqualTo(dummy.getId()));
            }

            @Test
            @DisplayName("아이디 끝에 공백이 들어간 경우 -> 성공으로 간주")
            public void loginWithSpaceTest() throws Exception {
                LoginDTO.Request request = new LoginDTO.Request(dummy.getId().concat(" "),rightPassword);

                LoginDTO.Response user = memberAuthService.login(request);

                assertAll("login",
                        () -> assertThat(user.getAccessToken()).isNotNull(),
                        () -> assertThat(jwtProvider.getId(user.getAccessToken())).isEqualTo(dummy.getId()));
            }
        }

        @Nested
        @DisplayName("fail")
        public class Fail {
            @Test
            @DisplayName("아이디 공백")
            public void idIsBlankTest() {
                LoginDTO.Request request = LoginDTO.Request.builder()
                        .id("               ")//공백으로만 이루어짐
                        .password(rightPassword)
                        .build();

                String message = assertThrows(ValidationException.class, () -> {
                    LoginDTO.Response user = memberAuthService.login(request);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");
            }

            @Test
            @DisplayName("아이디 null")
            public void idIsNullTest() {
                //아이디가 null로 주어짐
                LoginDTO.Request request = LoginDTO.Request.builder()
                        .password(rightPassword)
                        .build();

                String message = assertThrows(ValidationException.class, () -> {
                    LoginDTO.Response user = memberAuthService.login(request);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");
            }

            @Test
            @DisplayName("비밀번호 공백")
            public void pwIsBlankTest() {
                LoginDTO.Request request = LoginDTO.Request.builder()
                        .id(dummy.getId())
                        .password("                 ")//비밀번호가 공백으로 주어짐
                        .build();

                String message = assertThrows(ValidationException.class, () -> {
                    LoginDTO.Response user = memberAuthService.login(request);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");
            }

            @Test
            @DisplayName("비밀번호 null")
            public void pwIsNullTest() {
                LoginDTO.Request request = LoginDTO.Request.builder()
                        .id(dummy.getId())
                        .build();

                String message = assertThrows(ValidationException.class, () -> {
                    LoginDTO.Response user = memberAuthService.login(request);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");
            }

            @Test
            @DisplayName("존재하지 않는 아이디 테스트")
            public void idNotExistTest() {
                LoginDTO.Request request = LoginDTO.Request.builder()
                        .id("notExistId")
                        .password("notExistPassword")
                        .build();

                String message = assertThrows(UserInfoNotFoundException.class, () -> {
                    LoginDTO.Response user = memberAuthService.login(request);
                }).getMessage();
                assertThat(message).isEqualTo("잘못된 인증정보 입니다.");
            }

            @Test
            @DisplayName("비밀번호 불일치 테스트")
            public void passwordNotMatchTest() {
                int beforeTryCount = dummy.getTryCount();
                LoginDTO.Request request = LoginDTO.Request.builder()
                        .id(dummy.getId())
                        .password("notExistPassword")
                        .build();

                String message = assertThrows(LoginException.class, () -> {
                    LoginDTO.Response user = memberAuthService.login(request);
                }).getMessage();
                assertThat(message).isEqualTo("잘못된 인증정보 입니다.");
                dummy = memberRepository.findById(dummy.getId()).get();
                assertThat(dummy.getTryCount()).isEqualTo(beforeTryCount + 1);
            }

            @Test
            @DisplayName("로그인 시도 3회시 block, block시간이 지난 후 로그인 가능")
            public void memberBlockTest() throws ValidationException, UserInfoNotFoundException, LoginException {
                int beforeTryCount = dummy.getTryCount();
                String rightPass = dummy.getPassword();
                LoginDTO.Request request = LoginDTO.Request.builder()
                        .id(dummy.getId())
                        .password("notExistPassword")
                        .build();

                for (int i = 1; i <= 3; i++) {
                    String message = assertThrows(LoginException.class, () -> {
                        LoginDTO.Response user = memberAuthService.login(request);
                    }).getMessage();
                    assertThat(message).isEqualTo("잘못된 인증정보 입니다.");
                    Member updated = memberRepository.findById(dummy.getId()).get();
                    if (i != 3) {
                        assertThat(updated.getTryCount()).isEqualTo(beforeTryCount + i);
                    } else {
                        assertThat(updated.getTryCount()).isEqualTo(0);
                        assertThat(updated.getBlockedTime()).isNotNull();
                    }
                }

                LoginDTO.Request rightRequest = LoginDTO.Request.builder()
                        .id(dummy.getId())
                        .password(rightPassword)
                        .build();

                //시간이 지나지 않아서 로그인 실패
                String message = assertThrows(LoginException.class, () -> {
                    memberAuthService.login(rightRequest);
                }).getMessage();
                assertThat(message.indexOf("까지 로그인이 불가능합니다.")).isNotEqualTo(-1);

                //시간이 지나면 로그인 가능.
                //해당 유저의 block시간을 조정함
                Member updated = Member.builder()
                        .id(dummy.getId())
                        .name(dummy.getName())
                        .email(dummy.getEmail())
                        .password(dummy.getPassword())
                        .tryCount(dummy.getTryCount())
                        .blockedTime(LocalDateTime.now())//3분전에 block된걸로 셋팅
                        .roles(dummy.getRoles())
                        .build();

                memberRepository.save(updated);

                Optional<Member> byId = memberRepository.findById(updated.getId());

                LoginDTO.Response response = memberAuthService.login(rightRequest);
                assertThat(response.getAccessToken()).isNotNull();
                assertThat(jwtProvider.getId(response.getAccessToken())).isEqualTo(dummy.getId());
            }
        }
    }

    /**
     * * 성공케이스 (DB에 저장 잘 됐는지, DB에 잘 암호화 됐는지 확인) V
     * * 1. 정상 요청 v
     * * 2. 이름이 같은 요청이라도 ok v
     * 실패 케이스
     * * 2. 아이디의 길이가 5 미만 v
     * * 3. 아이디의 길이가 20초과 v
     * * 4. 아이디의 조합 중 특수문자가 들어간 경우 v
     * * 5. 50자 초과의 이름을 입력한 경우 v
     * * 6. 이메일형식이 아닌것을 입력한 경우 v
     * * 7. 8자 미만의 비밀번호 입력 v
     * * 8. 16자 초과의 비밀번호 입력 v
     * * 9. 소문자가 들어가지 않은 비밀번호 입력 v
     * * 10. 대문자가 들어가지 않은 비밀번호 입력 v
     * * 11. 숫자가 들어가지 않은 비밀번호 입력 v
     * * 12. 특수문자가 들어가지 않은 비밀번호 입력
     * * 13. 약관에 동의하지 않은 경우 v
     * * 14. 비밀번호 확인이 일치하지 않는경우 v
     * * 15. 아이디가 공백이면서 길이 조건을 만족하는 경우 or null인 경우 v
     * * 16. 이름이 null or 공백이면서 길이조건을 만족하는 경우 v
     * * 17. 이메일이 null or 공백인 경우 v
     * * 18. 비밀번호가 null or 공백이면서 길이조건을 만족하는 경우 v
     * * 19. 비밀번호 확인이 null or 공백인 경우 v
     * * 20. 아이디 중복인 경우 v
     * * 21. 이메일이 중복인 경우 v
     * * 22. 이메일 인증 코드가 null인 경우 v
     * * 23. 이메일 인증 코드가 blank인 경우 v
     * * 24. 이메일 인증 코드가 일치하지 않는 경우
     */
    @Nested
    @DisplayName("regist")
    public class RegistTest {
        //정상적인 요청의 객체를 미리 생성해둠
        SaveDTO.Request req;
        int beforeSize ;//db에 저장된 Member의 수

        @BeforeEach
        public void initRequestDTO() throws EmailAuthException {
            emailAuthCode.put("notExistEmail123@gmail.com","authCode");
            req = SaveDTO.Request.builder()
                    .id("notExistId123")
                    .name("notExistName")
                    .email("notExistEmail123@gmail.com")
                    .password("Abcdefg123!")
                    .passwordCheck("Abcdefg123!")
                    .agree(true)
                    .emailAuthCode(emailAuthCode.get("notExistEmail123@gmail.com"))
                    .build();
        }

        @Nested
        @DisplayName("success")
        public class Success {
            @Test
            @DisplayName("1. 정상 요청")
            public void success() throws SaveException, ValidationException, AlreadyExistException {
                SaveDTO.Response save = memberAuthService.save(req);
                assertAll("save",
                        ()->assertThat(save.getId()).isEqualTo(req.getId()),
                        ()->assertThat(save.getEmail()).isEqualTo(req.getEmail()),
                        ()->assertThat(save.getName()).isEqualTo(req.getName()),
                        ()->assertThat(categoryList.get(save.getId()).size()).isEqualTo(1));
            }

            @Test
            @DisplayName("2. 이름이 같은 요청이라도 ok")
            public void sameNameSuccess() throws SaveException, ValidationException, AlreadyExistException {
                String duplicatedName = memberList.get(0).getName();
                req.setName(duplicatedName);//중복된 이메일로 설정. 나머지는 조건 다 맞게

                SaveDTO.Response save = memberAuthService.save(req);
                assertAll("save duplicatedName",
                        ()->assertThat(save.getId()).isEqualTo(req.getId()),
                        ()->assertThat(save.getEmail()).isEqualTo(req.getEmail()),
                        ()->assertThat(save.getName()).isEqualTo(req.getName()),
                        ()->assertThat(categoryList.get(save.getId()).size()).isEqualTo(1));
            }
        }

        @Nested
        @DisplayName("fail")
        public class Fail {
            @Test
            @DisplayName("2. 아이디의 길이가 5 미만 ")
            public void idLengthLessThen() {
                String wrongId = "ab12";//5미만의 아이디
                req.setId(wrongId);//아이디만 잘못된걸로 셋팅

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("아이디는 5~20자의 영문 대소문자, 숫자로 이루어져야 합니다.");
            }

            @Test
            @DisplayName("3. 아이디의 길이가 20초과")
            public void idLengthOverThen() {
                String wrongId = "abcdfqweasdfasdfabcdfqweasdfasdf123";
                req.setId(wrongId);

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("아이디는 5~20자의 영문 대소문자, 숫자로 이루어져야 합니다.");
            }

            @Test
            @DisplayName("4. 아이디의 조합 중 특수문자가 들어간 경우")
            public void idContainsSpecialChar() {
                String wrongId = "abcdefg123!";
                req.setId(wrongId);

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("아이디는 5~20자의 영문 대소문자, 숫자로 이루어져야 합니다.");
            }

            @Test
            @DisplayName("5. 50자 초과의 이름을 입력한 경우")
            public void nameLengthOverThen() {
                String wrongName = "abcdfqweasdfasdfabcdfqweasdfasdf123abcdfqweasdfasdfabcdfqweasdfasdf123abcdfqweasdfasdfabcdfqweasdfasdf123abcdfqweasdfasdfabcdfqweasdfasdf123";
                req.setName(wrongName);

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("이름은 50자 이하여야 합니다.");
            }

            @Test
            @DisplayName("6. 이메일형식이 아닌것을 입력한 경우")
            public void notEmailType() {
                //이메일 형식 x
                String wrongEmail = "ralgusdnarw123";
                req.setEmail(wrongEmail);

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("유효한 이메일 형식이 아닙니다.");


                //이메일 형식 x 다른타입
                wrongEmail = " @gmail.com";
                req.setEmail(wrongEmail);

                message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("유효한 이메일 형식이 아닙니다.");
            }

            @Test
            @DisplayName("7. 8자 미만의 비밀번호 입력")
            public void pwLengthLessThen() {
                String wrongPw = "Ab123!";
                req.setPassword(wrongPw);
                req.setPasswordCheck(wrongPw);

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("비밀번호는 8~16자의 소문자, 대문자, 숫자, 특수문자로 이루어져야 합니다.");
            }

            @Test
            @DisplayName("8. 16자 초과의 비밀번호 입력")
            public void pwLengthOverThen() {
                String wrongPw = "Abcdefasdfzx123!@";//17자
                req.setPassword(wrongPw);
                req.setPasswordCheck(wrongPw);

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("비밀번호는 8~16자의 소문자, 대문자, 숫자, 특수문자로 이루어져야 합니다.");
            }

            @Test
            @DisplayName("9. 소문자가 들어가지 않은 비밀번호 입력")
            public void pwNotContainsLowerChar() {
                String wrongPw = "ABCSAF123!";//소문자 없는 비밀번호
                req.setPassword(wrongPw);
                req.setPasswordCheck(wrongPw);

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("비밀번호는 8~16자의 소문자, 대문자, 숫자, 특수문자로 이루어져야 합니다.");
            }

            @Test
            @DisplayName("10. 대문자가 들어가지 않은 비밀번호 입력")
            public void pwNotContainsUpperChar() {
                String wrongPw = "abcsaqwe123!";//대문자 없는 비밀번호
                req.setPassword(wrongPw);
                req.setPasswordCheck(wrongPw);

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("비밀번호는 8~16자의 소문자, 대문자, 숫자, 특수문자로 이루어져야 합니다.");
            }

            @Test
            @DisplayName("11. 숫자가 들어가지 않은 비밀번호 입력")
            public void pwNotContainsNumber() {
                String wrongPw = "ABCSAFasfsd!";//숫자없는 비밀번호
                req.setPassword(wrongPw);
                req.setPasswordCheck(wrongPw);

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("비밀번호는 8~16자의 소문자, 대문자, 숫자, 특수문자로 이루어져야 합니다.");
            }

            @Test
            @DisplayName("12. 특수문자가 들어가지 않은 비밀번호 입력")
            public void pwNotContainsSpecialChar() {
                String wrongPw = "asfAF12312";//특수문자 없는 비밀번호
                req.setPassword(wrongPw);
                req.setPasswordCheck(wrongPw);

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("비밀번호는 8~16자의 소문자, 대문자, 숫자, 특수문자로 이루어져야 합니다.");
            }

            @Test
            @DisplayName("13. 약관에 동의하지 않은 경우")
            public void notAgree() {
                req.setAgree(false);
                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("약관에 동의해주세요");

            }

            @Test
            @DisplayName("14. 비밀번호 확인이 일치하지 않는경우")
            public void pwNotMatches() {
                req.setPasswordCheck(req.getPassword().concat("wrong"));

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("비밀번호가 일치하지 않습니다.");
            }

            @Test
            @DisplayName("15. 아이디가 공백이면서 길이 조건을 만족하는 경우 or null인 경우")
            public void idIsBlankOrNull() {
                req.setId("            ");//공백인경우

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");

                req.setId(null);//null인 경우

                message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");
            }

            @Test
            @DisplayName("16. 이름이 null or 공백이면서 길이조건을 만족하는 경우")
            public void nameIsBlankOrNull() {
                req.setName("            ");//공백인경우

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");

                req.setName(null);//null인 경우

                message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");
            }

            @Test
            @DisplayName("17. 이메일이 null or 공백인 경우")
            public void emailIsNullOrBlank() {
                req.setEmail("            ");//공백인경우

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");

                req.setEmail(null);//null인 경우

                message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");
            }

            @Test
            @DisplayName("18. 비밀번호가 null or 공백이면서 길이조건을 만족하는 경우")
            public void pwIsNullOrBlank() {
                req.setPassword("             ");//공백인경우

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");

                req.setPassword(null);//null인 경우

                message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");
            }

            @Test
            @DisplayName("19. 비밀번호 확인이 null or 공백인 경우")
            public void pwCheckIsNullOrBlank() {
                req.setPasswordCheck("            ");//공백인경우

                String message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");

                req.setPasswordCheck(null);//null인 경우

                message = assertThrows(ValidationException.class, () -> {
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");
            }

            @Test
            @DisplayName("20. 아이디 중복인 경우")
            public void idDuplicated() {
                String duplicatedId = memberList.get(0).getId();

                req.setId(duplicatedId);
                String message = assertThrows(AlreadyExistException.class,()->{
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("존재하는 아이디입니다.");
            }

            @Test
            @DisplayName("21. 이메일이 중복인 경우")
            public void emailDuplicated() {
                String duplicatedEmail = memberList.get(0).getEmail();

                req.setEmail(duplicatedEmail);
                String message = assertThrows(AlreadyExistException.class,()->{
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("존재하는 이메일입니다.");
            }

            @Test
            @DisplayName("22. 이메일 인증 코드가 null인 경우")
            public void emailAuthCodeNullTest() {
                req.setEmailAuthCode(null);//이메일 인증 코드를 null로 셋팅
                String message = assertThrows(ValidationException.class,()->{
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");
            }

            @Test
            @DisplayName("23. 이메일 인증 코드가 blank인 경우")
            public void emailAuthCodeBlankTest() {
                req.setEmailAuthCode("        ");//이메일 인증 코드를 blank로 설정
                String message = assertThrows(ValidationException.class,()->{
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("입력값을 다시 확인해주세요");
            }

            @Test
            @DisplayName("24. 이메일 인증 코드가 일치하지 않는 경우")
            public void emailAuthCodeNotMatchTest() {
                req.setEmailAuthCode("NotMatchCode");//이메일 인증 코드가 일치하지 않는 경우
                String message = assertThrows(ValidationException.class,()->{
                    memberAuthService.save(req);
                }).getMessage();
                assertThat(message).isEqualTo("인증코드가 일치하지 않습니다.");
            }
        }
    }
}
