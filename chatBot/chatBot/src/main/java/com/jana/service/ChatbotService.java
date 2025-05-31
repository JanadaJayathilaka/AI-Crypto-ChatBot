package com.jana.service;


import com.jana.response.ApiResponse;


public interface ChatbotService {

    ApiResponse getCoinDetails(String prompt) throws Exception;

    String simpleChat(String prompt);




}
