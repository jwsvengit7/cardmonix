package com.cardmonix.cardmonix.service.Implementation.userImpl;

import com.cardmonix.cardmonix.configurations.CloudinaryConfig;
import com.cardmonix.cardmonix.domain.constant.Status;
import com.cardmonix.cardmonix.domain.entity.account.Deposit;
import com.cardmonix.cardmonix.domain.entity.coins.Coin;
import com.cardmonix.cardmonix.domain.entity.userModel.Balance;
import com.cardmonix.cardmonix.domain.entity.userModel.CoinWallet;
import com.cardmonix.cardmonix.domain.entity.userModel.User;
import com.cardmonix.cardmonix.domain.repository.*;
import com.cardmonix.cardmonix.eceptions.InsufficientFundException;
import com.cardmonix.cardmonix.eceptions.UserNotFoundException;
import com.cardmonix.cardmonix.events.PendingDeposit;
import com.cardmonix.cardmonix.request.RequestFromCoins;
import com.cardmonix.cardmonix.request.TradeCoinRequest;
import com.cardmonix.cardmonix.response.DepositReponse;
import com.cardmonix.cardmonix.service.DepositService;
import com.cardmonix.cardmonix.service.Implementation.authImpl.AuthServiceImpl;
import com.cardmonix.cardmonix.utils.CoinsUtils;
import com.cardmonix.cardmonix.utils.DepositUtils;
import com.cardmonix.cardmonix.utils.RandomValues;
import com.cardmonix.cardmonix.utils.UserUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.cardmonix.cardmonix.utils.ASCIIColors.ANSI_BLUE;
import static com.cardmonix.cardmonix.utils.ASCIIColors.ANSI_RED;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositServiceImpl implements ApplicationRunner,DepositService {
    private final HttpHeaders headers;
    private final RestTemplate restTemplate;
    private final ModelMapper modelMapper;
    private final AuthServiceImpl authServiceImplentation;
    private final CoinWalletRepository coinWalletRepository;
    private final ApplicationEventPublisher publisher;
    private final CoinRepository coinRepository;
    private final BalanceRepository balanceRepository;
    private final DepositRepository depositRepository;
    private final  ObjectMapper objectMapper;
    private String[] mySelectedCoin(){
       return new String[]{
                "Bitcoin",
                "Ethereum",
                "Tether",
                "BNB",
                "XRP",
                "Dogecoin",
                "Bitcoin Cash",
                "Cardano",
                "Litecoin",
                "TRON"};
    }


    @Scheduled(cron = "0 0 8 * * ?")
    private void runCoinPriceSheduled(){
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cache-Control","no-cache");
        String api= CoinsUtils.coinGeckoApi();
        RequestEntity<?> requestEntity = new RequestEntity<>(headers,HttpMethod.GET, URI.create(api));
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            List<RequestFromCoins> coins = null;
            ArrayList<String> saveCoin = new ArrayList<>(Arrays.asList(mySelectedCoin()));
            coins = objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<RequestFromCoins>>() {
            });
            coins.forEach((coinValues) -> {
                validateDbCheckUpPrice(coinValues,saveCoin);
            });
            log.info("coin updated"+ANSI_BLUE);
        }  catch(Exception e){
            log.info(e.getMessage()+ANSI_RED);
        }
    }
    private void validateDbCheckUpPrice(RequestFromCoins coinValues,ArrayList<String> saveCoin){
        Coin findCoin = coinRepository.findCoinByName(coinValues.getName());
        if (findCoin != null) {
            Float price = coinValues.getCurrent_price();
            Coin getCoinName = coinRepository.findCoinByName(coinValues.getName());
            Float oldPrice = getCoinName.getOld_price();

            if (price > oldPrice && getCoinName != null) {
                getCoinName.setOld_price(oldPrice);
                getCoinName.setCurrent_price(price);
                getCoinName.setImage(coinValues.getImage());
                getCoinName.setName(coinValues.getName());
                coinRepository.save(getCoinName);
            }
        } else {
            Coin saveNewCoin = new Coin(coinValues.getName(),
                    coinValues.getCurrent_price(),
                    coinValues.getImage(),
                    coinValues.getCurrent_price());
            for (String s : saveCoin) {
                if (saveNewCoin.getName().equals(s)) {
                    saveNewCoin.setActivate(true);
                }

            }
            coinRepository.save(saveNewCoin);
        }
        log.info("coins updated");
    }

    @Override
    public synchronized void run(ApplicationArguments args) throws JsonProcessingException {
        runCoinPriceSheduled();
    }


    @Override
    public List<DepositReponse> getAllDepositByUser(Long userId){
        User user = authServiceImplentation.verifyUser(UserUtils.getAccessTokenEmail());
        System.out.println(user.getId());
        if(!userId.equals(user.getId())) {
            throw new UserNotFoundException("User not found with deposit");
        }
        return user.getDeposits().stream().map((value)->modelMapper.map(value,DepositReponse.class)).collect(Collectors.toList());

    }

    @Override
    public String TradeCoin(TradeCoinRequest tradeCoinRequest) {
        User user =isLogging(UserUtils.getAccessTokenEmail());
        Coin coin = coinRepository.findCoinByName(tradeCoinRequest.getCoin());
        float current_price = coin.getCurrent_price();
        float convertedAmount = (float) (tradeCoinRequest.getAmount() / current_price);
        Deposit deposit = mapDeposit(convertedAmount,tradeCoinRequest,coin.getImage(),user);
        depositRepository.save(deposit);
        publisher.publishEvent(new PendingDeposit(deposit,"deposit"));
        return "Successfully Buy "+ tradeCoinRequest.getCoin() + " for "+ convertedAmount;

    }


    @Override
    public String withdraw(Double amount,String coin){
        User user =isLogging(UserUtils.getAccessTokenEmail());
        CheckBalance(user);
        Balance userBalance = user.getBalance();
        boolean check = DepositUtils.checkUserCoinBalance(user.getCoinWallets(),coin,amount,coinRepository,coinWalletRepository);
        if(check) {
            userBalance.setAmount(userBalance.getAmount() - amount);
            balanceRepository.save(userBalance);
            return ANSI_BLUE+"Successful Withdraw";
        }
        return ANSI_RED+"Error occurred";
    }




    @Override
    public String uploadProof(MultipartFile proof,Long id){
        isLogging(UserUtils.getAccessTokenEmail());
        Deposit topUp = depositRepository.findById(id).orElseThrow(()-> new RuntimeException("INVALID CREDENTIALS"));
        CloudinaryConfig cloudinaryConfig = new CloudinaryConfig();
        topUp.setProof(cloudinaryConfig.uploadPicture(proof, RandomValues.generateRandom()+new Date().getTime()));
        depositRepository.save(topUp);
        return "Successfully Upload  your proof of payment";
    }

    private Deposit mapDeposit(float convertedAmount,TradeCoinRequest tradeCoinRequest,String image,User user){
        return Deposit.builder()
                .transId(UUID.randomUUID().toString())
                .amountInCurrency(convertedAmount)
                .amount(tradeCoinRequest.getAmount())
                .status(Status.PENDING)
                .image(image)
                .coin(tradeCoinRequest.getCoin())
                .user(user)
                .localDateTime(LocalDateTime.now())
                .build();
    }

    private void CheckBalance(User user){
        System.out.println(user.getEmail());
        Balance checkBalance = balanceRepository.findBalanceByUser(user);
        if(checkBalance==null){
            throw new InsufficientFundException("Insufficient funds OR Check Type of Card");
        }
    }
    private User isLogging(String user){
        return authServiceImplentation.verifyUser(user);
    }


}
