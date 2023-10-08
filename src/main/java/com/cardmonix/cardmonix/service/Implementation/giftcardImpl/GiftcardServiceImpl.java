package com.cardmonix.cardmonix.service.Implementation.giftcardImpl;

import com.cardmonix.cardmonix.domain.constant.GiftCards;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class GiftcardServiceImpl {

    public List<GiftcardObject> getGiftcards(){
        List<GiftcardObject> allTypes = new ArrayList<>();
        for (GiftCards giftCard : GiftCards.values()) {
            allTypes.add(new GiftcardObject(giftCard.getImage(),giftCard.getAmount(), giftCard.getType()));
        }
        log.info("{} ",allTypes);
        return allTypes;
    }
}
