package com.hyeonuk.todo.email.service;

import com.hyeonuk.todo.email.dto.EmailAuthRemoveDTO;
import com.hyeonuk.todo.email.exception.EmailAuthException;
import com.hyeonuk.todo.email.dto.EmailAuthCheckDTO;
import com.hyeonuk.todo.email.dto.EmailAuthDTO;

public interface EmailAuthService {
    EmailAuthDTO.Response emailAuthSend(EmailAuthDTO.Request dto) throws EmailAuthException;
    EmailAuthCheckDTO.Response emailAuthCheck(EmailAuthCheckDTO.Request dto) throws EmailAuthException;

    void emailAuthRemove(EmailAuthRemoveDTO.Request dto) throws EmailAuthException;
}
