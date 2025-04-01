package com.altloc.backend.api.app.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.altloc.backend.api.app.controller.helpers.ControllerHelper;
import com.altloc.backend.api.app.dto.DomainDto;
import com.altloc.backend.api.app.dto.DomainRequest;
import com.altloc.backend.api.app.dto.ResponseDto;
import com.altloc.backend.api.app.factories.DomainDtoFactory;
import com.altloc.backend.store.entities.app.IdentityMatrixEntity;
import com.altloc.backend.store.repositories.app.DomainRepository;
import com.altloc.backend.store.entities.app.DomainEntity;
import com.altloc.backend.exception.BadRequestException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RequestMapping("/app")
@Transactional
@RestController
public class DomainController {

    private final DomainRepository domainRepository;
    private final DomainDtoFactory domainDtoFactory;
    private final ControllerHelper controllerHelper;

    public static final String FETCH_IDENTITY_MATRIX_DOMAINS = "/identity-matrix/{identity_matrix_id}/domains";
    public static final String FETCH_DOMAINS = "/domains";
    public static final String CREATE_OR_UPDATE_DOMAIN = "/identity-matrix/domain";
    public static final String GET_DOMAIN = "/domain/{domain_id}";
    public static final String DELETE_DOMAIN = "/domain/{domain_id}";

    @GetMapping(FETCH_IDENTITY_MATRIX_DOMAINS)
    public List<DomainDto> fetchDomains(
            @PathVariable(name = "identity_matrix_id") String identityMatrixId) {

        IdentityMatrixEntity identityMatrix = controllerHelper.getIdentityMatrixOrThrowException(identityMatrixId);

        return identityMatrix
                .getDomains()
                .stream()
                .map(domainDtoFactory::createDomainDto)
                .collect(Collectors.toList());

    }

    @GetMapping(FETCH_DOMAINS)
    public List<DomainDto> getHabits() {
        return domainRepository
                .findAll()
                .stream()
                .map(domainDtoFactory::createDomainDto)
                .collect(Collectors.toList());
    }

    @GetMapping(GET_DOMAIN)
    public DomainDto getDomainByID(
            @PathVariable("domain_id") String domainId) {
        DomainEntity domainEntity = controllerHelper.getDomainOrThrowException(domainId);
        return domainDtoFactory.createDomainDto(domainEntity);
    }

    @PutMapping(CREATE_OR_UPDATE_DOMAIN)
    public DomainDto createOrUpdateDomain(@RequestBody DomainRequest domainRequest) {

        Optional<String> optionalDomainId = Optional.ofNullable(domainRequest.getId());
        Optional<String> optionalDomainName = Optional.ofNullable(domainRequest.getName());
        Optional<String> optionalIdentityMatrixId = Optional.ofNullable(domainRequest.getIdentityMatrixId());

        boolean isCreate = optionalDomainId.isEmpty();

        if (isCreate && optionalDomainName.isEmpty()) {
            throw new BadRequestException("Domain name can't be empty.");
        }

        if (optionalIdentityMatrixId.isEmpty()) {
            throw new BadRequestException("Identity Matrix ID is required.");
        }

        IdentityMatrixEntity identityMatrix = controllerHelper
                .getIdentityMatrixOrThrowException(optionalIdentityMatrixId.get());

        final DomainEntity domainEntity = optionalDomainId
                .map(controllerHelper::getDomainOrThrowException)
                .orElseGet(() -> DomainEntity.builder()
                        .identityMatrixId(optionalIdentityMatrixId.get())
                        .build());

        optionalDomainName.ifPresent(domainName -> {
            domainRepository
                    .findDomainEntityByIdentityMatrixIdAndNameContainsIgnoreCase(identityMatrix.getId(), domainName)
                    .filter(existingDomain -> !existingDomain.getId().equals(domainEntity.getId()))
                    .ifPresent(existingDomain -> {
                        throw new BadRequestException(String.format("Domain \"%s\" already exists.", domainName));
                    });

            domainEntity.setName(domainName);
        });

        final DomainEntity savedDomain = domainRepository.saveAndFlush(domainEntity);
        return domainDtoFactory.createDomainDto(savedDomain);
    }

    @DeleteMapping(DELETE_DOMAIN)
    public ResponseDto deleteDomain(
            @PathVariable("domain_id") String domainId) {
        {
            DomainEntity domainEntity = controllerHelper.getDomainOrThrowException(domainId);

            if (!domainEntity.getHabits().isEmpty()) {
                throw new BadRequestException("Domain cannot be deleted because it has tasks.");

            }

            domainRepository.deleteById(domainId);

            return ResponseDto.makeDefault(true);

        }
    }

}
