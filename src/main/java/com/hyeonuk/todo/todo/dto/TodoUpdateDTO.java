package com.hyeonuk.todo.todo.dto;

import lombok.*;

public class TodoUpdateDTO {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Request{
        private String userId;
        private Long todoId;
        private String content;
    }
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Response{
        private Long todoId;
        private String content;
    }
}
