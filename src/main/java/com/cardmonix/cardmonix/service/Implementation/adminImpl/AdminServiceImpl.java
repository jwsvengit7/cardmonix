package com.cardmonix.cardmonix.service.Implementation.adminImpl;

import com.cardmonix.cardmonix.domain.constant.Currency;
import com.cardmonix.cardmonix.domain.constant.Status;
import com.cardmonix.cardmonix.domain.entity.account.Deposit;
import com.cardmonix.cardmonix.domain.entity.userModel.Balance;
import com.cardmonix.cardmonix.domain.entity.userModel.CoinWallet;
import com.cardmonix.cardmonix.domain.entity.userModel.User;
import com.cardmonix.cardmonix.domain.repository.BalanceRepository;
import com.cardmonix.cardmonix.domain.repository.CoinWalletRepository;
import com.cardmonix.cardmonix.domain.repository.DepositRepository;
import com.cardmonix.cardmonix.domain.repository.UserRepository;
import com.cardmonix.cardmonix.eceptions.ProductNotFoundException;
import com.cardmonix.cardmonix.eceptions.UserNotFoundException;
import com.cardmonix.cardmonix.response.DepositReponse;
import com.cardmonix.cardmonix.response.UserResponse;
import com.cardmonix.cardmonix.service.AdminService;
import com.cardmonix.cardmonix.service.Implementation.authImpl.AuthServiceImpl;
import com.cardmonix.cardmonix.utils.UserUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final AuthServiceImpl authService;
    private final CoinWalletRepository coinWalletRepository;
    private final BalanceRepository balanceRepository;
    private final DepositRepository depositRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    @Override
    public void deleteUser(Long userId){
        authService.verifyUser(UserUtils.getAccessTokenEmail());
        User user = authService.getUserRepository().findById(userId)
                .orElseThrow(()-> new UserNotFoundException("USER NOT FOUND"));
        authService.getUserRepository().delete(user);
    }
    @Override
    public UserResponse getUserById(Long userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("USER NOT FOUND"));
        return modelMapper.map(user,UserResponse.class);
    }
    @Override
    public List<UserResponse> getAllUsers() {
        List<User> findAllUsers = userRepository.findAll();
        return findAllUsers
                .stream().map(user -> modelMapper.map(user, UserResponse.class))
                .collect(Collectors.toList());
    }
    @Override
    public List<DepositReponse> getAllDeposit(){
        return depositRepository.findAll()
                .stream().map((c)->
                        modelMapper.map(c,DepositReponse.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String confirmDeposit(Long id) {
        authService.verifyUser(UserUtils.getAccessTokenEmail());
        Deposit deposit = searchForPendingRecords(id);
        User user = deposit.getUser();
        User findUser = authService.verifyUser(user.getEmail());
        Balance userBalance;
        if (findUser.getBalance() == null || deposit.getCoin() == null) {
            userBalance = new Balance();
            userBalance.setAmount(deposit.getAmount());
        } else {
            userBalance = findUser.getBalance();
            userBalance.setAmount(userBalance.getAmount() + deposit.getAmount());
        }
        saveBalance(userBalance,findUser);
        List<CoinWallet> coinWalletList = findUser.getCoinWallets();
        if (!checkIfCoinExist(coinWalletList, deposit.getCoin())) {
            coinWalletList.add(CoinWallet.builder()
                            .coin(deposit.getCoin())
                             .user(findUser)
                    .build());
        }
        deposit.setStatus(Status.ENABLE);
        saveDeposit(deposit);
        validateCoin(coinWalletList,deposit,findUser);
        return "Payment " + (deposit.getCoin().equals("N") ? "N" : "$") + deposit.getAmount() + " has been confirmed";
    }

    private void validateCoin(List<CoinWallet> coinWalletList,Deposit deposit,User findUser){
        if(!coinWalletList.isEmpty() && checkIfCoinExist(coinWalletList,deposit.getCoin())) {
               coinWalletList.forEach((coinEntityWallet) -> {
                if (coinEntityWallet.getCoin().equals(deposit.getCoin())) {
                    log.info("Deposit confirm processing ");
                    coinEntityWallet.setCoin(deposit.getCoin());
                    coinEntityWallet.setWallet_amount((coinEntityWallet.getWallet_amount() == null ? 0 : coinEntityWallet.getWallet_amount()) +deposit.getAmount());
                    coinEntityWallet.setWalletInUsd((coinEntityWallet.getWalletInUsd() == null ?0 : coinEntityWallet.getWalletInUsd())+deposit.getAmountInCurrency());
                    coinWalletRepository.save(coinEntityWallet);
                }
            });
        }
        else{
            coinWalletRepository.save(saveCoin(deposit,findUser));
        }
    }
    private void saveDeposit(Deposit deposit) {
        depositRepository.save(deposit);
    }
    private void saveBalance(Balance userBalance,User user){
        userBalance.setCurrency(Currency.USD);
        userBalance.setUser(user);
        balanceRepository.save(userBalance);
    }

    private boolean checkIfCoinExist(List<CoinWallet> coinWallets, String coin) {
        return coinWallets.stream()
                .anyMatch(c -> c.getCoin().contains(coin));
    }

    private Deposit searchForPendingRecords(Long id) {
        return depositRepository.findByDepositIdAndStatus(id, Status.PENDING)
                .orElseThrow(() -> new ProductNotFoundException("DEPOSIT RECORD EXPIRED"));
    }

    private CoinWallet saveCoin(Deposit deposit, User user) {
        return CoinWallet.builder()
                .coin(deposit.getCoin())
                .wallet_amount(deposit.getAmount())
                .walletInUsd(deposit.getAmountInCurrency())
                .user(user)
                .build();
    }




}
