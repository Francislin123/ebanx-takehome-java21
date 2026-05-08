package com.ebanx.challenge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Integer> handleNotFound(NoSuchElementException e) {
        return new ResponseEntity<>(0, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Integer> handleBadRequest(IllegalArgumentException e) {
        return new ResponseEntity<>(0, HttpStatus.NOT_FOUND);
    }
}