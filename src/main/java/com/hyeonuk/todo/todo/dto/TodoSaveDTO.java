package com.hyeonuk.todo.todo.dto;

import lombok.*;

public class TodoSaveDTO {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Request{
        private String userId;
        private Long categoryId;
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
