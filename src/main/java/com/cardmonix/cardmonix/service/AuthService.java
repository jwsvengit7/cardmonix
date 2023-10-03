package com.cardmonix.cardmonix.service;

import com.cardmonix.cardmonix.request.LoginRequest;
import com.cardmonix.cardmonix.request.RegisterRequest;
import com.cardmonix.cardmonix.response.ResponseFromUser;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface AuthService {
    ResponseFromUser LoginRequest(LoginRequest loginRequest);
    String resend_link_password(String email);
    String RegisterRequest(RegisterRequest registerRequest) throws MessagingException, UnsupportedEncodingException;
    Boolean verify_token(String token);

}
