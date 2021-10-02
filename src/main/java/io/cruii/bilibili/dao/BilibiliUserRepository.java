package io.cruii.bilibili.dao;

import io.cruii.bilibili.entity.BilibiliUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BilibiliUserRepository extends JpaRepository<BilibiliUser, Long> {
    @Query("from bilibili_user where dedeuserid = ?1")
    Optional<BilibiliUser> findOne(String dedeuserid);
}