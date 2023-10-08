package com.cardmonix.cardmonix.service;

import com.cardmonix.cardmonix.response.DepositReponse;
import com.cardmonix.cardmonix.response.UserResponse;

import java.util.List;

public interface AdminService {

    String confirmDeposit(Long id);
    void deleteUser(Long userId);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();

    List<DepositReponse> getAllDeposit();
}
