package com.base.auth.mapper;

import com.base.auth.dto.taskQuestion.TaskQuestionDto;
import com.base.auth.dto.taskQuestion.TaskQuestionEducatorDto;
import com.base.auth.dto.taskQuestion.TaskQuestionStudentDto;
import com.base.auth.form.taskQuestion.CreateTaskQuestionForm;
import com.base.auth.form.taskQuestion.UpdateQuestionTaskForm;
import com.base.auth.model.TaskQuestion;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {TaskMapper.class})
public interface TaskQuestionMapper {
  @Mapping(source = "question", target = "question")
  @Mapping(source = "questionType", target = "questionType")
  @Mapping(source = "options", target = "options")
  @BeanMapping(ignoreByDefault = true)
  TaskQuestion fromCreateTaskQuestionFormToEntity(CreateTaskQuestionForm createTaskQuestionForm);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "question", target = "question")
  @Mapping(source = "questionType", target = "questionType")
  @Mapping(source = "options", target = "options")
  @Mapping(source = "task", target = "task", qualifiedByName = "fromEntityToTaskDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToTaskQuestionDto")
  TaskQuestionDto fromEntityToTaskQuestionDto(TaskQuestion taskQuestion);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "question", target = "question")
  @Mapping(source = "questionType", target = "questionType")
  @Mapping(source = "options", target = "options")
  @Mapping(source = "task", target = "task", qualifiedByName = "fromEntityToTaskEducatorDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToTaskQuestionEducatorDto")
  TaskQuestionEducatorDto fromEntityToTaskQuestionEducatorDto(TaskQuestion taskQuestion);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "question", target = "question")
  @Mapping(source = "options", target = "options")
  @Mapping(source = "task", target = "task", qualifiedByName = "fromEntityToTaskStudentDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToTaskQuestionStudentDto")
  TaskQuestionStudentDto fromEntityToTaskQuestionStudentDto(TaskQuestion taskQuestion);

  @IterableMapping(elementTargetType = TaskQuestionDto.class, qualifiedByName = "fromEntityToTaskQuestionDto")
  List<TaskQuestionDto> fromEntityToTaskQuestionDtoList(List<TaskQuestion> taskQuestions);

  @IterableMapping(elementTargetType = TaskQuestionEducatorDto.class, qualifiedByName = "fromEntityToTaskQuestionEducatorDto")
  List<TaskQuestionEducatorDto> fromEntityToTaskQuestionEducatorDtoList(List<TaskQuestion> taskQuestions);

  @IterableMapping(elementTargetType = TaskQuestionStudentDto.class, qualifiedByName = "fromEntityToTaskQuestionStudentDto")
  List<TaskQuestionStudentDto> fromEntityToTaskQuestionStudentDtoList(List<TaskQuestion> taskQuestions);

  @Mapping(source = "question", target = "question")
  @Mapping(source = "options", target = "options")
  @BeanMapping(ignoreByDefault = true)
  void fromUpdateTaskQuestionFormToEntity(UpdateQuestionTaskForm updateQuestionTaskForm, @MappingTarget TaskQuestion taskQuestion);
}
