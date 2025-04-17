package com.altloc.backend.api.app.controller;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.altloc.backend.api.app.controller.helpers.ControllerHelper;
import com.altloc.backend.api.app.dto.ResponseDto;
import com.altloc.backend.api.app.dto.habit.HabitDto;
import com.altloc.backend.api.app.dto.habit.HabitRequest;
import com.altloc.backend.api.app.factories.HabitDtoFactory;
import com.altloc.backend.api.app.factories.CompletedHabitDtoFactory;
import com.altloc.backend.exception.BadRequestException;
import com.altloc.backend.model.UserDetailsImpl;
import com.altloc.backend.store.entities.app.CompletedHabitEntity;
import com.altloc.backend.store.entities.app.DomainEntity;
import com.altloc.backend.store.entities.app.HabitEntity;
import com.altloc.backend.store.enums.DayPart;
import com.altloc.backend.store.repositories.app.CompletedHabitRepository;
import com.altloc.backend.store.repositories.app.HabitRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/app")
@Transactional
@RestController
public class HabitContoller {

    private final HabitDtoFactory habitDtoFactory;
    private final HabitRepository habitRepository;
    private final ControllerHelper controllerHelper;
    private final CompletedHabitRepository completedHabitRepository;
    private final CompletedHabitDtoFactory completedHabitDtoFactory;

    public static final String FETCH_DOMAIN_HABITS = "/domain/{domain_id}/habits";
    public static final String FETCH_HABITS = "/habits";
    public static final String FETCH_DAY_PART_HABITS = "/habits/day-part/{day_part}";
    public static final String FETCH_HABIT = "/habit/{habit_id}";
    public static final String CREATE_OR_UPDATE_HABIT = "/domain/habit";
    public static final String DELETE_HABIT = "/habit/{habit_id}";
    public static final String COMPLETED_HABIT = "/habit/{habit_id}/completed";
    public static final String STATS_HABIT = "/habit/{habit_id}/stats";

    @GetMapping(FETCH_HABIT)
    public HabitDto getHabit(
            @PathVariable("habit_id") String habitId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        HabitEntity habitEntity = controllerHelper.getHabitOrThrowException(habitId);

        if (!habitEntity.getUserId().equals(user.getId())) {
            throw new BadRequestException("You don't have access to this habit.");
        }

        return habitDtoFactory.createHabitDto(habitEntity);
    }

    @GetMapping(FETCH_HABITS)
    public List<HabitDto> getHabits() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        return habitRepository
                .findAllByUserId(user.getId())
                .stream()
                .map(habitDtoFactory::createHabitDto)
                .collect(Collectors.toList());
    }

    @GetMapping(FETCH_DAY_PART_HABITS)
    public List<HabitDto> getDayPartHabits(
            @PathVariable(name = "day_part") DayPart dayPart) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        return habitRepository
                .findAllByDayPartAndUserId(dayPart, user.getId())
                .stream()
                .map(habitDtoFactory::createHabitDto)
                .collect(Collectors.toList());
    }

    @GetMapping(FETCH_DOMAIN_HABITS)
    public List<HabitDto> fetchHabits(
            @PathVariable(name = "domain_id") String domainId) {

        DomainEntity domain = controllerHelper.getDomainOrThrowException(domainId);

        return domain
                .getHabits()
                .stream()
                .map(habitDtoFactory::createHabitDto)
                .collect(Collectors.toList());

    }

    @PutMapping(CREATE_OR_UPDATE_HABIT)
    public HabitDto createOrUpdateHabit(@RequestBody HabitRequest habitRequest) {

        Optional<String> optionalHabitId = Optional.ofNullable(habitRequest.getId());
        Optional<String> optionalHabitName = Optional.ofNullable(habitRequest.getName());
        Optional<String> optionalDomainId = Optional.ofNullable(habitRequest.getDomainId());

        boolean isCreate = optionalDomainId.isEmpty();

        if (isCreate && optionalHabitName.isEmpty()) {
            throw new BadRequestException("Habit name can't be empty.");
        }

        if (optionalDomainId.isEmpty()) {
            throw new BadRequestException("Domain ID is required.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        DomainEntity domain = controllerHelper
                .getDomainOrThrowException(optionalDomainId.get());

        final HabitEntity habitEntity = optionalHabitId
                .map(controllerHelper::getHabitOrThrowException)
                .orElseGet(() -> HabitEntity.builder()
                        .domainId(optionalDomainId.get())
                        .build());

        optionalHabitName.ifPresent(habitName -> {
            habitRepository
                    .findHabitEntityByDomainIdAndNameContainsIgnoreCase(domain.getId(), habitName)
                    .filter(existingHabit -> !existingHabit.getId().equals(habitEntity.getId()))
                    .ifPresent(existingDomain -> {
                        throw new BadRequestException(String.format("Habit \"%s\" already exists.", habitName));
                    });

            habitEntity.setName(habitName);
        });

        final HabitEntity savedDomain = habitRepository.saveAndFlush(
                HabitEntity.builder()
                        .name(habitRequest.getName())
                        .domainId(habitRequest.getDomainId())
                        .runtime(habitRequest.getRuntime())
                        .dayPart(habitRequest.getDayPart())
                        .targetNumberOfCompletions(
                                habitRequest.getTargetNumberOfCompletions())

                        .userId(user.getId())
                        .build());
        return habitDtoFactory.createHabitDto(savedDomain);
    }

    @DeleteMapping(DELETE_HABIT)
    public ResponseDto deleteHabit(
            @PathVariable("habit_id") String habitId) {
        {
            controllerHelper.getHabitOrThrowException(habitId);

            habitRepository.deleteById(habitId);

            return ResponseDto.makeDefault(true);

        }
    }

    @PutMapping(COMPLETED_HABIT)
    public ResponseDto completeHabit(
            @PathVariable("habit_id") String habitId) {
        {
            HabitEntity habitEntity = controllerHelper.getHabitOrThrowException(habitId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

            Instant todayStart = controllerHelper.getStartOfTodayUtc();

            boolean alreadyCompleted = completedHabitRepository
                    .findFirstByHabitIdAndUserIdAndCompletedAtAfter(habitId, user.getId(), todayStart)
                    .isPresent();

            if (alreadyCompleted) {
                throw new BadRequestException("Habit already completed today.");
            }

            habitEntity.setNumberOfCompletions(habitEntity.getNumberOfCompletions() + 1);

            final CompletedHabitEntity completedHabitSaved = completedHabitRepository.saveAndFlush(
                    CompletedHabitEntity.builder()

                            .habitId(habitId)
                            .userId(user.getId())
                            .build());

            completedHabitDtoFactory.createCompletedHabitDto(completedHabitSaved);

            return ResponseDto.makeDefault(true);
        }
    }
}
