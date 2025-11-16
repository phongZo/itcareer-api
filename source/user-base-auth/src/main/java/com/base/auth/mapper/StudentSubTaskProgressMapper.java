package com.base.auth.mapper;

import com.base.auth.dto.studentSubTaskProgress.StudentSubTaskProgressDisplayDto;
import com.base.auth.dto.studentSubTaskProgress.StudentSubTaskProgressDto;
import com.base.auth.model.StudentSubTaskProgress;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {TaskMapper.class, StudentMapper.class})
public interface StudentSubTaskProgressMapper {
  @Mapping(source = "id", target = "id")
  @Mapping(source = "currentAttempt", target = "currentAttempt")
  @Mapping(source = "errorCount", target = "errorCount")
  @Mapping(source = "state", target = "state")
  @Mapping(source = "student", target = "student", qualifiedByName = "fromEntityToStudentDto")
  @Mapping(source = "task", target = "task", qualifiedByName = "fromEntityToTaskDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToStudentSubTaskProgressDto")
  StudentSubTaskProgressDto fromEntityToStudentSubTaskProgressDto(StudentSubTaskProgress studentSubTaskProgress);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "state", target = "state")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToStudentSubTaskProgressDisplayDto")
  StudentSubTaskProgressDisplayDto fromEntityToStudentSubTaskProgressDisplayDto(StudentSubTaskProgress studentSubTaskProgress);
}
