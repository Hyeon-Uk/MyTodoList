package com.hyeonuk.todo.email.dto;

import lombok.*;

public class EmailAuthRemoveDTO {
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class Request{
        private String key;
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class Response{
        private boolean result;
    }
}
