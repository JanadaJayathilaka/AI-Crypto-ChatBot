package com.jana.controller;


import com.jana.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Home {
    @GetMapping
    public ResponseEntity<ApiResponse> HomeController(){
        ApiResponse response = new ApiResponse();
        response.setMessage("welcome to Ai Chatbot");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
