package com.hyeonuk.todo.email.service;

import com.hyeonuk.todo.email.dto.EmailSendDTO;
import com.hyeonuk.todo.email.exception.EmailSendException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {
    @InjectMocks
    private EmailServiceImpl emailService;

    @Mock
    private JavaMailSender javaMailSender;

    private List<MimeMessage> mailList = new ArrayList<>();

    @BeforeEach
    public void init(){
        lenient().doAnswer(invocation -> {
            MimeMessage mimeMessage = invocation.getArgument(0);
            mailList.add(mimeMessage);
            return null;
        }).when(javaMailSender).send(any(MimeMessage.class));

        Properties properties = new Properties();
        lenient().when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getDefaultInstance(properties)));
    }

    /**
     * sendMail테스트
     * 성공케이스
     *  - 1. 이메일 정상전송
     * */
    @Nested
    @DisplayName("sendEmail")
    public class SendEmailTest{
        EmailSendDTO.Request request;
        int beforeSize;
        @BeforeEach
        public void init(){
            request = EmailSendDTO.Request.builder()
                    .to("tester@gmail.com")
                    .subject("test subject")
                    .content("test content")
                    .build();
            beforeSize = mailList.size();
        }

        @Nested
        @DisplayName("success")
        public class Success{
            @Test
            @DisplayName("1. 이메일 정상전송")
            public void successTest() throws EmailSendException {
                emailService.sendEmail(request);
                assertThat(mailList.size()).isEqualTo(beforeSize+1);//잘 들어갔는지 확인
            }
        }
    }
}