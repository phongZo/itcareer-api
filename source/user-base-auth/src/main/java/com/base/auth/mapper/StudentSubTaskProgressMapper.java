package com.base.auth.mapper;

import com.base.auth.dto.studentSubTaskProgress.StudentSubTaskProgressDisplayDto;
import com.base.auth.dto.studentSubTaskProgress.StudentSubTaskProgressDto;
import com.base.auth.model.StudentSubTaskProgress;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {SubTaskMapper.class, StudentMapper.class})
public interface StudentSubTaskProgressMapper {
  @Mapping(source = "id", target = "id")
  @Mapping(source = "currentAttempt", target = "currentAttempt")
  @Mapping(source = "errorCount", target = "errorCount")
  @Mapping(source = "state", target = "state")
  @Mapping(source = "student", target = "student", qualifiedByName = "fromEntityToStudentDto")
  @Mapping(source = "subTask", target = "subTask", qualifiedByName = "fromEntityToSubTaskDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToStudentSubTaskProgressDto")
  StudentSubTaskProgressDto fromEntityToStudentSubTaskProgressDto(StudentSubTaskProgress studentSubTaskProgress);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "subTask", target = "subTask", qualifiedByName = "fromEntityToSubTaskDisplayDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToStudentSubTaskProgressDisplayDto")
  StudentSubTaskProgressDisplayDto fromEntityToStudentSubTaskProgressDisplayDto(StudentSubTaskProgress studentSubTaskProgress);

  @IterableMapping(elementTargetType = StudentSubTaskProgressDto.class, qualifiedByName = "fromEntityToStudentSubTaskProgressDto")
  List<StudentSubTaskProgressDto> fromEntityToStudentSubTaskProgressDtoList(List<StudentSubTaskProgress> studentSubTaskProgresses);

  @IterableMapping(elementTargetType = StudentSubTaskProgressDisplayDto.class, qualifiedByName = "fromEntityToStudentSubTaskProgressDisplayDto")
  List<StudentSubTaskProgressDisplayDto> fromEntityToStudentSubTaskProgressDtoDisplayList(List<StudentSubTaskProgress> studentSubTaskProgresses);

}
