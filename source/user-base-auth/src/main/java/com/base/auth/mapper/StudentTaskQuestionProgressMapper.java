package com.base.auth.mapper;

import com.base.auth.dto.studentTaskQuestionProgress.StudentTaskQuestionProgressDisplayDto;
import com.base.auth.dto.studentTaskQuestionProgress.StudentTaskQuestionProgressDto;
import com.base.auth.form.studentTaskQuestionProgress.CreateStudentTaskQuestionProgressForm;
import com.base.auth.model.StudentTaskQuestionProgress;
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
    uses = {StudentSubTaskProgressMapper.class, TaskQuestionMapper.class})
public interface StudentTaskQuestionProgressMapper {
  @Mapping(source = "answer", target = "answer")
  @BeanMapping(ignoreByDefault = true)
  StudentTaskQuestionProgress fromCreateStudentTaskQuestionProgressFormToEntity(
      CreateStudentTaskQuestionProgressForm createStudentTaskQuestionProgressForm);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "answer", target = "answer")
  @Mapping(source = "isCorrect", target = "isCorrect")
  @Mapping(source = "studentSubTaskProgress", target = "studentSubTaskProgress", qualifiedByName = "fromEntityToStudentSubTaskProgressDto")
  @Mapping(source = "taskQuestion", target = "taskQuestion", qualifiedByName = "fromEntityToTaskQuestionDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToStudentTaskQuestionProgressDto")
  StudentTaskQuestionProgressDto fromEntityToStudentTaskQuestionProgressDto(StudentTaskQuestionProgress studentTaskQuestionProgress);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "answer", target = "answer")
  @Mapping(source = "isCorrect", target = "isCorrect")
  @Mapping(source = "studentSubTaskProgress", target = "studentSubTaskProgress", qualifiedByName = "fromEntityToStudentSubTaskProgressDisplayDto")
  @Mapping(source = "taskQuestion", target = "taskQuestion", qualifiedByName = "fromEntityToTaskQuestionStudentDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToStudentTaskQuestionProgressDisplayDto")
  StudentTaskQuestionProgressDisplayDto fromEntityToStudentTaskQuestionProgressDisplayDto(StudentTaskQuestionProgress studentTaskQuestionProgress);

  @IterableMapping(elementTargetType = StudentTaskQuestionProgressDto.class, qualifiedByName = "fromEntityToStudentTaskQuestionProgressDto")
  List<StudentTaskQuestionProgressDto> fromEntityToStudentTaskQuestionProgressDtoList(List<StudentTaskQuestionProgress> studentTaskQuestionProgresses);

  @IterableMapping(elementTargetType = StudentTaskQuestionProgressDisplayDto.class, qualifiedByName = "fromEntityToStudentTaskQuestionProgressDisplayDto")
  List<StudentTaskQuestionProgressDisplayDto> fromEntityToStudentTaskQuestionProgressDisplayDtoList(List<StudentTaskQuestionProgress> studentTaskQuestionProgresses);
}
