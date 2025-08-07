package com.base.auth.mapper;

import com.base.auth.dto.task.TaskDisplayDto;
import com.base.auth.dto.task.TaskDto;
import com.base.auth.dto.task.TaskEducatorDto;
import com.base.auth.dto.task.TaskStudentDto;
import com.base.auth.form.task.CreateTaskForm;
import com.base.auth.form.task.UpdateTaskForm;
import com.base.auth.model.Task;
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
    uses = {SimulationMapper.class})
public interface TaskMapper {
  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "introduction", target = "introduction")
  @Mapping(source = "content", target = "content")
  @Mapping(source = "kind", target = "kind")
  @BeanMapping(ignoreByDefault = true)
  Task fromCreateTaskFormToEntity(CreateTaskForm createTaskForm);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "introduction", target = "introduction")
  @Mapping(source = "content", target = "content")
  @Mapping(source = "kind", target = "kind")
  @Mapping(source = "imagePath", target = "imagePath")
  @Mapping(source = "filePath", target = "filePath")
  @Mapping(source = "videoPath", target = "videoPath")
  @Mapping(source = "maxErrors", target = "maxErrors")
  @Mapping(source = "totalQuestion", target = "totalQuestion")
  @Mapping(source = "simulation", target = "simulation", qualifiedByName = "fromEntityToSimulationDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToTaskDto")
  TaskDto fromEntityToTaskDto(Task task);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "introduction", target = "introduction")
  @Mapping(source = "simulation", target = "simulation", qualifiedByName = "fromEntityToSimulationClientDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToTaskDisplayDto")
  TaskDisplayDto fromEntityToTaskDisplayDto(Task task);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "introduction", target = "introduction")
  @Mapping(source = "content", target = "content")
  @Mapping(source = "imagePath", target = "imagePath")
  @Mapping(source = "filePath", target = "filePath")
  @Mapping(source = "videoPath", target = "videoPath")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToTaskStudentDto")
  TaskStudentDto fromEntityToTaskStudentDto(Task task);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "introduction", target = "introduction")
  @Mapping(source = "content", target = "content")
  @Mapping(source = "kind", target = "kind")
  @Mapping(source = "imagePath", target = "imagePath")
  @Mapping(source = "filePath", target = "filePath")
  @Mapping(source = "videoPath", target = "videoPath")
  @Mapping(source = "maxErrors", target = "maxErrors")
  @Mapping(source = "totalQuestion", target = "totalQuestion")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToTaskEducatorDto")
  TaskEducatorDto fromEntityToTaskEducatorDto(Task task);

  @IterableMapping(elementTargetType = TaskDto.class, qualifiedByName = "fromEntityToTaskDto")
  List<TaskDto> fromEntityToTaskDtoList(List<Task> tasks);

  @IterableMapping(elementTargetType = TaskDisplayDto.class, qualifiedByName = "fromEntityToTaskDisplayDto")
  List<TaskDisplayDto> fromEntityToTaskDisplayDtoList(List<Task> tasks);

  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "introduction", target = "introduction")
  @Mapping(source = "content", target = "content")
  @BeanMapping(ignoreByDefault = true)
  void fromUpdateTaskFormToEntity(UpdateTaskForm updateTaskForm, @MappingTarget Task task);
}
