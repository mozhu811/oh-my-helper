package io.cruii.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cruii
 * Created on 2023/2/24
 */
@Data
public class PushMessageDTO implements Serializable {
    private static final long serialVersionUID = 3793911128305924519L;

    private String dedeuserid;

    private String content;
}
