package com.altloc.backend.api.app;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.altloc.backend.api.dto.IdentityMatrixRequest;
import com.altloc.backend.api.dto.IndentityMatrixDto;
import com.altloc.backend.api.factories.IndentityMatrixDtoFactory;
import com.altloc.backend.exception.BadRequestException;
import com.altloc.backend.model.UserDetailsImpl;
import com.altloc.backend.store.entities.app.IndentityMatrixEntity;
import com.altloc.backend.store.repositories.app.IndentityMatrixRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RequestMapping("/app")
@RestController
public class IdentityMatrixController {

        private final IndentityMatrixDtoFactory indentityMatrixDtoFactory;
        private final IndentityMatrixRepository indentityMatrixRepository;

        @PostMapping("/identity-matrix")
        public IndentityMatrixDto createIndentityMatrix(
                        @RequestBody IdentityMatrixRequest request) {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

                indentityMatrixRepository
                                .findByName(request.getName())
                                .ifPresent(
                                                indentityMatrix -> {
                                                        throw new BadRequestException(
                                                                        String.format("Indentity matrix with name %s already exists",
                                                                                        request.getName()));
                                                });

                IndentityMatrixEntity indentityMatrix = indentityMatrixRepository.saveAndFlush(
                                IndentityMatrixEntity.builder()
                                                .name(request.getName())
                                                .userId(user.getId())
                                                .build());

                return indentityMatrixDtoFactory.createIndentityMatrixDto(indentityMatrix);
        }

}
