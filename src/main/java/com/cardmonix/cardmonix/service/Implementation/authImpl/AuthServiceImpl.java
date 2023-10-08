package com.cardmonix.cardmonix.service.Implementation.authImpl;

import com.cardmonix.cardmonix.domain.constant.Role;
import com.cardmonix.cardmonix.domain.entity.token.JwtToken;
import com.cardmonix.cardmonix.domain.entity.token.OTPConfirmation;
import com.cardmonix.cardmonix.domain.entity.userModel.User;
import com.cardmonix.cardmonix.domain.repository.BalanceRepository;
import com.cardmonix.cardmonix.domain.repository.JwtTokenRepository;
import com.cardmonix.cardmonix.domain.repository.UserRepository;
import com.cardmonix.cardmonix.eceptions.InvalidCredentialsException;
import com.cardmonix.cardmonix.eceptions.UserNotEnabledException;
import com.cardmonix.cardmonix.eceptions.UserNotFoundException;
import com.cardmonix.cardmonix.request.LoginRequest;
import com.cardmonix.cardmonix.request.RegisterRequest;
import com.cardmonix.cardmonix.response.ResponseFromUser;
import com.cardmonix.cardmonix.security.JwtService;
import com.cardmonix.cardmonix.service.AuthService;
import com.cardmonix.cardmonix.service.Implementation.otpImpl.ConfirmEmailMessageImpl;
import com.cardmonix.cardmonix.utils.DateUtils;
import com.cardmonix.cardmonix.utils.RandomValues;
import com.cardmonix.cardmonix.utils.UserUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Data
public class AuthServiceImpl implements  AuthService {
    private final UserRepository userRepository;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ConfirmEmailMessageImpl confirmEmailMessage;
    private final BalanceRepository balanceRepository;
    private final JwtTokenRepository jwtTokenRepository;

    @Value("${application.security.jwt.expiration}")
    private long expirationTime;

    @Override
    public ResponseFromUser LoginRequest(LoginRequest loginRequest)  {
        User user =verifyUser(loginRequest.getEmail());
        if(user.isActivate()) {
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new InvalidCredentialsException("Invalid Password");
            }
            JwtToken token = jwtTokenRepository.findByUser(user);
            if (token != null) {
                System.out.println(token.getToken());
                jwtTokenRepository.delete(token);
            }
            String jwt = jwtService.generateToken(user);
            String refresh = jwtService.generateRefreshToken(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),user.getPassword());

            JwtToken savedToken = jwtTokenRepository.save(jwtToken(jwt,refresh,user));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            HashMap<String,String> access = new HashMap<>();
            access.put("jwt-token",savedToken.getToken());
            access.put("refresh-token",savedToken.getRefreshToken());
            return ResponseFromUser.builder()
                    .activate(savedToken.getUser().isActivate())
                    .responseAccess(access)
                    .username(savedToken.getUser().getUser_name())
                    .email(savedToken.getUser().getEmail())
                    .build();
        }else{
           throw  new UserNotEnabledException("USER NOT ENABLED");
        }
    }
    private JwtToken jwtToken(String jwt,String refresh,User user){
       return JwtToken.builder()
                .token(jwt)
                .refreshToken(refresh)
                .user(user)
                .generatedAt(new Date(System.currentTimeMillis()))
                .expiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .build();
    }

        @Override
        public Boolean verify_token(String token) {
            Optional<JwtToken> optionalJwtToken = jwtTokenRepository.findByToken(token);
            if (optionalJwtToken.isPresent()) {
                JwtToken jwtToken = optionalJwtToken.get();
                return DateUtils.isExpired(jwtToken.getExpiresAt());
            } else {
                return false;
            }
        }

    @Override
    public String resend_link_password(String email) {
        User user = verifyUser(email);
        return generatingOtp(user);
    }

    @SneakyThrows
    @Override
    public String RegisterRequest(RegisterRequest registerRequest) {
        confirmUser(registerRequest.getEmail());
        User saveUser =userRepository.save(userRequestDTOObject(registerRequest));
        generatingOtp(saveUser);
        return "Registered successful";
    }
    private User userRequestDTOObject(RegisterRequest registerRequest){
        return User.builder()
                .activate(false)
                .email(registerRequest.getEmail())
                .user_name(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .profile("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")
                .role(Role.USER)
                .build();
    }

    private String generatingOtp(User user)  {
        String otp = RandomValues.generateRandom().substring(0,4);
        OTPConfirmation confirmationToken = new OTPConfirmation(otp, user);
        confirmEmailMessage.sendOtp(user,otp,confirmationToken);
        return otp;
    }

    public User verifyUser(String email){
        return  userRepository.findByEmail(email).orElseThrow(()-> new UserNotFoundException("USER NOT FOUND"));
    }
    private void confirmUser(String email){
          if(userRepository.existsByEmail(email)){
              throw new UserNotEnabledException("USER IS ENABLED");
          }
    }

    public Optional<User> verifyUserAndGetEmail(){
        return userRepository.findByEmail(UserUtils.getAccessTokenEmail());
    }
    public User findById(Long id){
        return userRepository.findById(id)
                .orElseThrow(()->new UserNotFoundException("USER NOT FOUND"));
    }



}
