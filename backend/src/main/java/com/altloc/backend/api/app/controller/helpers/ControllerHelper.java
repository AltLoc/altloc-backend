package com.altloc.backend.api.app.controller.helpers;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.altloc.backend.store.entities.app.DailyCommentEntity;
import com.altloc.backend.store.entities.app.DomainEntity;
import com.altloc.backend.store.entities.app.HabitEntity;
import com.altloc.backend.store.entities.app.IdentityMatrixEntity;
import com.altloc.backend.store.repositories.app.DailyCommentRepository;
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
    private final DailyCommentRepository dailyCommentRepository;

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

    public DailyCommentEntity getDailyCommentOrThrowException(
            String dailyCommentId) {

        return dailyCommentRepository
                .findById(dailyCommentId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(
                                "Daily comment with \"%s\" id doesn't exist.",
                                dailyCommentId)));
    }

    public Instant getStartOfTodayUtc() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return today.atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}