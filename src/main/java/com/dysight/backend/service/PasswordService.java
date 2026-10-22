package com.dysight.backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public String encriptar(String password) {
        return encoder.encode(password);
    }

    public boolean verificar(
            String password,
            String passwordEncriptada
    ) {
        return encoder.matches(
                password,
                passwordEncriptada
        );
    }
}