package com.base.auth.mapper;

import com.base.auth.dto.account.AccountDto;
import com.base.auth.dto.account.ProfileAccountDto;
import com.base.auth.form.account.CreateAccountAdminForm;
import com.base.auth.form.account.UpdateAccountAdminForm;
import com.base.auth.form.account.UpdateProfileAdminForm;
import com.base.auth.form.educator.SignUpEducatorForm;
import com.base.auth.form.educator.UpdateEducatorForm;
import com.base.auth.form.educator.UpdateProfileEducatorForm;
import com.base.auth.form.student.SignUpStudentForm;
import com.base.auth.form.student.UpdateProfileStudentForm;
import com.base.auth.form.student.UpdateStudentForm;
import com.base.auth.model.Account;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {GroupMapper.class})
public interface AccountMapper {

    @Mapping(source = "username", target = "username")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "birthday", target = "birthday")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    Account fromCreateAccountAdmiFormToAccount(CreateAccountAdminForm createAccountAdminForm);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "birthday", target = "birthday")
    @Mapping(source = "group", target = "group", qualifiedByName = "fromEntityToGroupDto")
    @Mapping(source = "lastLogin", target = "lastLogin")
    @Mapping(source = "avatarPath", target = "avatar")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "isSuperAdmin", target = "isSuperAdmin")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromAccountToDto")
    AccountDto fromAccountToDto(Account account);

    @IterableMapping(elementTargetType = AccountDto.class, qualifiedByName = "fromAccountToDto")
    List<AccountDto> fromAccountToDtoList(List<Account> accounts);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "birthday", target = "birthday")
    @BeanMapping(ignoreByDefault = true)
    Account fromSignUpStudentToAccount(SignUpStudentForm signUpStudentForm);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "birthday", target = "birthday")
    @BeanMapping(ignoreByDefault = true)
    Account fromSignUpEducatorToAccount(SignUpEducatorForm signUpEducatorForm);

    @Mapping(source = "fullName",target = "fullName")
    @Mapping(source = "birthday", target = "birthday")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateStudentFormToEntity(UpdateStudentForm updateStudentForm, @MappingTarget Account account );

    @Mapping(source = "fullName",target = "fullName")
    @Mapping(source = "birthday", target = "birthday")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateEducatorFormToEntity(UpdateEducatorForm updateEducatorForm, @MappingTarget Account account );

    @Mapping(source = "username", target = "username")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "birthday", target = "birthday")
    @Mapping(source = "avatarPath", target = "avatar")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromAccountToProfileDto")
    ProfileAccountDto fromAccountToProfileDto(Account account);

    @Mapping(source = "fullname", target = "fullName")
    @Mapping(source = "birthday", target = "birthday")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateProfileStudentFormToEntity(UpdateProfileStudentForm updateProfileStudentForm, @MappingTarget Account account);

    @Mapping(source = "fullname", target = "fullName")
    @Mapping(source = "birthday", target = "birthday")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateProfileEducatorFormToEntity(UpdateProfileEducatorForm updateProfileEducatorForm, @MappingTarget Account account);

    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "birthday", target = "birthday")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateAccountAdminFormToAccount(UpdateAccountAdminForm updateAccountAdminForm, @MappingTarget Account account);

    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "birthday", target = "birthday")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateProfileAdminFormToAccount(UpdateProfileAdminForm updateProfileAdminForm, @MappingTarget Account account);
}
