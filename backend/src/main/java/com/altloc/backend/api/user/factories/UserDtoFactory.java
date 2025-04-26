package com.altloc.backend.api.user.factories;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.user.dto.UserDto;
import com.altloc.backend.store.entities.UserEntity;
import com.altloc.backend.store.repositories.app.IdentityMatrixRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserDtoFactory {

    private final IdentityMatrixRepository identityMatrixRepository;

    public UserDto createUserDto(UserEntity entity) {
        return UserDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .emailVerified(entity.getEmailVerified())
                .avatarKey(entity.getAvatarKey())
                .currency(entity.getCurrency())
                .level(entity.getLevel())
                .score(entity.getScore())
                .hasMatrix(identityMatrixRepository.existsByUserId(entity.getId()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

}
