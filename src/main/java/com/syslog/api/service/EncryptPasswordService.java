package com.syslog.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EncryptPasswordService {

    private final PasswordEncoder encoder =
            PasswordEncoderFactories.createDelegatingPasswordEncoder();

    public String encryptPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean isPasswordValid(String existingPassword, String sentPassword) {
        return encoder.matches(sentPassword, existingPassword);
    }

    public boolean needsRehash(String existingPassword) {
        return encoder.upgradeEncoding(existingPassword);
    }
}
