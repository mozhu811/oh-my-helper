package io.cruii.annatation;

import io.cruii.component.ChannelValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ChannelValidator.class)
public @interface ChannelConstraint {

    String message() default "Invalid channel";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

