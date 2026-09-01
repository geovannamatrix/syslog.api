package com.syslog.api.service;

import com.syslog.api.exception.BadRequestException;
import com.syslog.api.model.dtos.UserRequestDTO;
import com.syslog.api.model.dtos.UserResponseDTO;
import com.syslog.api.model.dtos.UserUpdateRequestDTO;
import com.syslog.api.model.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.syslog.api.model.mapper.UserMapper.USER_MAPPER;

@Service
@AllArgsConstructor
public class UserService {

    private final EncryptPasswordService encryptPasswordService;
    private final UserRepository repository;

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {
        validateEmail(request.getEmail().toLowerCase());
        return save(request);
    }

    public void validateEmail(String email) {
        validateEmailDoesNotExistsOnDatabase(email);
    }

    public void validateEmailDoesNotExistsOnDatabase(String email) {
        if (isEmailOnDatabase(email)) {
            throw new BadRequestException("Sent Email already has a registered password");
        }
    }

    public boolean isEmailOnDatabase(String email) {
        return repository.findByEmail(email).isPresent();
    }

    public UserResponseDTO save(UserRequestDTO request) {
        var password = encryptPasswordService.encryptPassword(request.getPassword());
        request.setPassword(password);
        var user = USER_MAPPER.toEntity(request);
        Long userId;
        try {
            userId = repository.insertAndReturnId(user);
        } catch (DuplicateKeyException exception) {
            throw new BadRequestException("Sent Email already has a registered password");
        }
        return new UserResponseDTO(userId);
    }

    @Transactional
    public void updateUser(String email, UserUpdateRequestDTO request) {
        var user = repository.findByEmail(email);
        if (user.isEmpty()) {
            throw new BadRequestException("User not registered");
        }

        var password = validatePasswordToUpdate(request.getPassword());
        request.setPassword(password);
        var userUpdate = USER_MAPPER.toUpdateEntity(user.get(), request);

        repository.updateUser(userUpdate);
    }

    private String validatePasswordToUpdate(String password) {
        if (password != null && !password.isEmpty()) {
            password = encryptPassword(password);
        }
        return password;
    }

    private String encryptPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new BadRequestException("Password cannot be null or empty");
        }
        return encryptPasswordService.encryptPassword(password);
    }

    @Transactional
    public void deleteUser(Long id) {
        var user = repository.findById(id);
        if (user.isEmpty()) {
            throw new BadRequestException("User not found");
        }
        repository.delete(id);
    }
}
