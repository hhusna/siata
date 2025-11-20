package siata.siata.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import siata.siata.dto.JwtAuthenticationResponse;
import siata.siata.dto.LoginRequest;
import siata.siata.dto.RegisterRequest;
import siata.siata.entity.Pegawai;
import siata.siata.entity.User;
import siata.siata.security.JwtTokenProvider;
import siata.siata.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider tokenProvider;

    @Autowired
    UserService userService;

    @GetMapping("/pegawai")
    private Pegawai getPegawaiFromAuth(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getPegawai();
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, user.getUsername(), user.getRole()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        User createdUser = userService.registerUser(request);
        return ResponseEntity.ok("User " + createdUser.getUsername() + " registered successfully");
    }


}