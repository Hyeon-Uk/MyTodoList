package com.hyeonuk.todo.integ.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorMessageDTO {
    private String message;
    private int status;
}
