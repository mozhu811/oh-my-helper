package io.cruii.bilibili.dao;

import io.cruii.bilibili.entity.TaskConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TaskConfigRepository extends JpaRepository<TaskConfig, Long> {
    @Query("from task_config where dedeuserid = ?1")
    Optional<TaskConfig> findOne(String dedeuserid);
}