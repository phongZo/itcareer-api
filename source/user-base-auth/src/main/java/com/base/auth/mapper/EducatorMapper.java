package com.base.auth.mapper;

import com.base.auth.dto.educator.EducatorAutoCompleteDto;
import com.base.auth.dto.educator.EducatorDto;
import com.base.auth.dto.educator.ProfileEducatorDto;
import com.base.auth.form.educator.UpdateProfileEducatorForm;
import com.base.auth.model.Educator;
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
    uses = {AccountMapper.class})
public interface EducatorMapper {
  @Mapping(source = "id",target = "id")
  @Mapping(source = "birthday",target = "birthday")
  @Mapping(source ="account",target = "account",qualifiedByName="fromAccountToDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToEducatorDto")
  EducatorDto fromEntityToEducatorDto(Educator educator);

  @Mapping(source = "id",target = "id")
  @Mapping(source ="account",target = "accountAutoCompleteDto",qualifiedByName="fromAccountToAutoCompleteDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEducatorToDtoAutoComplete")
  EducatorAutoCompleteDto fromEducatorToDtoAutoComplete(Educator educator);

  @IterableMapping(elementTargetType = EducatorDto.class,qualifiedByName = "fromEntityToEducatorDto")
  @BeanMapping(ignoreByDefault = true)
  List<EducatorDto> fromEducatorListToEducatorDtoList(List<Educator> list);

  @IterableMapping(elementTargetType = EducatorAutoCompleteDto.class,qualifiedByName = "fromEducatorToDtoAutoComplete")
  @BeanMapping(ignoreByDefault = true)
  List<EducatorAutoCompleteDto> fromEducatorListToEducatorDtoListAutocomplete(List<Educator> list);

  @Mapping(source = "birthday", target = "birthday")
  @Mapping(source = "account", target = "profileAccountDto", qualifiedByName = "fromAccountToProfileDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEducatorToProfileDto")
  ProfileEducatorDto fromEducatorToProfileDto(Educator Educator);

  @Mapping(source = "birthday", target = "birthday")
  @BeanMapping(ignoreByDefault = true)
  void fromUpdateProfileEducatorFormToEntity(UpdateProfileEducatorForm updateProfileEducatorForm, @MappingTarget Educator Educator);
}
