package com.hyeonuk.todo.email.service;

import com.hyeonuk.todo.email.dto.EmailSendDTO;
import com.hyeonuk.todo.email.entity.EmailAuthentication;
import com.hyeonuk.todo.email.exception.EmailAuthException;
import com.hyeonuk.todo.email.dto.EmailAuthCheckDTO;
import com.hyeonuk.todo.email.dto.EmailAuthDTO;
import com.hyeonuk.todo.email.exception.EmailSendException;
import com.hyeonuk.todo.email.repository.EmailAuthenticationRepository;
import com.hyeonuk.todo.integ.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class EmailAuthServiceImpl implements EmailAuthService {
    private final EmailService emailService;//이메일을 보낼 service객체 주입
    private final EmailAuthenticationRepository emailAuthenticationRepository;//이메일 인증코드를 저장할 repository


    //문자와 숫자로 이루어진 랜덤한 코드를 생성

    private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 10;
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuffer sb = new StringBuffer(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)//이메일을 보내다가
    public EmailAuthDTO.Response emailAuthSend(EmailAuthDTO.Request dto) throws EmailAuthException {
        try {
            String code = generateCode();//랜덤 코드 생성
            String target = dto.getEmail();//코드를 전송할 이메일 생성

            //인증코드를 cache서버에 저장
            emailAuthenticationRepository.save(
                    EmailAuthentication.builder()
                            .email(target)
                            .code(code)
                            .build()
            );

            //인증번호를 전달할 객체를 만들기
            EmailSendDTO.Request sendRequest = EmailSendDTO.Request.builder()
                    .subject("MyTodoList 인증메일입니다.")
                    .to(target)
                    .content("이메일 인증 번호입니다.\n인증코드 : ".concat(code))
                    .build();

            emailService.sendEmail(sendRequest);
            return EmailAuthDTO.Response.builder()
                    .result(true)
                    .build();
        } catch (Exception e) {
            //오류가 발생하면 redis서버에서 인증코드를 삭제함
            emailAuthenticationRepository.deleteById(dto.getEmail());
            throw new EmailAuthException("인증번호 전송 오류");
        }
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)//커밋된 내용만 읽기.
    public EmailAuthCheckDTO.Response emailAuthCheck(EmailAuthCheckDTO.Request dto) throws EmailAuthException {
        try {
            String email = dto.getEmail();
            String code = dto.getCode();

            //cache서버에 저장된 인증코드와 일치하는지 확인
            Optional<EmailAuthentication> authentication = emailAuthenticationRepository.findById(email);

            if (authentication.isPresent()) {
                EmailAuthentication result = authentication.get();
                return EmailAuthCheckDTO.Response.builder()
                        //결과값이 같으면 true,아니면 false리턴
                        .result(!StringUtils.isBlank(code)
                                && !StringUtils.isBlank(result.getCode())
                                && result.getCode().equals(code))
                        .build();
            } else {
                return EmailAuthCheckDTO.Response.builder()
                        .result(false)
                        .build();
            }
        } catch (Exception e) {
            throw new EmailAuthException("이메일 인증 오류");
        }
    }
}
