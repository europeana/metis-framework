package eu.europeana.metis.authentication.rest;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.user.Credentials;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.NoUserFoundException;
import eu.europeana.metis.exception.UserAlreadyExistsException;
import eu.europeana.metis.authentication.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.authentication.service.AuthenticationService;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import java.util.ArrayList;
import java.util.Date;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-08
 */
public class AuthenticationControllerTest {

  private static final String EXAMPLE_EMAIL = "example@example.com";
  private static final String EXAMPLE_PASSWORD = "123qwe456";
  private static final String EXAMPLE_ACCESS_TOKEN = "1234567890qwertyuiopasdfghjklQWE";
  private static AuthenticationService authenticationService;
  private static MockMvc authenticationControllerMock;

  @BeforeClass
  public static void oneTimeSetUp() {
    authenticationService = mock(AuthenticationService.class);
    AuthenticationController authenticationController = new AuthenticationController(
        authenticationService);
    authenticationControllerMock = MockMvcBuilders
        .standaloneSetup(authenticationController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .build();
  }

  @After
  public void tearDown() {
    reset(authenticationService);
  }

  @Test
  public void registerUser() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithCredentials(anyString()))
        .thenReturn(new Credentials(EXAMPLE_EMAIL, EXAMPLE_PASSWORD));

    authenticationControllerMock.perform(post(RestEndpoints.AUTHENTICATION_REGISTER).header(
        HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.CREATED.value()));

