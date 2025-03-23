package com.altloc.backend.api.user.factories;

import org.springframework.stereotype.Component;

import com.altloc.backend.api.user.dto.UserDto;
import com.altloc.backend.store.entities.UserEntity;

@Component
public class UserDtoFactory {
    public UserDto createUserDto(UserEntity entity) {
        return UserDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .emailVerified(entity.isEmailVerified())
                .avatarKey(entity.getAvatarKey())
                .currency(entity.getCurrency())
                .level(entity.getLevel())
                .score(entity.getScore())
                .createdAt(entity.getCreatedAt())
                .build();
    }

}
