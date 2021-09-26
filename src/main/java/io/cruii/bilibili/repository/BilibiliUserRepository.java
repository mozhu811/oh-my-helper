package io.cruii.bilibili.repository;

import io.cruii.bilibili.entity.BilibiliUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author cruii
 * Created on 2021/9/15
 */
@Repository
public interface BilibiliUserRepository extends MongoRepository<BilibiliUser, String> {
}
