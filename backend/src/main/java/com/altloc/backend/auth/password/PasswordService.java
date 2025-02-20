package com.altloc.backend.auth.password;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.altloc.backend.exception.UserAlreadyExistException;
import com.altloc.backend.model.RegistrationDTO;
import com.altloc.backend.store.entity.PasswordEntity;
import com.altloc.backend.store.entity.UserEntity;
import com.altloc.backend.store.repository.PasswordRepository;
import com.altloc.backend.store.repository.UserRepository;

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
        passwordEntity.setUser(user);
        passwordEntity.setPasswordHashed(passwordEncoder.encode(rawPassword));

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
        return entity;
    }

    private RegistrationDTO mapToDTO(UserEntity entity) {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        return dto;
    }
}
