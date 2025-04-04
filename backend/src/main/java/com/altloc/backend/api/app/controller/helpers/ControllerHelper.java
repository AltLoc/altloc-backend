package com.altloc.backend.api.app.controller.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.altloc.backend.store.entities.app.DomainEntity;
import com.altloc.backend.store.entities.app.HabitEntity;
import com.altloc.backend.store.entities.app.IdentityMatrixEntity;
import com.altloc.backend.store.repositories.app.DomainRepository;
import com.altloc.backend.store.repositories.app.HabitRepository;
import com.altloc.backend.store.repositories.app.IdentityMatrixRepository;
import com.altloc.backend.exception.NotFoundException;

import jakarta.transaction.Transactional;

@RequiredArgsConstructor
@Component
@Transactional
public class ControllerHelper {

    private final IdentityMatrixRepository identityMatrixRepository;
    private final DomainRepository domainRepository;
    private final HabitRepository habitRepository;

    public IdentityMatrixEntity getIdentityMatrixOrThrowException(String identityMatrixId) {

        return identityMatrixRepository
                .findById(identityMatrixId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(
                                "Identity Matrix with \"%s\" doesn't exist.",
                                identityMatrixId)));
    }

    public DomainEntity getDomainOrThrowException(String domainId) {

        return domainRepository
                .findById(domainId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(
                                "Domain with \"%s\" id doesn't exist.",
                                domainId)));
    }

    public HabitEntity getHabitOrThrowException(String habitId) {

        return habitRepository
                .findById(habitId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(
                                "Habit with \"%s\" id doesn't exist.",
                                habitId)));
    }
}