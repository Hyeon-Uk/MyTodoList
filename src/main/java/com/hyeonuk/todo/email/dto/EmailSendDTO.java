package com.hyeonuk.todo.email.dto;

import lombok.*;

public class EmailSendDTO {
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @ToString

    public static class Request{
        String subject;
        String to;
        String content;
    }
}
