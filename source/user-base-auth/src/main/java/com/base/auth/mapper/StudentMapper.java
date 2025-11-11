package com.base.auth.mapper;

import com.base.auth.dto.student.ProfileStudentDto;
import com.base.auth.dto.student.StudentAutoCompleteDto;
import com.base.auth.dto.student.StudentDto;
import com.base.auth.form.student.UpdateProfileStudentForm;
import com.base.auth.model.Student;
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
public interface StudentMapper {
  @Mapping(source = "id",target = "id")
  @Mapping(source = "birthday",target = "birthday")
  @Mapping(source ="account",target = "account",qualifiedByName="fromAccountToDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromEntityToStudentDto")
  StudentDto fromEntityToStudentDto(Student student);

  @Mapping(source = "id",target = "id")
  @Mapping(source ="account",target = "accountAutoCompleteDto",qualifiedByName="fromAccountToAutoCompleteDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromStudentToDtoAutoComplete")
  StudentAutoCompleteDto fromStudentToDtoAutoComplete(Student student);

  @IterableMapping(elementTargetType = StudentDto.class,qualifiedByName = "fromEntityToStudentDto")
  @BeanMapping(ignoreByDefault = true)
  List<StudentDto> fromStudentListToStudentDtoList(List<Student> list);

  @IterableMapping(elementTargetType = StudentAutoCompleteDto.class,qualifiedByName = "fromStudentToDtoAutoComplete")
  @BeanMapping(ignoreByDefault = true)
  List<StudentAutoCompleteDto> fromStudentListToStudentDtoListAutocomplete(List<Student> list);

  @Mapping(source = "birthday", target = "birthday")
  @Mapping(source = "account", target = "profileAccountDto", qualifiedByName = "fromAccountToProfileDto")
  @BeanMapping(ignoreByDefault = true)
  @Named("fromStudentToProfileDto")
  ProfileStudentDto fromStudentToProfileDto(Student student);

  @IterableMapping(elementTargetType = ProfileStudentDto.class,qualifiedByName = "fromStudentToProfileDto")
  @BeanMapping(ignoreByDefault = true)
  List<ProfileStudentDto> fromStudentToProfileDtoList(List<Student> list);

  @Mapping(source = "birthday", target = "birthday")
  @BeanMapping(ignoreByDefault = true)
  void fromUpdateProfileStudentFormToEntity(
      UpdateProfileStudentForm updateProfileStudentForm, @MappingTarget Student student);
}
