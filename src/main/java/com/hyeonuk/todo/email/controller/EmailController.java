package com.hyeonuk.todo.email.controller;

import com.hyeonuk.todo.email.dto.EmailAuthCheckDTO;
import com.hyeonuk.todo.email.dto.EmailAuthDTO;
import com.hyeonuk.todo.email.exception.EmailAuthException;
import com.hyeonuk.todo.email.service.EmailAuthService;
import com.hyeonuk.todo.integ.dto.ErrorMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailAuthService emailAuthService;

    @PostMapping("/auth")
    public ResponseEntity<EmailAuthDTO.Response> emailAuthentication(
            @RequestBody EmailAuthDTO.Request dto) throws EmailAuthException {
        return new ResponseEntity<>(emailAuthService.emailAuthSend(dto), HttpStatus.CREATED);
    }

    @PostMapping("/auth/check")
    public ResponseEntity<EmailAuthCheckDTO.Response> emailAuthenticationCheck(
            @RequestBody EmailAuthCheckDTO.Request dto) throws EmailAuthException{
        return new ResponseEntity<>(emailAuthService.emailAuthCheck(dto),HttpStatus.OK);
    }

    @ExceptionHandler(EmailAuthException.class)
    public ResponseEntity<ErrorMessageDTO> error(EmailAuthException e){
        return new ResponseEntity<>(ErrorMessageDTO.builder()
                .message(e.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
