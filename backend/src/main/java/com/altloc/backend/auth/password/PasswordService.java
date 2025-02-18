package com.altloc.backend.auth.password;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.altloc.backend.entity.UserEntity;
import com.altloc.backend.entity.UserEntity.AuthMethod;
import com.altloc.backend.entity.PasswordEntity;
import com.altloc.backend.exception.UserAlreadyExistException;
import com.altloc.backend.model.RegistrationDTO;
import com.altloc.backend.repository.UserRepository;
import com.altloc.backend.repository.PasswordRepository;

@Service
public class PasswordService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordRepository passwordRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public RegistrationDTO registration(RegistrationDTO user) throws UserAlreadyExistException {
        // Check if user already exists
        if (userRepo.findByUsername(user.getUsername()) != null) {
            throw new UserAlreadyExistException("User already exists");
        }

        // Convert DTO -> Entity
        UserEntity userEntity = mapToEntity(user);

        // Save user to database
        UserEntity savedUser = userRepo.save(userEntity);

        // Create password account
        PasswordEntity passwordAccount = createPasswordAccount(savedUser, user.getPassword());
        passwordRepo.save(passwordAccount);

        // Convert Entity -> DTO
        return mapToDTO(savedUser);
    }

    private PasswordEntity createPasswordAccount(UserEntity user, String rawPassword) {
        PasswordEntity passwordEntity = new PasswordEntity();
        passwordEntity.setUser(user); // Join with user entity
        passwordEntity.setPassword(passwordEncoder.encode(rawPassword)); // Encode password
        return passwordEntity;
    }

    private UserEntity mapToEntity(RegistrationDTO dto) {
        UserEntity entity = new UserEntity();
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setEmailVerified(false);
        entity.setRole("USER");
        entity.setScore(0);
        entity.setLevel(1);
        entity.setCurrency(0);
        entity.setAuthMethods(
                List.of(AuthMethod.PASSWORD));
        return entity;
    }

    private RegistrationDTO mapToDTO(UserEntity entity) {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        return dto;
    }
}
