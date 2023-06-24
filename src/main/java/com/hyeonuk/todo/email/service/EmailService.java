package com.hyeonuk.todo.email.service;

import com.hyeonuk.todo.email.dto.EmailSendDTO;
import com.hyeonuk.todo.email.exception.EmailSendException;

public interface EmailService {
    public void sendEmail(EmailSendDTO.Request dto) throws EmailSendException;
}
