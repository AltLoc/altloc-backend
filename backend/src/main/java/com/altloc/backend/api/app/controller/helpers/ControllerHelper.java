package com.altloc.backend.api.app.controller.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.altloc.backend.store.entities.app.IndentityMatrixEntity;
import com.altloc.backend.store.repositories.app.IndentityMatrixRepository;
import com.altloc.backend.exception.NotFoundException;

import jakarta.transaction.Transactional;

@RequiredArgsConstructor
@Component
@Transactional
public class ControllerHelper {

    private IndentityMatrixRepository indentityMatrixRepository;

    public IndentityMatrixEntity getProjectOrThrowException(String matrixId) {

        return indentityMatrixRepository
                .findById(matrixId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(
                                "Identity Matrix with \"%s\" doesn't exist.",
                                matrixId)));
    }
}