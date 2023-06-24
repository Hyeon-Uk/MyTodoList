package com.hyeonuk.todo.member.controller;

import com.hyeonuk.todo.integ.dto.ErrorMessageDTO;
import com.hyeonuk.todo.integ.exception.AlreadyExistException;
import com.hyeonuk.todo.member.exception.UserInfoNotFoundException;
import com.hyeonuk.todo.integ.exception.ValidationException;
import com.hyeonuk.todo.member.dto.LoginDTO;
import com.hyeonuk.todo.member.dto.SaveDTO;
import com.hyeonuk.todo.member.exception.LoginException;
import com.hyeonuk.todo.member.exception.SaveException;
import com.hyeonuk.todo.member.service.MemberAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class MemberAuthController {
    private final MemberAuthService memberAuthService;

    @PostMapping(value = "/login",produces = "application/json;charset=utf-8",consumes = "application/json;charset=utf-8")
    public ResponseEntity<LoginDTO.Response> login(@RequestBody LoginDTO.Request dto) throws LoginException, UserInfoNotFoundException,ValidationException {
        try {
            return new ResponseEntity<>(memberAuthService.login(dto), HttpStatus.OK);
        }catch(UserInfoNotFoundException | ValidationException | LoginException e){
            throw e;
        }
    }

    @PostMapping("/regist")
    public ResponseEntity<SaveDTO.Response> regist(@RequestBody SaveDTO.Request dto) throws AlreadyExistException,ValidationException,SaveException{
        try{
            SaveDTO.Response save = memberAuthService.save(dto);
            return new ResponseEntity<>(save,HttpStatus.CREATED);
        }catch(AlreadyExistException | ValidationException | SaveException  e) {
            //복구코드 작성
            throw e;
        }
    }

    @ExceptionHandler({AlreadyExistException.class,ValidationException.class,SaveException.class})
    public ResponseEntity<ErrorMessageDTO> badRequestErrorHandler(Exception e){
        return new ResponseEntity<>(ErrorMessageDTO.builder()
                .message(e.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .build(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({LoginException.class,UserInfoNotFoundException.class})
    public ResponseEntity<ErrorMessageDTO> unauthorizedErrorHandler(Exception e){
        return new ResponseEntity<>(ErrorMessageDTO.builder()
                .message(e.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build(),HttpStatus.UNAUTHORIZED);
    }
}
