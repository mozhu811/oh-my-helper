package io.cruii.bilibili.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Data
public class CloudFunctionLog {
    private Long id;

    private LocalDateTime logTime;

    private String level;

    private String message;
}
