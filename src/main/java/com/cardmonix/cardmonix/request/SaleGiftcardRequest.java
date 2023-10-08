package com.cardmonix.cardmonix.request;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleGiftcardRequest {
    private String name;
    private Double amount;

}
