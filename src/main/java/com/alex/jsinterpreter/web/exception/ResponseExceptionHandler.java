package com.alex.jsinterpreter.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * class responsible for handling exceptions which will be returned in response to user
 *
 * @author Oleksandr Myronenko
 */
@RestControllerAdvice
public class ResponseExceptionHandler {
    @ExceptionHandler(UnsupportedOperationException.class)
    public final ResponseEntity<Object> handleUnsupportedOperationException(UnsupportedOperationException exception) {
        return ResponseEntity
                .of(ProblemDetail
                        .forStatusAndDetail(HttpStatus.NOT_IMPLEMENTED, "This operation does not supported"))
                .build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public final ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException exception) {
        return ResponseEntity
                .of(ProblemDetail
                        .forStatusAndDetail(HttpStatus.NOT_FOUND, "JS code was not found by id"))
                .build();
    }
}
