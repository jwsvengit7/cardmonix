package com.cardmonix.cardmonix.service;

import com.cardmonix.cardmonix.request.TradeCoinRequest;
import com.cardmonix.cardmonix.response.DepositReponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DepositService {
    String withdraw(Double amount,String coin);

    List<DepositReponse> getAllDepositByUser(Long userId);
    String uploadProof(MultipartFile proof, Long id);

    String TradeCoin(TradeCoinRequest tradeCoinRequest);

}
