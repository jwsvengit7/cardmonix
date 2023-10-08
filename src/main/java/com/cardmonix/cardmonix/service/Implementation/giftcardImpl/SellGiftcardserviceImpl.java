package com.cardmonix.cardmonix.service.Implementation.giftcardImpl;

import com.cardmonix.cardmonix.configurations.CloudinaryConfig;
import com.cardmonix.cardmonix.domain.constant.Status;
import com.cardmonix.cardmonix.domain.entity.coins.Giftcard;
import com.cardmonix.cardmonix.domain.entity.userModel.User;
import com.cardmonix.cardmonix.domain.repository.GiftcardRepository;
import com.cardmonix.cardmonix.request.SaleGiftcardRequest;
import com.cardmonix.cardmonix.service.Implementation.authImpl.AuthServiceImpl;
import com.cardmonix.cardmonix.service.SellGiftcardService;
import com.cardmonix.cardmonix.utils.RandomValues;
import com.cardmonix.cardmonix.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SellGiftcardserviceImpl implements SellGiftcardService {
    private final GiftcardRepository giftcardRepository;
    private final AuthServiceImpl serviceImplentation;


    @Override
    public String saleGiftcards(SaleGiftcardRequest saleGiftcardRequest, MultipartFile file) {
        User user = serviceImplentation.verifyUser(UserUtils.getAccessTokenEmail());
        CloudinaryConfig cloudinaryConfig = new CloudinaryConfig();
        Giftcard giftcard = Giftcard.builder()
                .amount(saleGiftcardRequest.getAmount())
                .type(saleGiftcardRequest.getName())
                .status(Status.PENDING)
                .imageUrl(cloudinaryConfig.uploadPicture(file, RandomValues.generateRandom()+System.currentTimeMillis()))
                .user(user)
                .build();
        giftcardRepository.save(giftcard);
        return "success";
    }
}
