package io.cruii.bilibili.repository;

import io.cruii.bilibili.entity.TaskConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author cruii
 * Created on 2021/9/14
 */
@Repository
public interface TaskConfigRepository extends MongoRepository<TaskConfig, String> {
}
