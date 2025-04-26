package com.altloc.backend.service;

import com.altloc.backend.store.entities.UserEntity;
import com.altloc.backend.store.entities.auth.EmailActivationEntity;
import com.altloc.backend.store.repositories.auth.EmailActivationRepository;
import com.altloc.backend.store.repositories.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class EmailActivationService {

    private final EmailActivationRepository emailActivationRepository;
    private final UserRepository userRepository;

    /**
     * Creates an activation token and saves it to the database.
     * 
     * @param user the user for whom the activation token is generated
     * @return the activation token
     * @throws MessagingException
     */
    public String createActivationForUser(UserEntity user) throws MessagingException {
        // Generate activation token
        String token = UUID.randomUUID().toString();

        // Create the activation entity
        EmailActivationEntity activation = EmailActivationEntity.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(10)) // Token expiration time - 10 minutes
                .build();

        // Save the activation to the database
        emailActivationRepository.save(activation);

        return token;
    }

    /**
     * Activates the user by the token.
     * 
     * @param token the activation token
     * @return
     */
    @Transactional
    public boolean activateUserByToken(String token) {
        // Find activation by token
        EmailActivationEntity activation = emailActivationRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Invalid or expired activation token"));

        // Check if the token has expired
        if (activation.getExpiryDate().isBefore(LocalDateTime.now())) {
            emailActivationRepository.delete(activation); // Delete expired token
            throw new ResponseStatusException(GONE, "Activation token expired");
        }

        // Get the user associated with the token
        UserEntity user = activation.getUser();

        // Check if the user's email is already verified
        if (user.getEmailVerified()) {
            throw new ResponseStatusException(CONFLICT, "Email is already verified");
        }

        // Mark the user's email as verified
        user.setEmailVerified(true);

        // Save the updated user to the database
        userRepository.save(user);

        // Delete the activation token as it's no longer needed
        emailActivationRepository.delete(activation);

        return true;
    }
}
