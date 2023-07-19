package com.hyeonuk.todo.email.service;

import com.hyeonuk.todo.email.dto.EmailSendDTO;
import com.hyeonuk.todo.email.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;//이메일을 보낼 mailSender 주입
    @Override
    @Transactional(rollbackFor = {EmailSendException.class})
    public void sendEmail(EmailSendDTO.Request dto) throws EmailSendException {
        try{
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message,false,"UTF-8");

            helper.setTo(dto.getTo());
            helper.setSubject(dto.getSubject());
            helper.setText(dto.getContent(),true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("이메일 발송 오류1");
        } catch(Exception e){//알 수 없는 오류
            throw new EmailSendException("이메일 발송 오류2");
        }
    }
}
