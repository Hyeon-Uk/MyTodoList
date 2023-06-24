package com.hyeonuk.todo.security.exception;

public class JwtException extends Exception{
    public JwtException(String message) {
        super(message);
    }
}
