package com.polar.experience.repository;

import com.polar.experience.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findBySessionNo(String sessionNo);

    List<Session> findByStatus(Integer status);
}