package com.base.auth.validation.impl;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.validation.TaskKind;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TaskKindValidation implements ConstraintValidator<TaskKind, Integer> {

  private boolean allowNull;

  @Override
  public void initialize(TaskKind constraintAnnotation) {
    allowNull = constraintAnnotation.allowNull();
  }

  @Override
  public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
    return value == null ? allowNull : UserBaseConstant.TASK_KINDS.contains(value);
  }
}
