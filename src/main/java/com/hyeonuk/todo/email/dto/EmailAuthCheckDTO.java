package com.hyeonuk.todo.email.dto;

import lombok.*;

public class EmailAuthCheckDTO {
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        String email;
        String code;
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        Boolean result;
    }
}
