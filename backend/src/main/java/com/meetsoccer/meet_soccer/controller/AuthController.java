package com.meetsoccer.meet_soccer.controller;

import com.meetsoccer.meet_soccer.dto.request.LoginRequestDTO;
import com.meetsoccer.meet_soccer.dto.request.RegisterRequestDTO;
import com.meetsoccer.meet_soccer.dto.response.JwtResponse;
import com.meetsoccer.meet_soccer.dto.response.MessageResponse;
import com.meetsoccer.meet_soccer.model.Player;
import com.meetsoccer.meet_soccer.model.Referee;
import com.meetsoccer.meet_soccer.repository.PlayerRepository;
import com.meetsoccer.meet_soccer.repository.RefereeRepository;
import com.meetsoccer.meet_soccer.security.jwt.JwtUtils;
import com.meetsoccer.meet_soccer.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    RefereeRepository refereeRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);


        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();


        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                roles.get(0)));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO signUpRequest) {
        if (signUpRequest.getRole().name() == "ROLE_PLAYER") {
            if (playerRepository.existsByEmail(signUpRequest.getEmail()) || refereeRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Username is already taken!"));
            }

            if (playerRepository.existsByEmail(signUpRequest.getEmail()) || refereeRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is already in use!"));
            }

            // Create new user's account
            Player user = new Player(
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()),
                    signUpRequest.getName(),
                    signUpRequest.getLastName(),
                    signUpRequest.getAge(),
                    signUpRequest.getPosition());
            playerRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        }
        if (signUpRequest.getRole().name() == "ROLE_REFEREE") {
            if (refereeRepository.existsByEmail(signUpRequest.getEmail()) || playerRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Username is already taken!"));
            }

            if (refereeRepository.existsByEmail(signUpRequest.getEmail()) || playerRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is already in use!"));
            }

            // Create new user's account
            Referee user = new Referee(
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()),
                    signUpRequest.getName(),
                    signUpRequest.getLastName(),
                    signUpRequest.getExperience());
            refereeRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        }
        return ResponseEntity.ok(new MessageResponse("Failed to register user!"));
    }
}
