package com.polar.experience.repository;

import com.polar.experience.entity.Session;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findBySessionNo(String sessionNo);

    List<Session> findByStatus(Integer status);

    /**
     * 行级悲观锁读取场次：与场次停用（编辑）操作互斥，
     * 保证“校验场次未停用 + 变更年龄段”过程中场次状态不会被并发改为停用。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Session s where s.id = :id")
    Optional<Session> findByIdForUpdate(@Param("id") Long id);
}