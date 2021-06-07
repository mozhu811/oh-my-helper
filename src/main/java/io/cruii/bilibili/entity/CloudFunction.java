package io.cruii.bilibili.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author cruii
 * Created on 2021/6/6
 */
@Data
@TableName("cloud_function")
public class CloudFunction{
    @TableId(type = IdType.AUTO)
    private Long id;

    private String functionId;

    private String functionName;

    private String topicId;

    private String region;

    private String dedeuserid;
}
