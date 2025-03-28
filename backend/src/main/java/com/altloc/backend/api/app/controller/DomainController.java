package com.altloc.backend.api.app.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.altloc.backend.api.app.controller.helpers.ControllerHelper;
import com.altloc.backend.api.app.dto.DomainDto;
import com.altloc.backend.api.app.factories.DomainDtoFactory;
import com.altloc.backend.store.entities.app.IdentityMatrixEntity;
import com.altloc.backend.store.repositories.app.DomainRepository;
import com.altloc.backend.store.entities.app.DomainEntity;
import com.altloc.backend.exception.BadRequestException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@RequestMapping("/app")
@Transactional
@RestController
public class DomainController {

    private final DomainRepository domainRepository;
    private final DomainDtoFactory domainDtoFactory;
    private final ControllerHelper controllerHelper;

    public static final String GET_DOMAINS = "/identity-matrix/{identity_matrix_id}/domains";
    public static final String CREATE_DOMAIN = "/identity-matrix/{identity_matrix_id}/domains";
    public static final String DELETE_DOMAIN = "/identity-matrix/{identity_matrix_id}/domain/{domain_id}";
    public static final String GET_DOMAIN_ById = "/identity-matrix/{identity_matrix_id}/domain/{domain_id}";

    @GetMapping(GET_DOMAINS)
    public List<DomainDto> getDomains(
            @PathVariable(name = "identity_matrix_id") String identityMatrixId) {

        IdentityMatrixEntity identityMatrix = controllerHelper.getIdentityMatrixOrThrowException(identityMatrixId);

        return identityMatrix
                .getDomains()
                .stream()
                .map(domainDtoFactory::createDomainDto)
                .collect(Collectors.toList());

    }

    @PostMapping(CREATE_DOMAIN)
    public DomainDto createDomain(
            @PathVariable(name = "identity_matrix_id") String identityMatrixId,
            @RequestParam(name = "domain_name") String domainName) {

        if (domainName.trim().isEmpty()) {
            throw new BadRequestException("Domain name cannot be empty.");
        }

        IdentityMatrixEntity identityMatrix = controllerHelper.getIdentityMatrixOrThrowException(identityMatrixId);

        identityMatrix
                .getDomains()
                .stream()
                .map(DomainEntity::getName)
                .filter(anotherDomainName -> anotherDomainName.equalsIgnoreCase(domainName))
                .findAny()
                .ifPresent(anotherDomainName -> {
                    throw new BadRequestException(String.format("Domain with name %s already exists.", domainName));
                });

        final DomainEntity savedDomain = domainRepository.saveAndFlush(
                DomainEntity.builder()
                        .name(domainName)
                        .identityMatrixId(identityMatrixId)
                        .build());

        return domainDtoFactory.createDomainDto(savedDomain);
    }

}
