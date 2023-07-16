package com.hyeonuk.todo.todo.dto;

import lombok.*;

public class CategoryUpdateDTO {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Request{
        private String userId;
        private Long categoryId;
        private String title;
    }
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Response{
        private Long categoryId;
        private String title;
    }
}
