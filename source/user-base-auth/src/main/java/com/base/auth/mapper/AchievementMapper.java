package com.base.auth.mapper;

import com.base.auth.dto.achievement.AchievementDto;
import com.base.auth.dto.achievement.AchievementStudentDto;
import com.base.auth.model.Achievement;
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
    uses = {SimulationMapper.class, StudentMapper.class})
public interface AchievementMapper {
  @Mapping(source = "id", target = "id")
  @Mapping(source = "filePath", target = "filePath")
  @Mapping(source = "createdDate", target = "createdDate")
  @Mapping(source = "modifiedDate", target = "modifiedDate")
  @Mapping(source = "simulation", target = "simulation", qualifiedByName = "fromEntityToSimulationDto")
  @Mapping(source = "student", target = "student", qualifiedByName = "fromEntityToStudentDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToAchievementDto")
  AchievementDto fromEntityToAchievementDto(Achievement achievement);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "filePath", target = "filePath")
  @Mapping(source = "simulation", target = "simulation", qualifiedByName = "fromEntityToSimulationDisplayDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToAchievementStudentDto")
  AchievementStudentDto fromEntityToAchievementStudentDto(Achievement achievement);

  @IterableMapping(elementTargetType = AchievementDto.class, qualifiedByName = "fromEntityToAchievementDto")
  List<AchievementDto> fromEntityToAchievementDtoList(List<Achievement> achievements);

  @IterableMapping(elementTargetType = AchievementStudentDto.class, qualifiedByName = "fromEntityToAchievementStudentDto")
  List<AchievementStudentDto> fromEntityToAchievementStudentDtoList(List<Achievement> achievements);
}
