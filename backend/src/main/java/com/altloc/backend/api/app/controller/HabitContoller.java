package com.altloc.backend.api.app.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    public static final String GET_HABITS = "/domain/{domain_id}/habits";
    public static final String CREATE_HABIT = "/domain/{domain_id}/habits";
    public static final String UPDATE_HABIT = "/habit/{habit_id}";
    public static final String DELETE_HABIT = "/habit/{habit_id}";

    @GetMapping(GET_HABITS)
    public List<HabitDto> getHabits(
            @PathVariable(name = "domain_id") String domainId) {

        DomainEntity domain = controllerHelper.getDomainOrThrowException(domainId);

        return domain
                .getHabits()
                .stream()
                .map(habitDtoFactory::createHabitDto)
                .collect(Collectors.toList());

    }

    @PostMapping(CREATE_HABIT)
    public HabitDto createHabit(
            @PathVariable(name = "domain_id") String domainId,
            @RequestBody HabitRequest habitRequest) {

        DomainEntity domain = controllerHelper.getDomainOrThrowException(domainId);

        if (domain == null) {
            throw new BadRequestException("Domain with id " + domainId + " not found.");
        }

        if (habitRequest.getName().trim().isEmpty()) {
            throw new BadRequestException("Habit name cannot be empty.");
        }

        final HabitEntity savedHabit = habitRepository.saveAndFlush(
                HabitEntity.builder()
                        .name(habitRequest.getName())
                        .domainId(domainId)
                        .build());

        return habitDtoFactory.createHabitDto(savedHabit);

    }

    @PatchMapping(UPDATE_HABIT)
    public HabitDto updateDomain(
            @PathVariable(name = "habit_id") String habitId,
            @RequestBody HabitRequest habitRequest) {

        if (habitRequest.getName().trim().isEmpty()) {
            throw new BadRequestException("Habit name cannot be empty.");
        }

        HabitEntity habitEntity = controllerHelper.getHabitOrThrowException(habitId);

        habitEntity.setName(habitRequest.getName());

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
