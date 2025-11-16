package com.base.auth.mapper;

import com.base.auth.dto.educator.EducatorDto;
import com.base.auth.dto.educator.ProfileEducatorDto;
import com.base.auth.model.Educator;
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
    uses = {AccountMapper.class})
public interface EducatorMapper {
  @Mapping(source = "id",target = "id")
  @Mapping(source ="account",target = "account",qualifiedByName="fromAccountToDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToEducatorDto")
  EducatorDto fromEntityToEducatorDto(Educator educator);

  @IterableMapping(elementTargetType = EducatorDto.class,qualifiedByName = "fromEntityToEducatorDto")
  @BeanMapping(ignoreByDefault = true)
  List<EducatorDto> fromEducatorListToEducatorDtoList(List<Educator> list);

  @Mapping(source = "account", target = "profileAccountDto", qualifiedByName = "fromAccountToProfileDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEducatorToProfileDto")
  ProfileEducatorDto fromEducatorToProfileDto(Educator Educator);
}
