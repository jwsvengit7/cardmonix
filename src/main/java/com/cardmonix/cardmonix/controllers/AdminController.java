package com.cardmonix.cardmonix.controllers;

import com.cardmonix.cardmonix.response.ApiResponse;
import com.cardmonix.cardmonix.response.DepositReponse;
import com.cardmonix.cardmonix.response.UserResponse;
import com.cardmonix.cardmonix.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/admin")
public class AdminController {
    private final AdminService adminService;
    @CrossOrigin("*")
    @GetMapping("/confirm-deposit/{id}")
    public ResponseEntity<ApiResponse<String>> confirmDeposit(@PathVariable Long id){
        ApiResponse<String> apiResponse = new ApiResponse<>(adminService.confirmDeposit(id));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
    @CrossOrigin("*")
    @GetMapping("/confirm-giftcard/{giftcardId}")
    public ResponseEntity<ApiResponse<String>> confirmGiftcard(@PathVariable Long giftcardId){
        ApiResponse<String> apiResponse = new ApiResponse<>(adminService.confirmGiftcardDeposit(giftcardId));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
    @CrossOrigin("*")
    @GetMapping("/get-all-deposit")
    public ResponseEntity<ApiResponse<List<DepositReponse>>> getAllDeposit(){
        ApiResponse<List<DepositReponse>> apiResponse = new ApiResponse<>(adminService.getAllDeposit());
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @CrossOrigin("*")
    @GetMapping("/getuserById/{userid}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userid){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>(adminService.getUserById(userid));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
    @CrossOrigin("*")
    @DeleteMapping("/delete-user/{userId}")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable("userId") Long userId){
        adminService.deleteUser(userId);
        ApiResponse<?> apiResponse = new ApiResponse<>("DELETED");
        return new ResponseEntity<>(apiResponse, HttpStatus.GONE);
    }
    @CrossOrigin("*")
    @GetMapping("/getUsers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getsortedPage(){
        ApiResponse<List<UserResponse>> apiResponse = new ApiResponse<>(adminService.getAllUsers());
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
