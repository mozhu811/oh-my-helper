package io.cruii.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("config_pusher")
public class PusherConfigDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;

    private String config;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
