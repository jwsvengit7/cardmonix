package com.cardmonix.cardmonix.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaystackAccountResponse {

        private String business_name;
        private String account_number;
        private String percentage_charge;
        private String settlement_bank;
        private String integration;
        private String domain;
        private String subaccount_code;
        private boolean is_verified;
        private String settlement_schedule;
        private boolean active;
        private boolean migrate;
        private Integer id;
        private String createdAt;
        private String updatedAt;
}
