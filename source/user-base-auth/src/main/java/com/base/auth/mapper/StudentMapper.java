package com.base.auth.mapper;

import com.base.auth.dto.student.ProfileStudentDto;
import com.base.auth.dto.student.StudentDto;
import com.base.auth.model.Student;
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
public interface StudentMapper {
  @Mapping(source = "id",target = "id")
  @Mapping(source ="account",target = "account",qualifiedByName="fromAccountToDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToStudentDto")
  StudentDto fromEntityToStudentDto(Student student);

  @IterableMapping(elementTargetType = StudentDto.class,qualifiedByName = "fromEntityToStudentDto")
  @BeanMapping(ignoreByDefault = true)
  List<StudentDto> fromStudentListToStudentDtoList(List<Student> list);

  @Mapping(source = "account", target = "profileAccountDto", qualifiedByName = "fromAccountToProfileDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromStudentToProfileDto")
  ProfileStudentDto fromStudentToProfileDto(Student student);

  @IterableMapping(elementTargetType = ProfileStudentDto.class,qualifiedByName = "fromStudentToProfileDto")
  @BeanMapping(ignoreByDefault = true)
  List<ProfileStudentDto> fromStudentToProfileDtoList(List<Student> list);
}
