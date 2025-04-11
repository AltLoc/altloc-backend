package com.altloc.backend.api.app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.altloc.backend.api.app.controller.helpers.ControllerHelper;
import com.altloc.backend.api.app.dto.IdentityMatrixRequest;
import com.altloc.backend.api.app.dto.IdentityMatrixDto;
import com.altloc.backend.api.app.dto.ResponseDto;
import com.altloc.backend.api.app.factories.IdentityMatrixDtoFactory;
import com.altloc.backend.exception.BadRequestException;
import com.altloc.backend.model.UserDetailsImpl;
import com.altloc.backend.store.entities.app.IdentityMatrixEntity;
import com.altloc.backend.store.repositories.app.IdentityMatrixRepository;

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

  private final IdentityMatrixDtoFactory identityMatrixDtoFactory;
  private final IdentityMatrixRepository identityMatrixRepository;
  private final ControllerHelper controllerHelper;

  public static final String FETCH_IDENTITY_MATRICES = "/identity-matrices/";
  public static final String CREATE_OR_UPDATE_IDENTITY_MATRIX = "/identity-matrix";
  public static final String DELETE_IDENTITY_MATRIX = "/identity-matrix/{identity_matrix_id}";
  public static final String GET_IDENTITY_MATRIX = "/identity-matrix/{identity_matrix_id}";

  @GetMapping(FETCH_IDENTITY_MATRICES)
  public List<IdentityMatrixDto> fetchIndentityMatrices(
      @RequestParam(value = "prefix_name", required = false

      ) Optional<String> optionalPrefixName) {
    optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

    Stream<IdentityMatrixEntity> identityMatrixStream = optionalPrefixName
        .map(identityMatrixRepository::streamAllByNameStartsWithIgnoreCase)
        .orElseGet(identityMatrixRepository::streamAllBy);

    return identityMatrixStream
        .map(identityMatrixDtoFactory::createIdentityMatrixDto)
        .collect(Collectors.toList());

  }

  @GetMapping(GET_IDENTITY_MATRIX)
  public IdentityMatrixDto getIdentityMatrix(
      @PathVariable(name = "identity_matrix_id") String identityMatrixId) {
    return identityMatrixDtoFactory.createIdentityMatrixDto(
        controllerHelper.getIdentityMatrixOrThrowException(identityMatrixId));
  }

  @PutMapping(CREATE_OR_UPDATE_IDENTITY_MATRIX)
  public IdentityMatrixDto createOrUpdateProject(@RequestBody IdentityMatrixRequest identityMatrixRequest) {

    Optional<String> optionalIdentityMatixId = Optional.ofNullable(identityMatrixRequest.getId());
    Optional<String> optionalIdentityMatixName = Optional.ofNullable(identityMatrixRequest.getName());

    System.out.println("optionalIdentityMatixId" + optionalIdentityMatixId);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

    optionalIdentityMatixName = optionalIdentityMatixName
        .filter(projectName -> !projectName.trim().isEmpty());

    boolean isCreate = !optionalIdentityMatixId.isPresent();

    if (isCreate && !optionalIdentityMatixName.isPresent()) {
      throw new BadRequestException("Identity Matrix name can't be empty.");
    }

    final IdentityMatrixEntity indentityMatrix = optionalIdentityMatixId
        .map(controllerHelper::getIdentityMatrixOrThrowException)
        .orElseGet(() -> IdentityMatrixEntity.builder().build());

    optionalIdentityMatixName
        .ifPresent(matrixName -> {

          identityMatrixRepository
              .findByName(matrixName)
              .filter(anotherMatrix -> !Objects.equals(
                  anotherMatrix.getId(),
                  indentityMatrix.getId()))
              .ifPresent(anotherProject -> {
                throw new BadRequestException(
                    String.format("Matrix \"%s\" already exists.",
                        matrixName));
              });

          indentityMatrix.setName(matrixName);
        });

    final IdentityMatrixEntity savedIndentityMatrix = identityMatrixRepository.saveAndFlush(
        IdentityMatrixEntity.builder()
            .name(identityMatrixRequest.getName())
            .description(identityMatrixRequest.getDescription())

            .userId(user.getId())
            .build());

    return identityMatrixDtoFactory.createIdentityMatrixDto(savedIndentityMatrix);
  }

  @DeleteMapping(DELETE_IDENTITY_MATRIX)
  public ResponseDto deleteIndentityMatrix(
      @PathVariable("identity_matrix_id") String identityMatrixId) {
    {
      controllerHelper.getIdentityMatrixOrThrowException(identityMatrixId);

      identityMatrixRepository.deleteById(identityMatrixId);
      return ResponseDto.makeDefault(true);

    }
  }
}
