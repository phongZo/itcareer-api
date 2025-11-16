package com.base.auth.validation;

import com.base.auth.constant.ITDreamConstant;
import com.base.auth.validation.impl.PasswordValidation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidation.class)
@Documented
public @interface Password {
  boolean allowNull() default false;

  String pattern() default ITDreamConstant.PASSWORD_PATTERN;

  String message() default "The password must be between 8 and 15 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
