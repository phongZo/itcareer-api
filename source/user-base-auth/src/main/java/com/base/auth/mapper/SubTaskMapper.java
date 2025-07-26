package com.base.auth.mapper;

import com.base.auth.dto.subtask.SubTaskClientDto;
import com.base.auth.dto.subtask.SubTaskDisplayDto;
import com.base.auth.dto.subtask.SubTaskDto;
import com.base.auth.form.subtask.CreateSubTaskForm;
import com.base.auth.form.subtask.UpdateSubTaskForm;
import com.base.auth.model.SubTask;
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
public interface SubTaskMapper {
  @Mapping(source = "title", target = "title")
  @Mapping(source = "introduction", target = "introduction")
  @Mapping(source = "content", target = "content")
  @Mapping(source = "imagePath", target = "imagePath")
  @Mapping(source = "filePath", target = "filePath")
  @Mapping(source = "videoPath", target = "videoPath")
  @BeanMapping(ignoreByDefault = true)
  SubTask fromCreateSubTaskFormToEntity(CreateSubTaskForm createSubTaskForm);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "introduction", target = "introduction")
  @Mapping(source = "content", target = "content")
  @Mapping(source = "imagePath", target = "imagePath")
  @Mapping(source = "filePath", target = "filePath")
  @Mapping(source = "videoPath", target = "videoPath")
  @Mapping(source = "task", target = "task", qualifiedByName = "fromEntityToTaskDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToSubTaskDto")
  SubTaskDto fromEntityToSubTaskDto(SubTask subTask);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "task", target = "task", qualifiedByName = "fromEntityToTaskDisplayDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToSubTaskDisplayDto")
  SubTaskDisplayDto fromEntityToSubTaskDisplayDto(SubTask subTask);

  @Mapping(source = "title", target = "title")
  @Mapping(source = "introduction", target = "introduction")
  @Mapping(source = "content", target = "content")
  @Mapping(source = "imagePath", target = "imagePath")
  @Mapping(source = "filePath", target = "filePath")
  @Mapping(source = "videoPath", target = "videoPath")
  @Mapping(source = "task", target = "task", qualifiedByName = "fromEntityToTaskDisplayDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToSubTaskClientDto")
  SubTaskClientDto fromEntityToSubTaskClientDto(SubTask subTask);

  @IterableMapping(elementTargetType = SubTaskDto.class, qualifiedByName = "fromEntityToSubTaskDto")
  List<SubTaskDto> fromEntityToSubTaskDtoList(List<SubTask> subTasks);

  @IterableMapping(elementTargetType = SubTaskDisplayDto.class, qualifiedByName = "fromEntityToSubTaskDisplayDto")
  List<SubTaskDisplayDto> fromEntityToSubTaskDisplayDtoList(List<SubTask> subTasks);

  @Mapping(source = "title", target = "title")
  @Mapping(source = "introduction", target = "introduction")
  @Mapping(source = "content", target = "content")
  @Mapping(source = "imagePath", target = "imagePath")
  @Mapping(source = "filePath", target = "filePath")
  @Mapping(source = "videoPath", target = "videoPath")
  @BeanMapping(ignoreByDefault = true)
  void fromUpdateSubTaskFormToEntity(UpdateSubTaskForm updateSubTaskForm, @MappingTarget SubTask subTask);
}
