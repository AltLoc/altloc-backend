package com.altloc.backend.store.repositories.app;

import org.springframework.stereotype.Repository;

import com.altloc.backend.store.entities.app.DailyCommentEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface DailyCommentRepository extends JpaRepository<DailyCommentEntity, String> {

    List<DailyCommentEntity> findAllByUserId(String userId);
}
