package com.base.auth.mapper;

import com.base.auth.dto.simulation.SimulationDisplayDto;
import com.base.auth.dto.simulation.SimulationClientDto;
import com.base.auth.dto.simulation.SimulationDto;
import com.base.auth.form.simulation.CreateSimulationForm;
import com.base.auth.form.simulation.UpdateSimulationForm;
import com.base.auth.model.Simulation;
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
    uses = {SpecializationMapper.class, EducatorMapper.class})
public interface SimulationMapper {
  @Mapping(source = "title", target = "title")
  @Mapping(source = "overview", target = "overview")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "level", target = "level")
  @Mapping(source = "totalEstimatedTime", target = "totalEstimatedTime")
  @Mapping(source = "imagePath", target = "imagePath")
  @Mapping(source = "videoPath", target = "videoPath")
  @BeanMapping(ignoreByDefault = true)
  Simulation fromCreateSimulationFormToEntity(CreateSimulationForm createSimulationForm);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "overview", target = "overview")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "level", target = "level")
  @Mapping(source = "totalEstimatedTime", target = "totalEstimatedTime")
  @Mapping(source = "imagePath", target = "imagePath")
  @Mapping(source = "videoPath", target = "videoPath")
  @Mapping(source = "avgRating", target = "avgRating")
  @Mapping(source = "participantQuantity", target = "participantQuantity")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "specialization", target = "specialization", qualifiedByName = "fromEntityToSpecializationDto")
  @Mapping(source = "educator", target = "educator", qualifiedByName = "fromEntityToEducatorDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToSimulationDto")
  SimulationDto fromEntityToSimulationDto(Simulation simulation);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "level", target = "level")
  @Mapping(source = "totalEstimatedTime", target = "totalEstimatedTime")
  @Mapping(source = "imagePath", target = "imagePath")
  @Mapping(source = "avgRating", target = "avgRating")
  @Mapping(source = "educator", target = "educator", qualifiedByName = "fromEducatorToProfileDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToSimulationDisplayDto")
  SimulationDisplayDto fromEntityToSimulationDisplayDto(Simulation simulation);

  @Mapping(source = "title", target = "title")
  @Mapping(source = "overview", target = "overview")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "level", target = "level")
  @Mapping(source = "totalEstimatedTime", target = "totalEstimatedTime")
  @Mapping(source = "imagePath", target = "imagePath")
  @Mapping(source = "videoPath", target = "videoPath")
  @Mapping(source = "avgRating", target = "avgRating")
  @Mapping(source = "participantQuantity", target = "participantQuantity")
  @Mapping(source = "specialization", target = "specialization", qualifiedByName = "fromEntityToSpecializationAutoCompleteDto")
  @Mapping(source = "educator", target = "educator", qualifiedByName = "fromEducatorToProfileDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToSimulationClientDto")
  SimulationClientDto fromEntityToSimulationClientDto(Simulation simulation);

  @IterableMapping(elementTargetType = SimulationDto.class, qualifiedByName = "fromEntityToSimulationDto")
  List<SimulationDto> fromEntityToSimulationDtoList(List<Simulation> simulations);

  @IterableMapping(elementTargetType = SimulationDisplayDto.class, qualifiedByName = "fromEntityToSimulationDisplayDto")
  List<SimulationDisplayDto> fromEntityToSimulationDisplayDtoList(List<Simulation> simulations);

  @Mapping(source = "title", target = "title")
  @Mapping(source = "overview", target = "overview")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "level", target = "level")
  @Mapping(source = "totalEstimatedTime", target = "totalEstimatedTime")
  @Mapping(source = "imagePath", target = "imagePath")
  @Mapping(source = "videoPath", target = "videoPath")
  @BeanMapping(ignoreByDefault = true)
  void fromUpdateSimulationFormToEntity(UpdateSimulationForm updateSimulationForm, @MappingTarget Simulation simulation);
}
