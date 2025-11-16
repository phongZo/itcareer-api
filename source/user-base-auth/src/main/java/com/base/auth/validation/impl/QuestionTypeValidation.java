package com.base.auth.validation.impl;

import com.base.auth.constant.ITDreamConstant;
import com.base.auth.validation.QuestionType;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class QuestionTypeValidation implements ConstraintValidator<QuestionType, Integer> {

  private boolean allowNull;

  @Override
  public void initialize(QuestionType constraintAnnotation) {
    allowNull = constraintAnnotation.allowNull();
  }

  @Override
  public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
    return value == null ? allowNull : ITDreamConstant.QUESTION_TYPES.contains(value);
  }
}
