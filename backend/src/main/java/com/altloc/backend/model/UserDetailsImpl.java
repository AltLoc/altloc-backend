package com.altloc.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.altloc.backend.store.entities.UserEntity;
import com.altloc.backend.store.enums.Role;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final String id;
    private final String username;
    private final String email;
    private final Boolean emailVerified;
    private final String password;
    private final String googleId;
    private final Role role;
    private final int score;
    private final int level;
    private final int currency;
    private final String avatarKey;

    public static UserDetailsImpl build(UserEntity user) {
        // String hashedPassword = null;
        // if (user.getPasswordAccount() != null) {
        // hashedPassword = user.getPasswordAccount().getPasswordHashed();
        // }
        // Проверка на наличие пароля
        String hashedPassword = user.getPasswordAccount() != null ? user.getPasswordAccount().getPasswordHashed()
                : null;

        // Проверка на наличие Google аккаунта
        String googleId = user.getGoogleAccount() != null ? user.getGoogleAccount().getGoogleId() : null;

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getEmailVerified(),
                // user.getPasswordAccount().getPasswordHashed(),
                hashedPassword,
                // user.getGoogleAccount().getGoogleId(),
                // user.getGoogleAccount() != null ? user.getGoogleAccount().getGoogleId() :
                // null,
                googleId,
                user.getRole(),
                user.getScore(),
                user.getLevel(),
                user.getCurrency(),
                user.getAvatarKey());

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Пока возвращаем пустой список, если роли не используются
        // return Collections.emptyList();
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
