package com.altloc.backend.api.app.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.altloc.backend.api.app.controller.helpers.ControllerHelper;
import com.altloc.backend.api.app.dto.HabitDto;
import com.altloc.backend.api.app.dto.HabitRequest;
import com.altloc.backend.api.app.dto.ResponseDto;
import com.altloc.backend.api.app.factories.HabitDtoFactory;
import com.altloc.backend.exception.BadRequestException;
import com.altloc.backend.store.entities.app.DomainEntity;
import com.altloc.backend.store.entities.app.HabitEntity;
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

    public static final String FETCH_DOMAIN_HABITS = "/domain/{domain_id}/habits";
    public static final String FETCH_HABITS = "/habits";
    public static final String CREATE_OR_UPDATE_HABIT = "/domain/habit";
    public static final String DELETE_HABIT = "/habit/{habit_id}";

    @GetMapping(FETCH_HABITS)
    public List<HabitDto> getHabits() {
        return habitRepository
                .findAll()
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

        final HabitEntity savedDomain = habitRepository.saveAndFlush(habitEntity);
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

}
