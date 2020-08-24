package com.bpbonline.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@ControllerAdvice(basePackages = "com.bpbonline.controller.api")
public class RestApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex){
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

}
