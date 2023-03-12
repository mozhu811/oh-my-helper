package io.cruii.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author cruii
 * Created on 2023/2/17
 */
@Data
@TableName("omg_user")
public class OmhUserDO {
    private Long id;

    @TableId(type = IdType.ASSIGN_ID)
    private String userId;

    private String nickname;

    private String passwd;

    private String telephone;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
