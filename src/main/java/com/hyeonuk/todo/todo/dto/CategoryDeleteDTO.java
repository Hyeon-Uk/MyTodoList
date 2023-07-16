package com.hyeonuk.todo.todo.dto;

import lombok.*;

public class CategoryDeleteDTO {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Request{
        private String userId;
        private Long categoryId;
    }
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Response{
        private Long categoryId;
        private boolean result;
    }
}
