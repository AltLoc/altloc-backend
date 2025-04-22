package com.altloc.backend.api.app.service;

import com.altloc.backend.store.entities.UserEntity;
import com.altloc.backend.store.entities.app.CompletedHabitEntity;
import com.altloc.backend.store.entities.app.HabitRewardEntity;
import com.altloc.backend.store.repositories.UserRepository;
import com.altloc.backend.store.repositories.app.HabitRewardRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HabitRewardService {

    private final HabitRewardRepository habitRewardRepository;
    private final UserRepository userRepository;

    public void rewardForCompletedHabit(CompletedHabitEntity completedHabit) {
        UserEntity user = completedHabit.getUser();

        int experience = 4;
        int crystals = 1;
        int baseLevelScore = 8;

        int updatedScore = user.getScore() + experience;
        int nextLevelThreshold = (user.getLevel() + 1) * baseLevelScore;

        if (updatedScore >= nextLevelThreshold) {
            user.setLevel(user.getLevel() + 1);
            user.setScore(updatedScore - nextLevelThreshold);
        } else {
            user.setScore(updatedScore);
        }

        user.setCurrency(user.getCurrency() + crystals);

        userRepository.save(user);

        HabitRewardEntity reward = HabitRewardEntity.builder()
                .completedHabit(completedHabit)
                .experience(experience)
                .crystals(crystals)
                .build();

        habitRewardRepository.save(reward);
    }
}