    verify(authenticationService).registerUser(EXAMPLE_EMAIL, EXAMPLE_PASSWORD);
  }

  @Test
  public void registerUserHeaderBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithCredentials(anyString()))
        .thenThrow(new BadContentException(""));

    authenticationControllerMock.perform(post(RestEndpoints.AUTHENTICATION_REGISTER).header(
        HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));

    verify(authenticationService, times(0)).registerUser(anyString(), anyString());
  }

  @Test
  public void registerUserNoUserFoundException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithCredentials(anyString()))
        .thenReturn(new Credentials(EXAMPLE_EMAIL, EXAMPLE_PASSWORD));
    doThrow(new NoUserFoundException("")).when(authenticationService)
        .registerUser(anyString(), anyString());
    authenticationControllerMock.perform(post(RestEndpoints.AUTHENTICATION_REGISTER).header(
        HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
  }

  @Test
  public void registerUserUserAlreadyExistsException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithCredentials(anyString()))
        .thenReturn(new Credentials(EXAMPLE_EMAIL, EXAMPLE_PASSWORD));
    doThrow(new UserAlreadyExistsException("")).when(authenticationService)
        .registerUser(anyString(), anyString());
    authenticationControllerMock.perform(post(RestEndpoints.AUTHENTICATION_REGISTER).header(
        HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.CONFLICT.value()));
  }

  @Test
  public void loginUser() throws Exception {
    MetisUser metisUser = new MetisUser();
    metisUser.setEmail(EXAMPLE_EMAIL);
    metisUser.setMetisUserAccessToken(
        new MetisUserAccessToken(EXAMPLE_EMAIL, EXAMPLE_ACCESS_TOKEN, new Date()));
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
  public void loginUserBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithCredentials(anyString()))
        .thenThrow(new BadContentException(""));
    authenticationControllerMock.perform(post(RestEndpoints.AUTHENTICATION_LOGIN).header(
        HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));
    verify(authenticationService, times(0)).loginUser(anyString(), anyString());
  }

  @Test
  public void updateUserPassword() throws Exception {
    MetisUser metisUser = new MetisUser();
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.authenticateUser(EXAMPLE_ACCESS_TOKEN)).thenReturn(metisUser);
    final String oldPassword = "123";
    final String newPassword = "12345";

    authenticationControllerMock
        .perform(
            put(RestEndpoints.AUTHENTICATION_UPDATE_PASSD).param("oldPassword", oldPassword)
                .param("newPassword", newPassword).header(
                HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    verify(authenticationService).authenticateUser(metisUser.getEmail(), oldPassword);
    verify(authenticationService).updateUserPassword(metisUser, newPassword);
  }

  @Test
  public void updateUserPasswordBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenThrow(new BadContentException(""));
    final String oldPassword = "123";
    final String newPassword = "12345";
    authenticationControllerMock
        .perform(
            put(RestEndpoints.AUTHENTICATION_UPDATE_PASSD).param("oldPassword", oldPassword)
                .param("newPassword", newPassword).header(
                HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));

    verify(authenticationService, times(0)).updateUserPassword(any(MetisUser.class), anyString());
  }

  @Test
  public void updateUserPasswordBadContentExceptionNewPasswordEmpty() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    final String oldPassword = "123";
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE_PASSD).param("oldPassword", oldPassword)
            .param("newPassword", "  ").header(
                HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));
    verify(authenticationService, times(0)).updateUserPassword(any(MetisUser.class), anyString());
  }

  @Test
  public void deleteUser() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.isUserAdmin(EXAMPLE_ACCESS_TOKEN)).thenReturn(true);

    authenticationControllerMock
        .perform(
            delete(RestEndpoints.AUTHENTICATION_DELETE).param("userEmailToDelete", EXAMPLE_EMAIL)
                .header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    verify(authenticationService).deleteUser(anyString());
  }

  @Test
  public void deleteUserBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenThrow(new BadContentException(""));
    authenticationControllerMock
        .perform(
            delete(RestEndpoints.AUTHENTICATION_DELETE).param("userEmailToDelete", EXAMPLE_EMAIL)
                .header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));

    verify(authenticationService, times(0)).deleteUser(anyString());
  }

  @Test
  public void deleteUserUserUnauthorizedException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.isUserAdmin(EXAMPLE_ACCESS_TOKEN)).thenReturn(false);
    authenticationControllerMock
        .perform(
            delete(RestEndpoints.AUTHENTICATION_DELETE).param("userEmailToDelete", EXAMPLE_EMAIL)
                .header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));

    verify(authenticationService, times(0)).deleteUser(anyString());
  }

  @Test
  public void updateUser() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);

    when(
        authenticationService.hasPermissionToRequestUserUpdate(EXAMPLE_ACCESS_TOKEN, EXAMPLE_EMAIL))
        .thenReturn(true);

    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE).param("userEmailToUpdate", EXAMPLE_EMAIL)
            .header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.OK.value()));
    verify(authenticationService).updateUserFromZoho(EXAMPLE_EMAIL);
  }

  @Test
  public void updateUserBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenThrow(new BadContentException(""));
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE).param("userEmailToUpdate", EXAMPLE_EMAIL)
            .header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));
    verify(authenticationService, times(0))
        .hasPermissionToRequestUserUpdate(anyString(), anyString());
    verify(authenticationService, times(0)).updateUserFromZoho(anyString());
  }

  @Test
  public void updateUserNoUserFoundException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);

    when(
        authenticationService.hasPermissionToRequestUserUpdate(EXAMPLE_ACCESS_TOKEN, EXAMPLE_EMAIL))
        .thenReturn(true);
    doThrow(new NoUserFoundException("")).when(authenticationService)
        .updateUserFromZoho(EXAMPLE_EMAIL);

    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE).param("userEmailToUpdate", EXAMPLE_EMAIL)
            .header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
  }

  @Test
  public void updateUserUserUnauthorizedException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);

    when(
        authenticationService.hasPermissionToRequestUserUpdate(EXAMPLE_ACCESS_TOKEN, EXAMPLE_EMAIL))
        .thenReturn(false);

    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE).param("userEmailToUpdate", EXAMPLE_EMAIL)
            .header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    verify(authenticationService, times(0)).updateUserFromZoho(anyString());
  }

  @Test
  public void updateUserToMakeAdmin() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.isUserAdmin(EXAMPLE_ACCESS_TOKEN)).thenReturn(true);
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE_ROLE_ADMIN)
            .param("userEmailToMakeAdmin", EXAMPLE_EMAIL).header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    verify(authenticationService).updateUserMakeAdmin(EXAMPLE_EMAIL);
  }

  @Test
  public void updateUserToMakeAdminBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenThrow(new BadContentException(""));
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE_ROLE_ADMIN)
            .param("userEmailToMakeAdmin", EXAMPLE_EMAIL).header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));
    verify(authenticationService, times(0)).isUserAdmin(anyString());
    verify(authenticationService, times(0)).updateUserMakeAdmin(anyString());
  }

  @Test
  public void updateUserToMakeAdminNoUserFoundException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.isUserAdmin(EXAMPLE_ACCESS_TOKEN)).thenReturn(true);
    doThrow(new NoUserFoundException("")).when(authenticationService)
        .updateUserMakeAdmin(EXAMPLE_EMAIL);
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE_ROLE_ADMIN)
            .param("userEmailToMakeAdmin", EXAMPLE_EMAIL).header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
  }

  @Test
  public void updateUserToMakeAdminUserUnauthorizedException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.isUserAdmin(EXAMPLE_ACCESS_TOKEN)).thenReturn(false);
    authenticationControllerMock
        .perform(put(RestEndpoints.AUTHENTICATION_UPDATE_ROLE_ADMIN)
            .param("userEmailToMakeAdmin", EXAMPLE_EMAIL).header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    verify(authenticationService, times(0)).updateUserMakeAdmin(anyString());
  }

  @Test
  public void getUserByAccessToken() throws Exception {
    MetisUser metisUser = new MetisUser();
    metisUser.setEmail(EXAMPLE_EMAIL);
    metisUser.setMetisUserAccessToken(
        new MetisUserAccessToken(EXAMPLE_EMAIL, EXAMPLE_ACCESS_TOKEN, new Date()));
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
  public void getUserByAccessTokenBadContentException() throws Exception {
    MetisUser metisUser = new MetisUser();
    metisUser.setEmail(EXAMPLE_EMAIL);
    metisUser.setMetisUserAccessToken(
        new MetisUserAccessToken(EXAMPLE_EMAIL, EXAMPLE_ACCESS_TOKEN, new Date()));

    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenThrow(new BadContentException(""));

    authenticationControllerMock
        .perform(get(RestEndpoints.AUTHENTICATION_USER_BY_TOKEN)
            .header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));
  }


  @Test
  public void getAllUsers() throws Exception {
    MetisUser metisUser0 = new MetisUser();
    metisUser0.setEmail("0" + EXAMPLE_EMAIL);
    MetisUser metisUser1 = new MetisUser();
    metisUser1.setEmail("1" + EXAMPLE_EMAIL);
    ArrayList<MetisUser> metisUsers = new ArrayList<>();
    metisUsers.add(metisUser0);
    metisUsers.add(metisUser1);

    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.hasPermissionToRequestAllUsers(EXAMPLE_ACCESS_TOKEN))
        .thenReturn(true);
    when(authenticationService.getAllUsers(EXAMPLE_ACCESS_TOKEN)).thenReturn(metisUsers);
    authenticationControllerMock
        .perform(get(RestEndpoints.AUTHENTICATION_USERS).header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.OK.value()))
        .andExpect(jsonPath("$[0].email", is("0" + EXAMPLE_EMAIL)))
        .andExpect(jsonPath("$[1].email", is("1" + EXAMPLE_EMAIL)));
  }

  @Test
  public void getAllUsersBadContentException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenThrow(new BadContentException(""));
    authenticationControllerMock
        .perform(get(RestEndpoints.AUTHENTICATION_USERS).header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.NOT_ACCEPTABLE.value()));
  }

  @Test
  public void getAllUsersUserUnauthorizedException() throws Exception {
    when(authenticationService.validateAuthorizationHeaderWithAccessToken(anyString()))
        .thenReturn(EXAMPLE_ACCESS_TOKEN);
    when(authenticationService.hasPermissionToRequestAllUsers(EXAMPLE_ACCESS_TOKEN))
        .thenReturn(false);
    authenticationControllerMock
        .perform(get(RestEndpoints.AUTHENTICATION_USERS).header(HttpHeaders.AUTHORIZATION, ""))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));

    verify(authenticationService, times(0)).getAllUsers(EXAMPLE_ACCESS_TOKEN);
  }
}