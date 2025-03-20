package com.altloc.backend.api.app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.altloc.backend.api.app.controller.helpers.ControllerHelper;
import com.altloc.backend.api.app.dto.IdentityMatrixRequest;
import com.altloc.backend.api.app.dto.IndentityMatrixDto;
import com.altloc.backend.api.app.dto.ResponseDto;
import com.altloc.backend.api.app.factories.IndentityMatrixDtoFactory;
import com.altloc.backend.exception.BadRequestException;
import com.altloc.backend.model.UserDetailsImpl;
import com.altloc.backend.store.entities.app.IndentityMatrixEntity;
import com.altloc.backend.store.repositories.app.IndentityMatrixRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class IdentityMatrixController {

        private final IndentityMatrixDtoFactory indentityMatrixDtoFactory;
        private final IndentityMatrixRepository indentityMatrixRepository;
        private final ControllerHelper controllerHelper;

        public static final String FETCH_IDENTITY_MATRIX = "/identity-matrices";
        public static final String CREATE_OR_UPDATE_IDENTITY_MATRIX = "/identity-matrix";
        public static final String DELETE_IDENTITY_MATRIX = "/identity-matrix/{identityMatrix_id}";

        @GetMapping(FETCH_IDENTITY_MATRIX)
        public List<IndentityMatrixDto> fetchIndentityMatrices(
                        @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {
                optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

                Stream<IndentityMatrixEntity> projectStream = optionalPrefixName
                                .map(indentityMatrixRepository::streamAllByNameStartsWithIgnoreCase)
                                .orElseGet(indentityMatrixRepository::streamAllBy);

                return projectStream
                                .map(indentityMatrixDtoFactory::createIndentityMatrixDto)
                                .collect(Collectors.toList());

        }

        @PutMapping(CREATE_OR_UPDATE_IDENTITY_MATRIX)
        public IndentityMatrixDto createOrUpdateProject(@RequestBody IdentityMatrixRequest identityMatrixRequest) {

                Optional<String> optionalIdentityMatixId = Optional.ofNullable(identityMatrixRequest.getId());
                Optional<String> optionalIdentityMatixName = Optional.ofNullable(identityMatrixRequest.getName());

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

                optionalIdentityMatixName = optionalIdentityMatixName
                                .filter(projectName -> !projectName.trim().isEmpty());

                boolean isCreate = !optionalIdentityMatixId.isPresent();

                if (isCreate && !optionalIdentityMatixName.isPresent()) {
                        throw new BadRequestException("Identity Matrix name can't be empty.");
                }

                final IndentityMatrixEntity indentityMatrix = optionalIdentityMatixId
                                .map(controllerHelper::getProjectOrThrowException)
                                .orElseGet(() -> IndentityMatrixEntity.builder().build());

                optionalIdentityMatixName
                                .ifPresent(projectName -> {

                                        indentityMatrixRepository
                                                        .findByName(projectName)
                                                        .filter(anotherProject -> !Objects.equals(
                                                                        anotherProject.getId(),
                                                                        indentityMatrix.getId()))
                                                        .ifPresent(anotherProject -> {
                                                                throw new BadRequestException(
                                                                                String.format("Project \"%s\" already exists.",
                                                                                                projectName));
                                                        });

                                        indentityMatrix.setName(projectName);
                                });

                final IndentityMatrixEntity savedIndentityMatrix = indentityMatrixRepository.saveAndFlush(
                                IndentityMatrixEntity.builder()
                                                .name(identityMatrixRequest.getName())
                                                .userId(user.getId())
                                                .build());

                return indentityMatrixDtoFactory.createIndentityMatrixDto(savedIndentityMatrix);
        }

        @DeleteMapping(DELETE_IDENTITY_MATRIX)
        public ResponseDto deleteIndentityMatrix(
                        @PathVariable("identityMatrix_id") String identityMatrixId) {
                {
                        controllerHelper.getProjectOrThrowException(identityMatrixId);

                        indentityMatrixRepository.deleteById(identityMatrixId);
                        return ResponseDto.makeDefault(true);

                }
        }
}
