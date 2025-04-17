package com.altloc.backend.api.app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.altloc.backend.api.app.controller.helpers.ControllerHelper;
import com.altloc.backend.api.app.dto.IdentityMatrixDto;
import com.altloc.backend.api.app.dto.ResponseDto;
import com.altloc.backend.api.app.factories.IdentityMatrixDtoFactory;
import com.altloc.backend.exception.BadRequestException;
import com.altloc.backend.model.UserDetailsImpl;
import com.altloc.backend.service.MinioService;
import com.altloc.backend.store.entities.app.IdentityMatrixEntity;
import com.altloc.backend.store.repositories.app.IdentityMatrixRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.MediaType;

@RequiredArgsConstructor
@RequestMapping("/app")
@Transactional
@RestController
public class IdentityMatrixController {

  private final IdentityMatrixDtoFactory identityMatrixDtoFactory;
  private final IdentityMatrixRepository identityMatrixRepository;
  private final ControllerHelper controllerHelper;
  private final MinioService minioService;

  public static final String FETCH_IDENTITY_MATRICES = "/identity-matrices";
  public static final String CREATE_OR_UPDATE_IDENTITY_MATRIX = "/identity-matrix";
  public static final String DELETE_IDENTITY_MATRIX = "/identity-matrix/{identity_matrix_id}";
  public static final String GET_IDENTITY_MATRIX = "/identity-matrix/{identity_matrix_id}";

  @GetMapping(FETCH_IDENTITY_MATRICES)
  public List<IdentityMatrixDto> fetchIdentityMatrices(
      @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

    optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

    Stream<IdentityMatrixEntity> identityMatrixStream = optionalPrefixName
        .map(prefix -> identityMatrixRepository.streamAllByUserIdAndNameStartsWithIgnoreCase(user.getId(), prefix))
        .orElseGet(() -> identityMatrixRepository.streamAllByUserId(user.getId()));

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

  @PutMapping(value = CREATE_OR_UPDATE_IDENTITY_MATRIX, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public IdentityMatrixDto createOrUpdateIdentityMatrix(
      @RequestParam(value = "id", required = false) String id,
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "description", required = false) String description,
      @RequestParam(value = "bannerKey", required = false) String bannerKey,
      @RequestParam(value = "banner", required = false) MultipartFile bannerFile) {

    Optional<String> optionalIdentityMatrixId = Optional.ofNullable(id);
    Optional<String> optionalIdentityMatrixName = Optional.ofNullable(name).filter(n -> !n.trim().isEmpty());

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

    boolean isCreate = optionalIdentityMatrixId.isEmpty();

    if (isCreate && optionalIdentityMatrixName.isEmpty()) {
      throw new BadRequestException("Identity Matrix name can't be empty.");
    }

    IdentityMatrixEntity identityMatrix = optionalIdentityMatrixId
        .map(controllerHelper::getIdentityMatrixOrThrowException)
        .orElseGet(IdentityMatrixEntity::new);

    optionalIdentityMatrixName.ifPresent(newName -> {
      identityMatrixRepository.findByName(newName)
          .filter(existing -> !existing.getId().equals(identityMatrix.getId()))
          .ifPresent(existing -> {
            throw new BadRequestException(String.format("Matrix \"%s\" already exists.", newName));
          });
      identityMatrix.setName(newName);
    });

    identityMatrix.setDescription(description);
    identityMatrix.setUserId(user.getId());

    // If a new file is uploaded, upload it to MinIO and set the bannerKey
    if (bannerFile != null && !bannerFile.isEmpty()) {
      try {
        String fileKey = "users/identityMatrix/" + UUID.randomUUID() + "-" + bannerFile.getOriginalFilename();
        minioService.uploadFile(fileKey, bannerFile.getInputStream(), bannerFile.getSize(),
            bannerFile.getContentType());
        identityMatrix.setBannerKey(fileKey);
      } catch (Exception e) {
        throw new RuntimeException("Ошибка загрузки файла в MinIO: " + e.getMessage(), e);
      }
    } else if (bannerKey != null && !bannerKey.isBlank()) {
      identityMatrix.setBannerKey(bannerKey);
    } else if (isCreate) {
      throw new BadRequestException("При создании необходимо загрузить файл или передать bannerKey.");
    }

    IdentityMatrixEntity saved = identityMatrixRepository.saveAndFlush(identityMatrix);
    return identityMatrixDtoFactory.createIdentityMatrixDto(saved);
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
