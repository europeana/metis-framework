package eu.europeana.metis.authentication.rest;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.metis.utils.RestEndpoints;
import eu.europeana.metis.authentication.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.authentication.service.AuthenticationService;
import eu.europeana.metis.authentication.user.Credentials;
import eu.europeana.metis.authentication.user.EmailParameter;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.OldNewPasswordParameters;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.NoUserFoundException;
import eu.europeana.metis.exception.UserAlreadyExistsException;
import eu.europeana.metis.utils.TestUtils;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-08
 */
class AuthenticationControllerTest {

  private static final String EXAMPLE_EMAIL = "example@example.com";
  private static final String EXAMPLE_PASSWORD = "123qwe456";
  private static final String EXAMPLE_ACCESS_TOKEN = "1234567890qwertyuiopasdfghjklQWE";
  private static AuthenticationService authenticationService;
  private static MockMvc authenticationControllerMock;

  @BeforeAll
  static void oneTimeSetUp() {
    authenticationService = mock(AuthenticationService.class);
    AuthenticationController authenticationController = new AuthenticationController(
        authenticationService);
    authenticationControllerMock = MockMvcBuilders
        .standaloneSetup(authenticationController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .build();
  }

  @AfterEach
  void tearDown() {
    reset(authenticationService);
  }

  @Test
  void registerUser() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithCredentials(anyString()))
        .thenReturn(new Credentials(EXAMPLE_EMAIL, EXAMPLE_PASSWORD));

    authenticationControllerMock.perform(post(RestEndpoints.AUTHENTICATION_REGISTER).header(
        HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.CREATED.value()));

