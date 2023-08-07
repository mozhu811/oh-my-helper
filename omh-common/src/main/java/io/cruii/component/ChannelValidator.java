package io.cruii.component;

import io.cruii.annatation.ChannelConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ChannelValidator implements ConstraintValidator<ChannelConstraint, String> {

    private static final String[] ALLOWED_CHANNELS = {"bark", "telegram", "serverchan", "feishu", "qywechat"};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // 如果值为null，交由@NotNull注解处理
        }
        for (String allowed : ALLOWED_CHANNELS) {
            if (allowed.equals(value)) {
                return true;
            }
        }
        // 返回错误提示信息
        context.buildConstraintViolationWithTemplate("channel的可选值为：bark, telegram, serverchan, feishu, qywechat")
                .addConstraintViolation();
        return false;
    }
}

