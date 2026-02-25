package com.qridaba.qridabaplatform.payload;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data){
        return new ResponseEntity<>(
                new ApiResponse<>(message, HttpStatus.OK.value(), data),
                HttpStatus.OK
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data){
        return new ResponseEntity<>(
                new ApiResponse<>(message, HttpStatus.CREATED.value(), data),
                HttpStatus.CREATED
        );
    }

    public static ResponseEntity<ApiResponse<Object>> error(String message, HttpStatus status){
        return new ResponseEntity<>(
                new ApiResponse<>(message, status.value(), null),
                status
        );
    }
}
