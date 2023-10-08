package com.cardmonix.cardmonix.controllers;

import com.cardmonix.cardmonix.request.TradeCoinRequest;
import com.cardmonix.cardmonix.response.ApiResponse;
import com.cardmonix.cardmonix.response.DepositReponse;
import com.cardmonix.cardmonix.service.DepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/deposit")
@RequiredArgsConstructor
public class DepositController {
    private final DepositService depositService;
    @CrossOrigin("*")
    @GetMapping("/get-deposit-byUser/{userId}")
    public ResponseEntity<ApiResponse<List<DepositReponse>>> getDeposit(@PathVariable Long userId){
        ApiResponse<List<DepositReponse>> apiResponse = new ApiResponse<>(depositService.getAllDepositByUser(userId));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @CrossOrigin("*")
    @PostMapping("/buy-coin")
    public ResponseEntity<ApiResponse<String>> buyCoin(@RequestBody TradeCoinRequest tradeCoinRequest){
        ApiResponse<String> apiResponse = new ApiResponse<>(depositService.TradeCoin(tradeCoinRequest));
        return new ResponseEntity<>(apiResponse,HttpStatus.CREATED);
    }
    @CrossOrigin("*")
    @PostMapping("/withdraw/{price}/{coin}")
    public ResponseEntity<ApiResponse<String>> withdraw(@PathVariable Double price, @PathVariable String coin){
        ApiResponse<String> apiResponse = new ApiResponse<>(depositService.withdraw(price,coin));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);

    }
    @CrossOrigin("*")
    @PutMapping(value = "/upload-proof",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> topUp(@RequestParam("file") MultipartFile file ,@RequestParam("id") Long id){
        ApiResponse<String> apiResponse = new ApiResponse<>(depositService.uploadProof(file,id));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);

    }
}
