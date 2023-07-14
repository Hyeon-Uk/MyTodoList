package com.hyeonuk.todo.email.service;

import com.hyeonuk.todo.email.dto.EmailAuthCheckDTO;
import com.hyeonuk.todo.email.dto.EmailAuthDTO;
import com.hyeonuk.todo.email.dto.EmailSendDTO;
import com.hyeonuk.todo.email.entity.EmailAuthentication;
import com.hyeonuk.todo.email.exception.EmailAuthException;
import com.hyeonuk.todo.email.exception.EmailSendException;
import com.hyeonuk.todo.email.repository.EmailAuthenticationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailAuthServiceImplTest {
    @InjectMocks
    private EmailAuthServiceImpl emailAuthService;

    @Mock
    private EmailService emailService;

    private List<EmailSendDTO.Request> emailSendList = new ArrayList<>();

    @Mock
    private EmailAuthenticationRepository emailAuthenticationRepository;

    private List<EmailAuthentication> emailAuthenticationList = new ArrayList<>();

    @BeforeEach
    public void mockInit() throws EmailSendException, EmailAuthException {
        //emailService 관련 메서드 정의
        lenient().doAnswer(invocation -> {
            EmailSendDTO.Request request = invocation.getArgument(0);
            emailSendList.add(request);
            return null;
        }).when(emailService).sendEmail(any(EmailSendDTO.Request.class));

        //emailAuthenticationRepository관련 메서드 정의
        lenient().when(emailAuthenticationRepository.save(any(EmailAuthentication.class))).thenAnswer(invocation -> {
            EmailAuthentication emailAuthentication = invocation.getArgument(0, EmailAuthentication.class);

            emailAuthenticationList = emailAuthenticationList.stream()
                    .filter(auth -> !auth.getEmail().equals(emailAuthentication.getEmail()))
                    .collect(Collectors.toList());

            emailAuthenticationList.add(emailAuthentication);
            return emailAuthentication;
        });

        lenient().when(emailAuthenticationRepository.findById(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0, String.class);
            return emailAuthenticationList.stream()
                    .filter(auth -> auth.getEmail().equals(email))
                    .findFirst();
        });

        lenient().doAnswer(invocation -> {
            String id = invocation.getArgument(0);

            emailAuthenticationList = emailAuthenticationList.stream().filter(auth -> !auth.getEmail().equals(id))
                    .collect(Collectors.toList());

            return null;
        }).when(emailAuthenticationRepository).deleteById(anyString());
    }

    /**
     * emailAuthSend 테스트
     * 성공 케이스
     * - 1. 이메일로 인증코드가 잘 들어간 뒤, 인증코드가 redis 서버에 잘 저장됨
     * 실패 케이스
     * - 1. 이메일 전송 중 오류가 발생하면 redis서버에 저장된 인증코드가 사라져야함
     */
    @Nested
    @DisplayName("emailAuthSend")
    public class EmailAuthSendTest {
        private EmailAuthDTO.Request req;
        private int beforeEmailSendSize;

        @BeforeEach
        public void init() {
            req = EmailAuthDTO.Request.builder()
                    .email("tester@gmail.com")
                    .build();
            beforeEmailSendSize = emailSendList.size();
        }

        @Nested
        @DisplayName("success")
        public class Success {
            @Test
            @DisplayName("1. 이메일로 인증코드가 잘 들어간 뒤, 인증코드가 redis 서버에 잘 저장됨")
            public void successTest() throws EmailAuthException {
                EmailAuthDTO.Response response = emailAuthService.emailAuthSend(req);

                assertThat(response.getResult()).isEqualTo(true);
                Optional<EmailAuthentication> redis = emailAuthenticationRepository.findById(req.getEmail());
                assertThat(redis).isNotEmpty();
                assertThat(emailSendList.size()).isEqualTo(beforeEmailSendSize + 1);
            }
        }

        @Nested
        @DisplayName("fail")
        public class Fail {
            @Test
            @DisplayName("1. 이메일 전송 중 오류가 발생하면 redis서버에 저장된 인증코드가 사라져야함")
            public void throwExceptionDuringSendEmail() throws EmailSendException {
                doThrow(new EmailSendException("이메일 오류")).when(emailService).sendEmail(any(EmailSendDTO.Request.class));

                String message = assertThrows(EmailAuthException.class, () -> {
                    emailAuthService.emailAuthSend(req);
                }).getMessage();

                assertThat(message).isEqualTo("인증번호 전송 오류");
                assertThat(emailAuthenticationRepository.findById(req.getEmail())).isEmpty();
                assertThat(emailSendList.size()).isEqualTo(beforeEmailSendSize);
            }
        }
    }

    /**
     * emailAuthCheck 테스트
     * 성공 케이스
     * - 1.인증한 코드에 맞게 인증완료
     * 실패 케이스
     * - 1. 인증한 코드와 일치하지 않음
     */
    @Nested
    @DisplayName("emailAuthCheck")
    public class EmailAuthCheckTest {
        private EmailAuthCheckDTO.Request req;
        private String email = "tester@gmail.com";
        private String tempCode = "code";

        @BeforeEach
        public void init() {
            req = EmailAuthCheckDTO.Request.builder()
                    .email(email)
                    .code(tempCode)
                    .build();

            emailAuthenticationList.add(EmailAuthentication.builder()
                    .email(email)
                    .code(tempCode)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        @Nested
        @DisplayName("success")
        public class Success {
            @Test
            @DisplayName("1.인증한 코드에 맞게 인증완료")
            public void successTest() throws EmailAuthException {
                EmailAuthCheckDTO.Response response = emailAuthService.emailAuthCheck(req);

                assertThat(response.getResult()).isTrue();
            }
        }

        @Nested
        @DisplayName("fail")
        public class Fail {
            @Test
            @DisplayName("1. 인증한 코드와 일치하지 않음")
            public void notMatchCodeTest() throws EmailAuthException {
                req.setCode("notMatchCode");

                EmailAuthCheckDTO.Response response = emailAuthService.emailAuthCheck(req);

                assertThat(response.getResult()).isFalse();
            }
        }
    }
}