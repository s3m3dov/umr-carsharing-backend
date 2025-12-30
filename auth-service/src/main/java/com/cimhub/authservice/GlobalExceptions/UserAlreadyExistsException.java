package com.cimhub.authservice.GlobalExceptions;

public class UserAlreadyExistsException extends  RuntimeException{
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
