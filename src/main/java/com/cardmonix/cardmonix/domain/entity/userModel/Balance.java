package com.cardmonix.cardmonix.domain.entity.userModel;

import com.cardmonix.cardmonix.domain.constant.Currency;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Table(name = "balance")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long balaceId;
    @Column(name = "amount")
    private Double amount;

    @Column(name = "giftcard_amount")
    private Double giftcard_amount;
    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private User user;
    public Balance(Currency currency,Double amount) {
        this.currency=currency;
        this.amount=amount;
    }
}
