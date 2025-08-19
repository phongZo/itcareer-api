package com.base.auth.service;

import com.base.auth.constant.UserBaseConstant;
import com.base.auth.dto.ErrorCode;
import com.base.auth.exception.BadRequestException;
import com.base.auth.form.GoogleLoginForm;
import com.base.auth.form.GoogleUserInfoForm;
import com.base.auth.model.Account;
import com.base.auth.model.Educator;
import com.base.auth.model.Group;
import com.base.auth.model.Student;
import com.base.auth.repository.AccountRepository;
import com.base.auth.repository.EducatorRepository;
import com.base.auth.repository.GroupRepository;
import com.base.auth.repository.StudentRepository;
import java.util.Map;
import java.util.Objects;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@Transactional
public class GoogleAuthService {
  @Autowired
  AccountRepository accountRepository;

  @Autowired
  StudentRepository studentRepository;

  @Autowired
  EducatorRepository educatorRepository;

  @Autowired
  GroupRepository groupRepository;

  final RestTemplate restTemplate = new RestTemplate();

  public Account authenticateWithGoogle(GoogleLoginForm googleLoginForm){
    GoogleUserInfoForm googleUserInfoForm = verifyGoogleToken(googleLoginForm.getAccessToken());
    if (googleUserInfoForm == null){
      throw new BadRequestException("Invalid Google access token", ErrorCode.GOOGLE_ERROR_ACCESS_TOKEN_INVALID);
    }

    Account account = accountRepository.findAccountByEmail(googleUserInfoForm.getEmail());
    if (account == null){
      Account newAccount = new Account();
      newAccount.setEmail(googleUserInfoForm.getEmail());
      newAccount.setUsername(googleUserInfoForm.getUserName());
      newAccount.setFullName(googleUserInfoForm.getFullName());
      newAccount.setAvatarPath(googleUserInfoForm.getAvatarPath());
      if ("educator".equalsIgnoreCase(googleLoginForm.getUserRole())){
        Group group = groupRepository.findFirstByKind(UserBaseConstant.USER_KIND_EDUCATOR);
        if (group != null){
          newAccount.setGroup(group);
        }
        newAccount.setStatus(UserBaseConstant.STATUS_WAITING_APPROVE);
        newAccount.setKind(UserBaseConstant.USER_KIND_EDUCATOR);
      } else {
        Group group = groupRepository.findFirstByKind(UserBaseConstant.USER_KIND_STUDENT);
        if (group != null){
          newAccount.setGroup(group);
        }
        newAccount.setKind(UserBaseConstant.USER_KIND_STUDENT);
      }
      accountRepository.save(newAccount);
      if (Objects.equals(newAccount.getKind(), UserBaseConstant.USER_KIND_STUDENT)){
        Student student = new Student();
        student.setAccount(newAccount);
        studentRepository.save(student);
      } else {
        Educator educator = new Educator();
        educator.setAccount(newAccount);
        educatorRepository.save(educator);
      }
      return newAccount;
    }
    return account;
  }

  private GoogleUserInfoForm verifyGoogleToken(String accessToken){
    try {
      String url = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;
      ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        Map<String, Object> userInfo = response.getBody();

        GoogleUserInfoForm googleUserInfo = new GoogleUserInfoForm();
        googleUserInfo.setEmail((String) userInfo.get("email"));
        googleUserInfo.setUserName((String) userInfo.get("name"));
        googleUserInfo.setFullName((String) userInfo.get("name"));
        googleUserInfo.setAvatarPath((String) userInfo.get("picture"));

        return googleUserInfo;
      }
      return null;
    } catch (Exception e) {
      log.error("Error verifying Google token: ", e);
      return null;
    }
  }
}
