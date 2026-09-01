package com.syslog.api.service;

import com.syslog.api.exception.BadRequestException;
import com.syslog.api.exception.NotFoundException;
import com.syslog.api.model.dtos.ChangePasswordRequestDTO;
import com.syslog.api.model.dtos.CredentialDTO;
import com.syslog.api.model.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.syslog.api.model.mapper.UserMapper.USER_MAPPER;

@Service
@Slf4j
@AllArgsConstructor
public class CredentialService {

    private final EncryptPasswordService encryptPasswordService;
    private final UserRepository repository;

    public void authenticate(String email, String password) {
        CredentialDTO credential = validateEmailOnDatabase(email);
        if (!isPasswordValid(password, credential)) {
            throw new BadRequestException("Username or Password are not Valid");
        }
    }
    public boolean isPasswordValid(String password, CredentialDTO credential) {
        return encryptPasswordService.isPasswordValid(credential.getPassword(), password);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequestDTO request) {
        CredentialDTO credential = validateEmailOnDatabase(email);
        if (!isChangePasswordRequestValid(credential, request.getPassword())) {
            throw new BadRequestException("Username or Password are not Valid");
        }
        updatePassword(credential, request.getNewPassword());
    }

    public boolean isChangePasswordRequestValid(CredentialDTO credential, String password) {
        boolean isPasswordValid = password != null && isPasswordValid(password, credential);
        if (!isPasswordValid) {
            log.info("{} Credentials are Invalid or Not Sent", credential.getId());
        }
        return isPasswordValid;
    }

    public CredentialDTO validateEmailOnDatabase(String email) {
        var credential = repository.findByEmail(email);
        if (credential.isEmpty()) {
            throw new NotFoundException("Email not found on Database");
        }
        return USER_MAPPER.toModel(credential.get());
    }

    public void updatePassword(CredentialDTO credential, String newPassword) {
        credential.setPassword(encryptPasswordService.encryptPassword(newPassword));
        repository.updatePassword(credential.getId(), credential.getPassword());
    }
}