    verify(authenticationService).registerUser(EXAMPLE_EMAIL, EXAMPLE_PASSWORD);
  }

  @Test
  void registerUserHeaderBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithCredentials(anyString()))
        .thenThrow(new BadContentException(""));

    authenticationControllerMock.perform(post(RestEndpoints.AUTHENTICATION_REGISTER).header(
        HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));

    verify(authenticationService, times(0)).registerUser(anyString(), anyString());
  }

  @Test
  void registerUserNoUserFoundException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithCredentials(anyString()))
        .thenReturn(new Credentials(EXAMPLE_EMAIL, EXAMPLE_PASSWORD));
    doThrow(new NoUserFoundException("")).when(authenticationService)
        .registerUser(anyString(), anyString());
    authenticationControllerMock.perform(post(RestEndpoints.AUTHENTICATION_REGISTER).header(
        HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
  }

  @Test
  void registerUserUserAlreadyExistsException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithCredentials(anyString()))
        .thenReturn(new Credentials(EXAMPLE_EMAIL, EXAMPLE_PASSWORD));
    doThrow(new UserAlreadyExistsException("")).when(authenticationService)
        .registerUser(anyString(), anyString());
    authenticationControllerMock.perform(post(RestEndpoints.AUTHENTICATION_REGISTER).header(
        HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.CONFLICT.value()));
  }

  @Test
  void loginUser() throws Exception {
    MetisUser metisUser = spy(new MetisUser());
    doReturn(EXAMPLE_EMAIL).when(metisUser).getEmail();
    when(authenticationService.validateAuthorizationHeaderWithCredentials(anyString()))
        .thenReturn(new Credentials(EXAMPLE_EMAIL, EXAMPLE_PASSWORD));
    when(authenticationService.loginUser(EXAMPLE_EMAIL, EXAMPLE_PASSWORD)).thenReturn(metisUser);

    authenticationControllerMock.perform(post(RestEndpoints.AUTHENTICATION_LOGIN).header(
        HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.OK.value()))
        .andExpect(jsonPath("$.email", is(EXAMPLE_EMAIL)))
        .andExpect(jsonPath("$.metisUserAccessToken.accessToken", is(EXAMPLE_ACCESS_TOKEN)));
  }

  @Test
  void loginUserBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithCredentials(anyString()))
        .thenThrow(new BadContentException(""));
    authenticationControllerMock.perform(post(RestEndpoints.AUTHENTICATION_LOGIN).header(
        HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));
    verify(authenticationService, times(0)).loginUser(anyString(), anyString());
  }

  @Test
  void updateUserPassword() throws Exception {
    MetisUser metisUser = spy(new MetisUser());
    doReturn(EXAMPLE_EMAIL).when(metisUser).getEmail();
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.authenticateUser(EXAMPLE_ACCESS_TOKEN)).thenReturn(metisUser);
    final OldNewPasswordParameters oldNewPasswordParameters = new OldNewPasswordParameters("123",
        "12345");

    authenticationControllerMock
        .perform(
            put(RestEndpoints.AUTHENTICATION_UPDATE_PASSD)
                .header(HttpHeaders.AUTHORIZATION, "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.convertObjectToJsonBytes(oldNewPasswordParameters)))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    verify(authenticationService)
        .authenticateUser(EXAMPLE_EMAIL, oldNewPasswordParameters.getOldPassword());
    verify(authenticationService)
        .updateUserPassword(EXAMPLE_EMAIL, oldNewPasswordParameters.getNewPassword());
  }

  @Test
  void updateUserPasswordBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenThrow(new BadContentException(""));
    final OldNewPasswordParameters oldNewPasswordParameters = new OldNewPasswordParameters("123",
        "12345");
    authenticationControllerMock
        .perform(
            put(RestEndpoints.AUTHENTICATION_UPDATE_PASSD)
                .header(HttpHeaders.AUTHORIZATION, "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.convertObjectToJsonBytes(oldNewPasswordParameters)))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));

    verify(authenticationService, times(0)).updateUserPassword(anyString(), anyString());
  }

  @Test
  void updateUserPasswordBadContentExceptionNewPasswordEmpty() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    final OldNewPasswordParameters oldNewPasswordParameters = new OldNewPasswordParameters("123",
        null);
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE_PASSD)
            .header(HttpHeaders.AUTHORIZATION, "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.convertObjectToJsonBytes(oldNewPasswordParameters)))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));
    verify(authenticationService, times(0)).updateUserPassword(anyString(), anyString());
  }

  @Test
  void deleteUser() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.isUserAdmin(EXAMPLE_ACCESS_TOKEN)).thenReturn(true);
    final EmailParameter emailParameter = new EmailParameter(EXAMPLE_EMAIL);

    authenticationControllerMock
        .perform(
            delete(RestEndpoints.AUTHENTICATION_DELETE)
                .header(HttpHeaders.AUTHORIZATION, "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.convertObjectToJsonBytes(emailParameter)))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    verify(authenticationService).deleteUser(anyString());
  }

  @Test
  void deleteUserBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenThrow(new BadContentException(""));
    final EmailParameter emailParameter = new EmailParameter(EXAMPLE_EMAIL);
    authenticationControllerMock
        .perform(
            delete(RestEndpoints.AUTHENTICATION_DELETE)
                .header(HttpHeaders.AUTHORIZATION, "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.convertObjectToJsonBytes(emailParameter)))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));

    verify(authenticationService, times(0)).deleteUser(anyString());
  }

  @Test
  void deleteUserUserUnauthorizedException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.isUserAdmin(EXAMPLE_ACCESS_TOKEN)).thenReturn(false);

    final EmailParameter emailParameter = new EmailParameter(EXAMPLE_EMAIL);
    authenticationControllerMock
        .perform(
            delete(RestEndpoints.AUTHENTICATION_DELETE)
                .header(HttpHeaders.AUTHORIZATION, "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.convertObjectToJsonBytes(emailParameter)))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));

    verify(authenticationService, times(0)).deleteUser(anyString());
  }

  @Test
  void updateUser() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);

    when(
        authenticationService.hasPermissionToRequestUserUpdate(EXAMPLE_ACCESS_TOKEN, EXAMPLE_EMAIL))
        .thenReturn(true);

    final EmailParameter emailParameter = new EmailParameter(EXAMPLE_EMAIL);
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE)
            .header(HttpHeaders.AUTHORIZATION, "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.convertObjectToJsonBytes(emailParameter)))
        .andExpect(status().is(HttpStatus.OK.value()));
    verify(authenticationService).updateUserFromZoho(EXAMPLE_EMAIL);
  }

  @Test
  void updateUserBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenThrow(new BadContentException(""));

    final EmailParameter emailParameter = new EmailParameter(EXAMPLE_EMAIL);
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE)
            .header(HttpHeaders.AUTHORIZATION, "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.convertObjectToJsonBytes(emailParameter)))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));
    verify(authenticationService, times(0))
        .hasPermissionToRequestUserUpdate(anyString(), anyString());
    verify(authenticationService, times(0)).updateUserFromZoho(anyString());
  }

  @Test
  void updateUserNoUserFoundException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);

    when(
        authenticationService.hasPermissionToRequestUserUpdate(EXAMPLE_ACCESS_TOKEN, EXAMPLE_EMAIL))
        .thenReturn(true);
    doThrow(new NoUserFoundException("")).when(authenticationService)
        .updateUserFromZoho(EXAMPLE_EMAIL);

    final EmailParameter emailParameter = new EmailParameter(EXAMPLE_EMAIL);
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE)
            .header(HttpHeaders.AUTHORIZATION, "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.convertObjectToJsonBytes(emailParameter)))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
  }

  @Test
  void updateUserUserUnauthorizedException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);

    when(
        authenticationService.hasPermissionToRequestUserUpdate(EXAMPLE_ACCESS_TOKEN, EXAMPLE_EMAIL))
        .thenReturn(false);

    final EmailParameter emailParameter = new EmailParameter(EXAMPLE_EMAIL);
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE)
            .header(HttpHeaders.AUTHORIZATION, "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.convertObjectToJsonBytes(emailParameter)))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    verify(authenticationService, times(0)).updateUserFromZoho(anyString());
  }

  @Test
  void updateUserToMakeAdmin() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.isUserAdmin(EXAMPLE_ACCESS_TOKEN)).thenReturn(true);

    final EmailParameter emailParameter = new EmailParameter(EXAMPLE_EMAIL);
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE_ROLE_ADMIN)
            .header(HttpHeaders.AUTHORIZATION, "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.convertObjectToJsonBytes(emailParameter)))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    verify(authenticationService).updateUserMakeAdmin(EXAMPLE_EMAIL);
  }

  @Test
  void updateUserToMakeAdminBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenThrow(new BadContentException(""));

    final EmailParameter emailParameter = new EmailParameter(EXAMPLE_EMAIL);
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE_ROLE_ADMIN)
            .header(HttpHeaders.AUTHORIZATION, "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.convertObjectToJsonBytes(emailParameter)))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));
    verify(authenticationService, times(0)).isUserAdmin(anyString());
    verify(authenticationService, times(0)).updateUserMakeAdmin(anyString());
  }

  @Test
  void updateUserToMakeAdminNoUserFoundException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.isUserAdmin(EXAMPLE_ACCESS_TOKEN)).thenReturn(true);
    doThrow(new NoUserFoundException("")).when(authenticationService)
        .updateUserMakeAdmin(EXAMPLE_EMAIL);

    final EmailParameter emailParameter = new EmailParameter(EXAMPLE_EMAIL);
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE_ROLE_ADMIN)
            .header(HttpHeaders.AUTHORIZATION, "")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.convertObjectToJsonBytes(emailParameter)))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
  }

  @Test
  void updateUserToMakeAdminUserUnauthorizedException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.isUserAdmin(EXAMPLE_ACCESS_TOKEN)).thenReturn(false);

    final EmailParameter emailParameter = new EmailParameter(EXAMPLE_EMAIL);
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE_ROLE_ADMIN)
            .header(HttpHeaders.AUTHORIZATION, "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.convertObjectToJsonBytes(emailParameter)))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    verify(authenticationService, times(0)).updateUserMakeAdmin(anyString());
  }

  @Test
  void getUserByAccessToken() throws Exception {
    MetisUser metisUser = spy(new MetisUser());
    doReturn(EXAMPLE_EMAIL).when(metisUser).getEmail();
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.authenticateUser(EXAMPLE_ACCESS_TOKEN)).thenReturn(metisUser);

    authenticationControllerMock
        .perform(get(RestEndpoints.AUTHENTICATION_USER_BY_TOKEN)
            .header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.OK.value()))
        .andExpect(jsonPath("$.metisUserAccessToken.accessToken", is(EXAMPLE_ACCESS_TOKEN)));
  }

  @Test
  void getUserByAccessTokenBadContentException() throws Exception {
    MetisUser metisUser = spy(new MetisUser());
    doReturn(EXAMPLE_EMAIL).when(metisUser).getEmail();

    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenThrow(new BadContentException(""));

    authenticationControllerMock
        .perform(get(RestEndpoints.AUTHENTICATION_USER_BY_TOKEN)
            .header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));
  }


  @Test
  void getAllUsers() throws Exception {
    MetisUser metisUser0 = spy(new MetisUser());
    doReturn(EXAMPLE_EMAIL).when(metisUser0).getEmail();
    MetisUser metisUser1 = spy(new MetisUser());
    doReturn(EXAMPLE_EMAIL).when(metisUser1).getEmail();
    ArrayList<MetisUser> metisUsers = new ArrayList<>();
    metisUsers.add(metisUser0);
    metisUsers.add(metisUser1);

    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.hasPermissionToRequestAllUsers(EXAMPLE_ACCESS_TOKEN))
        .thenReturn(true);
    when(authenticationService.getAllUsers()).thenReturn(metisUsers);
    authenticationControllerMock
        .perform(get(RestEndpoints.AUTHENTICATION_USERS).header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.OK.value()))
        .andExpect(jsonPath("$[0].email", is("0" + EXAMPLE_EMAIL)))
        .andExpect(jsonPath("$[1].email", is("1" + EXAMPLE_EMAIL)));
  }

  @Test
  void getAllUsersBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenThrow(new BadContentException(""));
    authenticationControllerMock
        .perform(get(RestEndpoints.AUTHENTICATION_USERS).header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));
  }

  @Test
  void getAllUsersUserUnauthorizedException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.hasPermissionToRequestAllUsers(EXAMPLE_ACCESS_TOKEN))
        .thenReturn(false);
    authenticationControllerMock
        .perform(get(RestEndpoints.AUTHENTICATION_USERS).header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));

    verify(authenticationService, times(0)).getAllUsers();
  }
}