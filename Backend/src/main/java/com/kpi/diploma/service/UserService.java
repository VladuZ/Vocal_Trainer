package com.kpi.diploma.service;

import com.kpi.diploma.entity.User;
import com.kpi.diploma.exception.ObjAlreadyExistsException;
import com.kpi.diploma.model.*;
import com.kpi.diploma.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse register(RegistrationRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new ObjAlreadyExistsException("Username is already in use");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ObjAlreadyExistsException("Email is already in use");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        userRepository.save(user);

        return login(new LoginRequest(request.email(), request.password()));
    }

    public LoginResponse login(LoginRequest request) {
        String username = userRepository.findUsernameByEmail(request.email()).orElseThrow(() -> new BadCredentialsException("Invalid email"));
        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.password()));
        User user = (User) authentication.getPrincipal();
        String jwt = jwtService.generateToken(username);
        return new LoginResponse(request.email(), username, jwt);
    }

    public void delete() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userRepository.delete(user);
    }

    @Transactional
    public ChangeResponse changeUsername(String login, String username) {
        return changeUser(login, ChangeInfo.USERNAME, username);
    }

    @Transactional
    public ChangeResponse changeEmail(String email, String username) {
        return changeUser(email, ChangeInfo.EMAIL, username);
    }

    @Transactional
    public ChangeResponse changePassword(String password, String username) {
        return changeUser(password, ChangeInfo.PASSWORD, username);
    }

    protected ChangeResponse changeUser(String newValue, ChangeInfo type, String username) {
        if (newValue == null || newValue.isEmpty()) {
            throw new BadCredentialsException("Empty parameter.");
        }
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        String token = "";
        switch (type) {
            case EMAIL -> user.setEmail(newValue);
            case PASSWORD -> {
                user.setPassword(passwordEncoder.encode(newValue));
                newValue = "";
            }
            case USERNAME -> {
                user.setUsername(newValue);
                token = jwtService.generateToken(newValue);
            }
            default -> throw new RuntimeException("Unsupported change type");
        }
        userRepository.save(user);
        return new ChangeResponse(newValue, token);
    }
}
