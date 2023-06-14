package com.hyeonuk.todo.member.dto;

import lombok.*;


public class SaveDTO {//세이브 할 떄 필요한 request, response
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String id;//아이디값 (5~20)
        private String email;//이메일값
        private String password;//비밀번호값
        private String passwordCheck;//비밀번호 체크
        private String name;//이름
        private boolean agree;//약관 동의

        private String address;
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        private String id;
        private String email;
        private String name;
    }
}
