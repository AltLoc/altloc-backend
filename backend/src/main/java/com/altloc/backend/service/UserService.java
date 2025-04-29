package com.altloc.backend.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.altloc.backend.model.UserDetailsImpl;
import com.altloc.backend.store.entities.UserEntity;
import com.altloc.backend.store.repositories.UserRepository;

import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // @Autowired
    // public void setUserRepository(UserRepository userRepository) {
    // this.userRepository = userRepository;
    // }

    // email is used instead of username
    // @Override
    // public UserDetails loadUserByUsername(String username) throws
    // UsernameNotFoundException {

    // UserEntity user = userRepository.findByEmail(username)
    // .orElseThrow(() -> new UsernameNotFoundException(
    // String.format("User with email %s not found", username)));

    // return UserDetailsImpl.build(user);
    // }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user by email: {}", username);

        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    logger.error("User with email {} not found", username);
                    return new UsernameNotFoundException(
                            String.format("User with email %s not found", username));
                });

        return UserDetailsImpl.build(user);
    }

    // public UserDetails loadUserByEmail(String email) throws
    // EmailNotFoundException {
    // UserEntity user = userRepository.findByEmail(email)
    // .orElseThrow(() -> new EmailNotFoundException(
    // String.format("User with email %s not found", email)));

    // return UserDetailsImpl.build(user);
    // }

}
