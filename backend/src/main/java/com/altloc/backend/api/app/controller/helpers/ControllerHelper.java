package com.altloc.backend.api.app.controller.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.altloc.backend.store.entities.app.IdentityMatrixEntity;
import com.altloc.backend.store.repositories.app.IdentityMatrixRepository;
import com.altloc.backend.exception.NotFoundException;

import jakarta.transaction.Transactional;

@RequiredArgsConstructor
@Component
@Transactional
public class ControllerHelper {

    private final IdentityMatrixRepository identityMatrixRepository;

    public IdentityMatrixEntity getIdentityMatrixOrThrowException(String identityMatrixId) {

        return identityMatrixRepository
                .findById(identityMatrixId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(
                                "Identity Matrix with \"%s\" doesn't exist.",
                                identityMatrixId)));
    }
}