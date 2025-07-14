package com.base.auth.validation.impl;

import com.base.auth.validation.Password;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class PasswordValidation implements ConstraintValidator<Password, String> {

  private boolean allowNull;
  private String pattern;

  @Override
  public void initialize(Password constraintAnnotation) {
    allowNull = constraintAnnotation.allowNull();
    pattern = constraintAnnotation.pattern();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
    return StringUtils.isBlank(value) ? allowNull : StringUtils.isNotBlank(value) && value.matches(pattern);
  }
}
