package com.altloc.backend.api.app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.altloc.backend.api.app.dto.dailyComment.DailyCommentDto;
import com.altloc.backend.api.app.factories.DailyCommentDtoFactory;
import com.altloc.backend.model.UserDetailsImpl;
import com.altloc.backend.store.repositories.app.DailyCommentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@RequestMapping("/app")
@Transactional
@RestController
public class DailyCommentContoller {

    private final DailyCommentDtoFactory dailyCommentDtoFactory;
    private final DailyCommentRepository dailyCommentRepository;

    public static final String FETCH_DAILY_COMMENTS = "/daily-comments";
    public static final String FETCH_DAILY_COMMENT = "/daily-comment/{daily_comment_id}";
    public static final String CREATE_OR_UPDATE_DAILY_COMMENT = "/daily-comment";
    public static final String DELETE_DAILY_COMMENT = "/daily-comment/{daily_comment_id}";

    @GetMapping(FETCH_DAILY_COMMENTS)
    public List<DailyCommentDto> getDailyComments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        return dailyCommentRepository
                .findAllByUserId(user.getId())
                .stream()
                .map(dailyCommentDtoFactory::createDailyCommentDto)
                .toList();
    }

    // @DeleteMapping(DELETE_HABIT)
    // public ResponseDto deleteHabit(
    // @PathVariable("habit_id") String habitId) {
    // {
    // controllerHelper.getHabitOrThrowException(habitId);

    // habitRepository.deleteById(habitId);

    // return ResponseDto.makeDefault(true);

    // }

}
