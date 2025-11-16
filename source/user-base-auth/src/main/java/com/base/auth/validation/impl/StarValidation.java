package com.base.auth.validation.impl;

import com.base.auth.constant.ITDreamConstant;
import com.base.auth.validation.Star;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class StarValidation implements ConstraintValidator<Star, Integer> {

  private boolean allowNull;

  @Override
  public void initialize(Star constraintAnnotation) {
    allowNull = constraintAnnotation.allowNull();
  }

  @Override
  public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
    return value == null ? allowNull : ITDreamConstant.STARS.contains(value);
  }
}
