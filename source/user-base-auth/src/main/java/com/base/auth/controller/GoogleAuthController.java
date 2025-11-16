package com.base.auth.controller;

import com.base.auth.constant.ITDreamConstant;
import com.base.auth.dto.ApiMessageDto;
import com.base.auth.dto.ErrorCode;
import com.base.auth.exception.BadRequestException;
import com.base.auth.form.GoogleLoginForm;
import com.base.auth.model.Account;
import com.base.auth.service.GoogleAuthService;
import com.base.auth.service.impl.UserServiceImpl;
import java.util.HashMap;
import java.util.Objects;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/google")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class GoogleAuthController{
  @Autowired
  GoogleAuthService googleAuthService;

  @Autowired
  UserServiceImpl userService;

  @Autowired
  @Qualifier("defaultAuthorizationServerTokenServices")
  AuthorizationServerTokenServices tokenServices;

  @Autowired
  ClientDetailsService clientDetailsService;

  @Value("${security.oauth2.client.client-id}")
  private String clientId;

  @PostMapping(value = "/student-login", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiMessageDto<String> studentLogin(@Valid @RequestBody GoogleLoginForm googleLoginForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    try {
      googleLoginForm.setUserRole("student");
      Account account = googleAuthService.authenticateWithGoogle(googleLoginForm);

      ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
      TokenRequest tokenRequest = new TokenRequest(new HashMap<>(), clientId, client.getScope(), "student");

      OAuth2AccessToken accessToken = userService.getAccessTokenForGoogleStudent(client, tokenRequest, googleLoginForm, tokenServices);

      apiMessageDto.setData(accessToken.getValue());
      apiMessageDto.setMessage("Login success");
    } catch (Exception e) {
      log.error("Student Google login failed: ", e);
      apiMessageDto.setMessage("Login failed: " + e.getMessage());
      apiMessageDto.setData(null);
    }
    return apiMessageDto;
  }

  @PostMapping(value = "/educator-login", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiMessageDto<String> educatorLogin(@Valid @RequestBody GoogleLoginForm googleLoginForm, BindingResult bindingResult){
    ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
    try {
      googleLoginForm.setUserRole("educator");
      Account account = googleAuthService.authenticateWithGoogle(googleLoginForm);

      if (Objects.equals(account.getKind(), ITDreamConstant.USER_KIND_EDUCATOR)){
        if (Objects.equals(account.getStatus(), ITDreamConstant.STATUS_WAITING_APPROVE)){
          apiMessageDto.setMessage("Login success. Please wait for admin approval");
          return apiMessageDto;
        } else if (!Objects.equals(account.getStatus(), ITDreamConstant.STATUS_WAITING_APPROVE) && !Objects.equals(account.getStatus(), ITDreamConstant.STATUS_ACTIVE)){
          throw new BadRequestException("Account is disabled", ErrorCode.ACCOUNT_ERROR_NOT_ACTIVE);
        }
      }

      ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
      TokenRequest tokenRequest = new TokenRequest(new HashMap<>(), clientId, client.getScope(), "educator");

      OAuth2AccessToken accessToken = userService.getAccessTokenForGoogleEducator(client, tokenRequest, googleLoginForm, tokenServices);

      apiMessageDto.setData(accessToken.getValue());
      apiMessageDto.setMessage("Login success");
    } catch (Exception e) {
      log.error("Educator Google login failed: ", e);
      apiMessageDto.setMessage("Login failed: " + e.getMessage());
      apiMessageDto.setData(null);
    }
    return apiMessageDto;
  }
}
