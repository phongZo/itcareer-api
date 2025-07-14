package com.base.auth.validation.impl;

import com.base.auth.validation.Phone;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class PhoneValidation implements ConstraintValidator<Phone, String> {
  private boolean allowNull;
  private String pattern;

  @Override
  public void initialize(Phone constraintAnnotation) {
    allowNull = constraintAnnotation.allowNull();
    pattern = constraintAnnotation.pattern();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
    return StringUtils.isBlank(value) ? allowNull : StringUtils.isNotBlank(value) && value.matches(pattern);
  }
}
