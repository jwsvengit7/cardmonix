package com.cardmonix.cardmonix.domain.entity.coins;

import com.cardmonix.cardmonix.domain.constant.Status;
import com.cardmonix.cardmonix.domain.entity.userModel.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "giftcard")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Giftcard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long card_id;

    private String type;
    private Double amount;
    private LocalDateTime localDateTime;
    private String trace;
    @Enumerated(EnumType.STRING)
    private Status status;

    private String imageUrl;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id")
    private User user;

}
