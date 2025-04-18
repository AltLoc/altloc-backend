package com.altloc.backend.api.app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.altloc.backend.api.app.controller.helpers.ControllerHelper;
import com.altloc.backend.api.app.dto.ResponseDto;
import com.altloc.backend.api.app.dto.dailyComment.DailyCommentDto;
import com.altloc.backend.api.app.dto.dailyComment.DailyCommentRequest;
import com.altloc.backend.api.app.factories.DailyCommentDtoFactory;
import com.altloc.backend.exception.BadRequestException;
import com.altloc.backend.exception.ForbiddenException;
import com.altloc.backend.model.UserDetailsImpl;
import com.altloc.backend.store.entities.app.DailyCommentEntity;
import com.altloc.backend.store.enums.Mood;
import com.altloc.backend.store.repositories.app.DailyCommentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RequestMapping("/app")
@Transactional
@RestController
public class DailyCommentContoller {

    private final DailyCommentDtoFactory dailyCommentDtoFactory;
    private final DailyCommentRepository dailyCommentRepository;
    private final ControllerHelper controllerHelper;

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

    @GetMapping(FETCH_DAILY_COMMENT)
    public DailyCommentDto getDailyComment(
            @PathVariable("daily_comment_id") String dailyCommentId) {
        DailyCommentEntity dailyCommentEntity = controllerHelper.getDailyCommentOrThrowException(dailyCommentId);
        return dailyCommentDtoFactory.createDailyCommentDto(dailyCommentEntity);
    }

    @PutMapping(CREATE_OR_UPDATE_DAILY_COMMENT)
    public DailyCommentDto createOrUpdateDailyComment(@RequestBody DailyCommentRequest dailyCommentRequest) {
        Optional<String> optionalCommentId = Optional.ofNullable(dailyCommentRequest.getId());
        Optional<String> optionalContent = Optional.ofNullable(dailyCommentRequest.getContent());
        Optional<Mood> optionalMood = Optional.ofNullable(dailyCommentRequest.getMood());

        boolean isCreate = optionalCommentId.isEmpty();

        if (isCreate && (optionalContent.isEmpty() || optionalMood.isEmpty())) {
            throw new BadRequestException("Content and mood must not be empty when creating a comment.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        DailyCommentEntity commentEntity = optionalCommentId
                .map(id -> {
                    DailyCommentEntity existing = controllerHelper.getDailyCommentOrThrowException(id);
                    if (!existing.getUserId().equals(user.getId())) {
                        throw new ForbiddenException("You can only update your own comments.");
                    }
                    return existing;
                })
                .orElseGet(() -> DailyCommentEntity.builder()
                        .userId(user.getId())
                        .build());

        optionalContent.ifPresent(commentEntity::setContent);
        optionalMood.ifPresent(commentEntity::setMood);

        DailyCommentEntity saved = dailyCommentRepository.saveAndFlush(commentEntity);
        return dailyCommentDtoFactory.createDailyCommentDto(saved);
    }

    @DeleteMapping(DELETE_DAILY_COMMENT)
    public ResponseDto deleteDailyComment(
            @PathVariable("daily_comment_id") String dailyCommentId) {
        {
            controllerHelper.getDailyCommentOrThrowException(dailyCommentId);

            dailyCommentRepository.deleteById(dailyCommentId);

            return ResponseDto.makeDefault(true);

        }

    }
}