package com.hyeonuk.todo.email.dto;

import lombok.*;

public class EmailAuthRemoveDTO {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        private String key;
    }
}
