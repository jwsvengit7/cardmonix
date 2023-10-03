package com.cardmonix.cardmonix.controllers;

import com.cardmonix.cardmonix.request.LoginRequest;
import com.cardmonix.cardmonix.request.OtpValidateRequest;
import com.cardmonix.cardmonix.request.RegisterRequest;
import com.cardmonix.cardmonix.response.ApiResponse;
import com.cardmonix.cardmonix.response.ResponseFromUser;
import com.cardmonix.cardmonix.service.AuthService;
import com.cardmonix.cardmonix.service.OtpService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;




@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@RestController
public class UserAuthController {
    private final AuthService authService;
    private final OtpService otpService;


    @CrossOrigin("*")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> createUser(@RequestBody RegisterRequest registerRequest) throws MessagingException, UnsupportedEncodingException {
        ApiResponse<String> apiResponse = new ApiResponse<>(authService.RegisterRequest(registerRequest));
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);

    }
    @CrossOrigin("*")
    @GetMapping("/regenerate-otp")
    public ResponseEntity<ApiResponse<String>> generateOtp(@RequestParam("email") String email) {
        ApiResponse<String> apiResponse = new ApiResponse<>(authService.resend_link_password(email));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @CrossOrigin("*")
    @GetMapping("/verify-token/{token}")
    public ResponseEntity<ApiResponse<Boolean>> verifyToken(@PathVariable String token) {
        ApiResponse<Boolean> apiResponse = new ApiResponse<>(authService.verify_token(token));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
    @CrossOrigin("*")
    @PostMapping("/otp_verify")
    public ResponseEntity<ApiResponse<String>> verifyUser(@RequestBody OtpValidateRequest otpValidateRequest){
        ApiResponse<String> verify = new ApiResponse<>(otpService.verifyUserOtp(otpValidateRequest));
        return new ResponseEntity<>(verify,HttpStatus.ACCEPTED);

    }
    @CrossOrigin("*")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<ResponseFromUser>> loginUser(@RequestBody LoginRequest loginRequest){
        ApiResponse<ResponseFromUser> apiResponse= new ApiResponse<>(authService.LoginRequest(loginRequest));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }




}
