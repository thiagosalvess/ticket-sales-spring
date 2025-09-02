package com.thiagosalvess.ticketsales.features.authentication.controller;

import com.thiagosalvess.ticketsales.features.authentication.dto.TokenJWTResponse;
import com.thiagosalvess.ticketsales.features.authentication.dto.UserAuthenticationDto;
import com.thiagosalvess.ticketsales.features.authentication.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/login")
public class AuthController {
    private AuthenticationManager manager;

    private TokenService tokenService;

    public AuthController(AuthenticationManager manager, TokenService tokenService) {
        this.manager = manager;
        this.tokenService = tokenService;
    }

    @PostMapping
    public ResponseEntity login(@RequestBody @Valid UserAuthenticationDto dto) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        var authentication = manager.authenticate(authenticationToken);

        var tokenJWT = tokenService.generateToken(authentication);

        return ResponseEntity.ok(new TokenJWTResponse(tokenJWT));
    }

}
