package com.hyeonuk.todo.email.dto;

import lombok.*;

public class EmailAuthDTO {
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        String email;
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
