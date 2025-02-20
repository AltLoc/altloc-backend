package com.altloc.backend.auth.password;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import com.altloc.backend.model.RegistrationDTO;
import com.altloc.backend.service.UserService;
import com.altloc.backend.store.entity.PasswordEntity;
import com.altloc.backend.store.entity.UserEntity;
import com.altloc.backend.store.repository.UserRepository;
import com.altloc.backend.config.SecurityConfig;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@RestController
@RequestMapping("/auth/password")
public class PasswordController {

    @Lazy
    @Autowired
    private UserRepository userRepository;

    @Lazy
    @Autowired
    private SecurityConfig securityConfig;

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;

    @Lazy
    @Autowired
    private UserService userService;

    @PostMapping("/registration")
    public ResponseEntity<?> registration(
            @Valid @RequestBody RegistrationDTO request) {

        System.out.println("DEBUG user data: " + request);
        try {
            // Проверка на существование пользователя
            if (userRepository.existsUserByUsername(request.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose a different username");
            }
            if (userRepository.existsUserByEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose a different email");
            }

            // Создаем нового пользователя
            UserEntity user = UserEntity.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .emailVerified(false)
                    .role("USER") // Установите роль по умолчанию
                    .score(0) // Установите начальные значения
                    .level(1)
                    .avatarKey("")
                    .currency(0)
                    .build();

            // Создаем запись пароля
            PasswordEntity passwordEntity = PasswordEntity.builder()
                    .user(user) // Связываем с пользователем
                    .passwordHashed(securityConfig.passwordEncoder().encode(request.getPassword()))
                    .build();
            System.out.println("DEBUG user password: " + passwordEntity);

            // Связываем пользователя с записью пароля
            user.setPasswordAccount(passwordEntity);

            // Сохраняем пользователя (каскадное сохранение также сохранит пароль)
            userRepository.save(user);
            System.out.println("Debug user data:" + user);
            return ResponseEntity.ok("User successfully registered");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something went wrong");
        }
    }

}
