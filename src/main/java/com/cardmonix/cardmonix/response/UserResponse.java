package com.cardmonix.cardmonix.response;

import com.cardmonix.cardmonix.domain.constant.Role;
import com.cardmonix.cardmonix.domain.entity.userModel.Balance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String user_name;
    private String phone;
    private String dob;
    private String profile;
    private  Role role;
    private Balance balance;

}
