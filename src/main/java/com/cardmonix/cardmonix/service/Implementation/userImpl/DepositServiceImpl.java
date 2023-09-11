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
import com.cardmonix.cardmonix.events.PendingDeposit;
import com.cardmonix.cardmonix.request.RequestFromCoins;
import com.cardmonix.cardmonix.request.TradeCoinRequest;
import com.cardmonix.cardmonix.response.DepositReponse;
import com.cardmonix.cardmonix.service.DepositService;
import com.cardmonix.cardmonix.service.Implementation.authImpl.AuthServiceImpl;
import com.cardmonix.cardmonix.utils.CoinsUtils;
import com.cardmonix.cardmonix.utils.RandomValues;
import com.cardmonix.cardmonix.utils.UserUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
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
public class DepositServiceImpl implements ApplicationRunner,DepositService {
    private final HttpHeaders headers;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AuthServiceImpl authServiceImplentation;
    private final GiftcardRepository giftcardRepository;
    private final CoinWalletRepository coinWalletRepository;
    private final ApplicationEventPublisher publisher;
    private final CoinRepository coinRepository;
    private final BalanceRepository balanceRepository;
    private final DepositRepository depositRepository;
    private final  ObjectMapper objectMapper;

    @Override
    public synchronized void run(ApplicationArguments args) throws JsonProcessingException {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cache-Control","no-cache");
        String api= CoinsUtils.coinGeckoApi();
        RequestEntity<?> requestEntity = new RequestEntity<>(headers,HttpMethod.GET, URI.create(api));
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            List<RequestFromCoins> coins = null;
            ArrayList<String> saveCoin = new ArrayList<>();
            String[] coin = {
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
            saveCoin.addAll(Arrays.asList(coin));
            coins = objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<RequestFromCoins>>() {
            });
            coins.forEach((coinValues) -> {
                Coin findCoin = coinRepository.findCoinByName(coinValues.getName());
                if (findCoin != null) {
                    Float price = coinValues.getCurrent_price();
                    Coin GetCoinName = coinRepository.findCoinByName(coinValues.getName());
                    Float oldPrice = GetCoinName.getOld_price();

                    if (price > oldPrice && GetCoinName != null) {
                        GetCoinName.setOld_price(oldPrice);
                        GetCoinName.setCurrent_price(price);
                        GetCoinName.setImage(coinValues.getImage());
                        GetCoinName.setName(coinValues.getName());

                        coinRepository.save(GetCoinName);
                    }
                } else {
                    Coin saveNewCoin = new Coin(coinValues.getName(), coinValues.getCurrent_price(), coinValues.getImage(), coinValues.getCurrent_price());
                    for (String s : saveCoin) {
                        if (saveNewCoin.getName().equals(s)) {
                            saveNewCoin.setActivate(true);
                        }
                    }
                    coinRepository.save(saveNewCoin);
                }

            });
        }  catch(Exception e){
            System.out.println(e.getMessage()+ANSI_RED);
        }

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

    @Override
    public List<DepositReponse> getAllDepositByUser(){
        User user = authServiceImplentation.verifyUser(UserUtils.getAccessTokenEmail());
        return user.getDeposits().stream().map((value)->modelMapper.map(value,DepositReponse.class)).collect(Collectors.toList());

    }

    @Override
    public String TradeCoin(TradeCoinRequest tradeCoinRequest) {
        User user =isLogging(UserUtils.getAccessTokenEmail());
        float current_price = coinRepository.findCoinByName(tradeCoinRequest.getCoin()).getCurrent_price();
        float convertedAmount = (float) (tradeCoinRequest.getAmount() / current_price);

        Deposit deposit = Deposit.builder()
                .transId(UUID.randomUUID().toString())
                .amountInCurrency(convertedAmount)
                .amount(tradeCoinRequest.getAmount())
                .status(Status.PENDING)
                .coin(tradeCoinRequest.getCoin())
                .user(user)
                .localDateTime(LocalDateTime.now())
                .build();
        depositRepository.save(deposit);
        publisher.publishEvent(new PendingDeposit(deposit,"deposit"));

        return ANSI_BLUE+"Successfully Buy "+ tradeCoinRequest.getCoin() + " for "+ convertedAmount;

    }
    @Override
    public String withdraw(Double amount,String coin){
        User user =isLogging(UserUtils.getAccessTokenEmail());
        CheckBalance(user);
        Balance userBalance = user.getBalance();
        boolean check = checkUserCoinBalance(user.getCoinWallets(),coin,amount);
        if(check) {
            userBalance.setAmount(userBalance.getAmount() - amount);
            balanceRepository.save(userBalance);
            return ANSI_BLUE+"Successful Withdraw";
        }
        return ANSI_RED+"Error occurred";
    }

    private boolean checkUserCoinBalance(List<CoinWallet> coinWallets, String coin, Double amount) {
        Coin checkCoin = coinRepository.findCoinByName(coin);
        for (CoinWallet c : coinWallets) {
            if (c.getCoin().equals(coin)) {
                c.setWallet_amount(c.getWallet_amount() - amount);
                c.setWalletInUsd((float) (c.getWalletInUsd() - checkCoin.getCurrent_price() / amount));
                coinWalletRepository.save(c);
                return true;
            }
        }

        return false;
    }


    @Override
    public List<DepositReponse> getAllDeposit(){
       return depositRepository.findAll()
               .stream().map((c)->
                   modelMapper.map(c,DepositReponse.class)).collect(Collectors.toList());
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

    public String validateAmount(Double amount){
        List<User> userlist = userRepository.findAll();
        List<Coin> coin  = coinRepository.findAll();
        userlist.stream()
                .map((c)->{
                    Balance userBalance = c.getBalance();
                    Double userAmount  =userBalance.getAmount();
                    return userBalance;

                });
        return null;

    }


}
