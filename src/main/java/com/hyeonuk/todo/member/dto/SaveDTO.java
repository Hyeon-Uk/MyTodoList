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

        private String emailAuthCode;//메일로 받은 인증코드를 저장할 변수

        //입력시 좌우 공백을 제거해주기 위함
        public void setId(String id) {
            if(id != null) {
                this.id = id.trim();
            }
        }

        public void setEmail(String email) {
            if(email != null) {
                this.email = email.trim();
            }
        }

        public void setName(String name) {
            if(name != null) {
                this.name = name.trim();
            }
        }
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
