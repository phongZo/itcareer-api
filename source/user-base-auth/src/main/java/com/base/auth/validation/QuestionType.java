package com.base.auth.validation;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.validation.impl.QuestionTypeValidation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = QuestionTypeValidation.class)
@Documented
public @interface QuestionType {
  boolean allowNull() default false;

  String pattern() default UserBaseConstant.PHONE_PATTERN;

  String message() default "Question type: 1 - File, 2 - Text, 3 - Multiple choice";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
