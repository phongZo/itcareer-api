package com.base.auth.mapper;

import com.base.auth.dto.specialization.SpecializationAutoCompleteDto;
import com.base.auth.dto.specialization.SpecializationDto;
import com.base.auth.form.specialization.CreateSpecializationForm;
import com.base.auth.form.specialization.UpdateSpecializationForm;
import com.base.auth.model.Specialization;
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
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SpecializationMapper {
  @Mapping(source = "name", target = "name")
  @BeanMapping(ignoreByDefault = true)
  Specialization fromCreateSpecializationFormToEntity(CreateSpecializationForm createSpecializationForm);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToSpecializationDto")
  SpecializationDto fromEntityToSpecializationDto(Specialization specialization);

  @Mapping(source = "name", target = "name")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToSpecializationAutoCompleteDto")
  SpecializationAutoCompleteDto fromEntityToSpecializationAutoCompleteDto(Specialization specialization);

  @IterableMapping(elementTargetType = SpecializationDto.class, qualifiedByName = "fromEntityToSpecializationDto")
  @BeanMapping(ignoreByDefault = true)
  List<SpecializationDto> fromEntityToSpecializationDtoList(List<Specialization> specializations);

  @IterableMapping(elementTargetType = SpecializationAutoCompleteDto.class, qualifiedByName = "fromEntityToSpecializationAutoCompleteDto")
  @BeanMapping(ignoreByDefault = true)
  List<SpecializationAutoCompleteDto> fromEntityToSpecializationAutoCompleteDtoList(List<Specialization> specializations);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  void fromUpdateSpecializationFormToEntity(UpdateSpecializationForm updateSpecializationForm, @MappingTarget Specialization specialization);
}
