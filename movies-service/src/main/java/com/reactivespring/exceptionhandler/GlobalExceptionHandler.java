package com.reactivespring.exceptionhandler;

import com.reactivespring.exception.MoviesInfoClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MoviesInfoClientException.class)
    public ResponseEntity handleMoviesException(MoviesInfoClientException moviesInfoClientException) {
        log.error("Exception cought in handleMoviesException : {}", moviesInfoClientException.getMessage() );
        return ResponseEntity.status(moviesInfoClientException.getStatusCode()).body(moviesInfoClientException.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity handleRuntimeException(RuntimeException rex) {
        log.error("Exception cought in handleMoviesException : {}", rex.getMessage() );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(rex.getMessage());
    }
}
