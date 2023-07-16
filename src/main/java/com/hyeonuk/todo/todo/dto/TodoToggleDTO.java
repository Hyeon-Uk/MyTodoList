package com.hyeonuk.todo.todo.dto;

import lombok.*;

public class TodoToggleDTO {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Request{
        private String userId;
        private Long todoId;
    }
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Response{
        private boolean result;
    }
}
