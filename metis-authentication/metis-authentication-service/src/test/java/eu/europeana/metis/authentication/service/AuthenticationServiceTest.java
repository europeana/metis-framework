package eu.europeana.metis.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zoho.crm.library.crud.ZCRMRecord;
import eu.europeana.metis.authentication.dao.PsqlMetisUserDao;
import eu.europeana.metis.zoho.ZohoAccessClient;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.Credentials;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import eu.europeana.metis.authentication.user.MetisUserModel;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.NoUserFoundException;
import eu.europeana.metis.exception.UserAlreadyExistsException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-07
 */
class AuthenticationServiceTest {

  private static final String EXAMPLE_EMAIL = "example@example.com";
  private static final String EXAMPLE_PASSWORD = "123qwe456";
  private static final String EXAMPLE_ACCESS_TOKEN = "1234567890qwertyuiopasdfghjklQWE";
  private static PsqlMetisUserDao psqlMetisUserDao;
  private static ZohoAccessClient zohoAccessClient;
  private static AuthenticationService authenticationService;

  @BeforeAll
  static void setUp() {
    psqlMetisUserDao = Mockito.mock(PsqlMetisUserDao.class);
    zohoAccessClient = Mockito.mock(ZohoAccessClient.class);
    authenticationService = new AuthenticationService(psqlMetisUserDao, zohoAccessClient);
  }

  @AfterEach
  void cleanUp() {
    Mockito.reset(psqlMetisUserDao);
    Mockito.reset(zohoAccessClient);
  }

  @Test
  void registerUser() throws Exception {
    when(psqlMetisUserDao.getMetisUserByEmail(anyString())).thenReturn(null);

    final ZCRMRecord zcrmRecordContactWithAccountInTheFields = getZCRMRecordContactWithAccountInTheFields();
    when(zohoAccessClient.getZcrmRecordContactByEmail(anyString()))
        .thenReturn(zcrmRecordContactWithAccountInTheFields);
    when(zohoAccessClient.getZcrmRecordOrganizationByName(anyString()))
        .thenReturn(
            (ZCRMRecord) zcrmRecordContactWithAccountInTheFields.getFieldValue("Account_Name"));
    authenticationService.registerUser(EXAMPLE_EMAIL, EXAMPLE_PASSWORD);
    verify(psqlMetisUserDao).createMetisUser(any(MetisUserModel.class));
  }

  @Test
  void registerUserAlreadyExistsInDB() {
    MetisUserModel metisUser = new MetisUserModel();
    metisUser.setEmail(EXAMPLE_EMAIL);
    when(psqlMetisUserDao.getMetisUserByEmail(anyString())).thenReturn(metisUser);
    assertThrows(UserAlreadyExistsException.class,
        () -> authenticationService.registerUser(EXAMPLE_EMAIL, EXAMPLE_PASSWORD));
  }

  @Test
  void registerUserFailsOnZohoUserRetrieval() throws Exception {
    when(psqlMetisUserDao.getMetisUserByEmail(anyString())).thenReturn(null);
    when(zohoAccessClient.getZcrmRecordContactByEmail(anyString()))
        .thenThrow(new BadContentException("Exception"));
    assertThrows(BadContentException.class,
        () -> authenticationService.registerUser(EXAMPLE_EMAIL, EXAMPLE_PASSWORD));
  }

  @Test
  void registerUserDoesNotExistInZoho() throws Exception {
    when(psqlMetisUserDao.getMetisUserByEmail(anyString())).thenReturn(null);
    when(zohoAccessClient.getZcrmRecordContactByEmail(anyString())).thenReturn(null);
    assertThrows(NoUserFoundException.class,
        () -> authenticationService.registerUser(EXAMPLE_EMAIL, EXAMPLE_PASSWORD));
  }


  @Test
  void registerUserParsingUserFromZohoNoOrganizationNameProvided() throws Exception {
    final ZCRMRecord zcrmRecordContactWithAccountInTheFieldsNoOrganizationName = getZCRMRecordContactWithAccountInTheFieldsNoOrganizationName();
    when(psqlMetisUserDao.getMetisUserByEmail(anyString())).thenReturn(null);
    when(zohoAccessClient.getZcrmRecordContactByEmail(anyString()))
        .thenReturn(zcrmRecordContactWithAccountInTheFieldsNoOrganizationName);
    when(zohoAccessClient.getZcrmRecordOrganizationByName(anyString()))
        .thenReturn(
            (ZCRMRecord) zcrmRecordContactWithAccountInTheFieldsNoOrganizationName
                .getFieldValue("Account_Name"));
    assertThrows(BadContentException.class,
        () -> authenticationService.registerUser(EXAMPLE_EMAIL, EXAMPLE_PASSWORD));

  }

  @Test
  void registerUserFailsOnZohoOrganizationRetrieval() throws Exception {
    when(psqlMetisUserDao.getMetisUserByEmail(anyString())).thenReturn(null);
    when(zohoAccessClient.getZcrmRecordContactByEmail(anyString()))
        .thenReturn(getZCRMRecordContactWithAccountInTheFields());
    when(zohoAccessClient.getZcrmRecordOrganizationByName(anyString())).thenReturn(null);
    assertThrows(BadContentException.class,
        () -> authenticationService.registerUser(EXAMPLE_EMAIL, EXAMPLE_PASSWORD));
  }

  @Test
  void updateUserFromZoho() throws Exception {
    final ZCRMRecord zcrmRecordContactWithAccountInTheFields = getZCRMRecordContactWithAccountInTheFields();

    MetisUserModel metisUser = new MetisUserModel();
    when(psqlMetisUserDao.getMetisUserByEmail(anyString())).thenReturn(metisUser);
    when(zohoAccessClient.getZcrmRecordContactByEmail(anyString()))
        .thenReturn(zcrmRecordContactWithAccountInTheFields);
    when(zohoAccessClient.getZcrmRecordOrganizationByName(anyString()))
        .thenReturn(
            (ZCRMRecord) zcrmRecordContactWithAccountInTheFields.getFieldValue("Account_Name"));
    authenticationService.updateUserFromZoho(EXAMPLE_EMAIL);
    verify(psqlMetisUserDao).updateMetisUser(any(MetisUserModel.class));
  }

  @Test
  void updateUserFromZohoAnAdminStaysAdmin() throws Exception {
    final ZCRMRecord zcrmRecordContactWithAccountInTheFields = getZCRMRecordContactWithAccountInTheFields();

    MetisUserModel metisUser = new MetisUserModel();
    metisUser.setAccountRole(AccountRole.METIS_ADMIN);
    when(psqlMetisUserDao.getMetisUserByEmail(anyString())).thenReturn(metisUser);
    when(zohoAccessClient.getZcrmRecordContactByEmail(anyString()))
        .thenReturn(zcrmRecordContactWithAccountInTheFields);
    when(zohoAccessClient.getZcrmRecordOrganizationByName(anyString()))
        .thenReturn(
            (ZCRMRecord) zcrmRecordContactWithAccountInTheFields.getFieldValue("Account_Name"));
    ArgumentCaptor<MetisUserModel> metisUserArgumentCaptor = ArgumentCaptor.forClass(MetisUserModel.class);
    authenticationService.updateUserFromZoho(EXAMPLE_EMAIL);
    verify(psqlMetisUserDao).updateMetisUser(metisUserArgumentCaptor.capture());
    assertEquals(AccountRole.METIS_ADMIN, metisUserArgumentCaptor.getValue().getAccountRole());
  }

  @Test
  void updateUserFromZohoNoUserFound() {
    when(psqlMetisUserDao.getMetisUserByEmail(anyString())).thenReturn(null);
    assertThrows(NoUserFoundException.class,
        () -> authenticationService.updateUserFromZoho(EXAMPLE_EMAIL));
    verify(psqlMetisUserDao, times(0)).updateMetisUser(any(MetisUserModel.class));
  }

  @Test
  void validateAuthorizationHeaderWithCredentials() throws Exception {
    String authenticationString = EXAMPLE_EMAIL + ":" + EXAMPLE_PASSWORD;
    byte[] base64AuthenticationBytes = Base64.encodeBase64(authenticationString.getBytes());
    String authorizationHeader = "Basic " + new String(base64AuthenticationBytes);

    Credentials credentials = authenticationService
        .validateAuthorizationHeaderWithCredentials(authorizationHeader);
    assertEquals(EXAMPLE_EMAIL, credentials.getEmail());
    assertEquals(EXAMPLE_PASSWORD, credentials.getPassword());
  }

  @Test
  void validateAuthorizationHeaderWithCredentialsAuthorizationHeaderEmtpy() {
    assertThrows(BadContentException.class,
        () -> authenticationService.validateAuthorizationHeaderWithCredentials(""));
  }

  @Test
  void validateAuthorizationHeaderWithCredentialsAuthorizationHeaderNotValid() {
    String authenticationString = EXAMPLE_EMAIL + EXAMPLE_PASSWORD;
    byte[] base64AuthenticationBytes = Base64.encodeBase64(authenticationString.getBytes());
    String authorizationHeader = "Basic " + new String(base64AuthenticationBytes);
    assertThrows(BadContentException.class, () -> authenticationService
        .validateAuthorizationHeaderWithCredentials(authorizationHeader));
  }

  @Test
  void validateAuthorizationHeaderWithCredentialsAuthorizationHeaderNotValidScheme() {
    String authenticationString = EXAMPLE_EMAIL + EXAMPLE_PASSWORD;
    byte[] base64AuthenticationBytes = Base64.encodeBase64(authenticationString.getBytes());
    String authorizationHeader = "Whatever " + new String(base64AuthenticationBytes);
    assertThrows(BadContentException.class, () -> authenticationService
        .validateAuthorizationHeaderWithCredentials(authorizationHeader));
  }

  @Test
  void validateAuthorizationHeaderWithAccessToken() throws Exception {
    String authorizationHeader = "Bearer " + EXAMPLE_ACCESS_TOKEN;
    assertEquals(EXAMPLE_ACCESS_TOKEN,
        authenticationService.validateAuthorizationHeaderWithAccessToken(authorizationHeader));
  }

  @Test
  void validateAuthorizationHeaderWithAccessTokenAuthorizationHeaderEmtpy() {
    assertThrows(UserUnauthorizedException.class,
        () -> authenticationService.validateAuthorizationHeaderWithAccessToken(""));
  }

  @Test
  void validateAuthorizationHeaderWithAccessTokenAuthorizationHeaderNotValid() {
    assertThrows(UserUnauthorizedException.class,
        () -> authenticationService.validateAuthorizationHeaderWithAccessToken("Bearer "));
  }

  @Test
  void validateAuthorizationHeaderWithAccessTokenAuthorizationHeaderNotValidCharacters() {
    String accessToken = authenticationService.generateAccessToken();
    String invalidAccessToken = "ξξ" + accessToken.substring(2);
    assertThrows(UserUnauthorizedException.class, () -> authenticationService
        .validateAuthorizationHeaderWithAccessToken("Bearer " + invalidAccessToken));
  }

  @Test
  void validateAuthorizationHeaderWithAccessTokenAuthorizationHeaderNotValidScheme() {
    assertThrows(UserUnauthorizedException.class,
        () -> authenticationService.validateAuthorizationHeaderWithAccessToken("Whatever "));
  }

  @Test
  void loginUser() throws Exception {
    MetisUserModel metisUser = registerAndCaptureMetisUser();
    when(psqlMetisUserDao.getMetisUserByEmail(anyString())).thenReturn(metisUser);
    authenticationService.loginUser(EXAMPLE_EMAIL, EXAMPLE_PASSWORD);
    verify(psqlMetisUserDao).createUserAccessToken(any(MetisUserAccessToken.class));
  }

  @Test
  void loginUserTokenExistsSoUpdateTimestamp() throws Exception {
    MetisUserModel metisUser = registerAndCaptureMetisUser();
    metisUser.setMetisUserAccessToken(
        new MetisUserAccessToken(EXAMPLE_EMAIL, EXAMPLE_ACCESS_TOKEN, new Date()));
    when(psqlMetisUserDao.getMetisUserByEmail(anyString())).thenReturn(metisUser);
    authenticationService.loginUser(EXAMPLE_EMAIL, EXAMPLE_PASSWORD);
    verify(psqlMetisUserDao).updateAccessTokenTimestamp(anyString());
  }

  @Test
  void loginUserAuthenticateFailure() {
    when(psqlMetisUserDao.getMetisUserByEmail(anyString())).thenReturn(null);
    assertThrows(UserUnauthorizedException.class,
        () -> authenticationService.loginUser(EXAMPLE_EMAIL, EXAMPLE_PASSWORD));
  }

  @Test
  void updateUserPassword() {
    ArgumentCaptor<MetisUserModel> metisUserArgumentCaptor = ArgumentCaptor.forClass(MetisUserModel.class);
    when(psqlMetisUserDao.getMetisUserByEmail(EXAMPLE_EMAIL)).thenReturn(new MetisUserModel());
    authenticationService.updateUserPassword(EXAMPLE_EMAIL, EXAMPLE_PASSWORD);
    verify(psqlMetisUserDao).updateMetisUser(metisUserArgumentCaptor.capture());
    assertNotNull(metisUserArgumentCaptor.getValue().getPassword());
  }

  @Test
  void updateUserMakeAdmin() throws Exception {
    when(psqlMetisUserDao.getMetisUserByEmail(EXAMPLE_EMAIL)).thenReturn(new MetisUserModel());
    authenticationService.updateUserMakeAdmin(EXAMPLE_EMAIL);
    verify(psqlMetisUserDao).updateMetisUserToMakeAdmin(EXAMPLE_EMAIL);
  }

  @Test
  void updateUserMakeAdminUserDoesNotExist() {
    when(psqlMetisUserDao.getMetisUserByEmail(EXAMPLE_EMAIL)).thenReturn(null);
    assertThrows(NoUserFoundException.class,
        () -> authenticationService.updateUserMakeAdmin(EXAMPLE_EMAIL));
  }

  @Test
  void isUserAdmin() throws Exception {
    MetisUserModel metisUser = new MetisUserModel();
    metisUser.setAccountRole(AccountRole.METIS_ADMIN);
    metisUser.setMetisUserAccessToken(
        new MetisUserAccessToken(EXAMPLE_EMAIL, EXAMPLE_ACCESS_TOKEN, new Date()));
    when(psqlMetisUserDao.getMetisUserByAccessToken(EXAMPLE_ACCESS_TOKEN)).thenReturn(metisUser);
    assertTrue(authenticationService.isUserAdmin(EXAMPLE_ACCESS_TOKEN));

    verify(psqlMetisUserDao).updateAccessTokenTimestampByAccessToken(EXAMPLE_ACCESS_TOKEN);
  }

  @Test
  void isUserAdminFalse() throws Exception {
    MetisUserModel metisUser = new MetisUserModel();
    metisUser.setMetisUserAccessToken(
        new MetisUserAccessToken(EXAMPLE_EMAIL, EXAMPLE_ACCESS_TOKEN, new Date()));
    when(psqlMetisUserDao.getMetisUserByAccessToken(EXAMPLE_ACCESS_TOKEN)).thenReturn(metisUser);
    assertFalse(authenticationService.isUserAdmin(EXAMPLE_ACCESS_TOKEN));

    verify(psqlMetisUserDao).updateAccessTokenTimestampByAccessToken(EXAMPLE_ACCESS_TOKEN);
  }

  @Test
  void hasPermissionToRequestUserUpdateOwnUser() throws Exception {
    final String storedMetisUserEmail = "storedEmail@example.com";
    MetisUserModel metisUser = new MetisUserModel();
    metisUser.setEmail(storedMetisUserEmail);
    metisUser.setMetisUserAccessToken(
        new MetisUserAccessToken(EXAMPLE_EMAIL, EXAMPLE_ACCESS_TOKEN, new Date()));
    MetisUserModel storedMetisUser = new MetisUserModel();
    storedMetisUser.setEmail(storedMetisUserEmail);
    when(psqlMetisUserDao.getMetisUserByAccessToken(EXAMPLE_ACCESS_TOKEN)).thenReturn(metisUser);
    when(psqlMetisUserDao.getMetisUserByEmail(storedMetisUserEmail)).thenReturn(storedMetisUser);

    assertTrue(authenticationService
        .hasPermissionToRequestUserUpdate(EXAMPLE_ACCESS_TOKEN, storedMetisUserEmail));
  }

  @Test
  void hasPermissionToRequestUserUpdateRequesterIsAdmin() throws Exception {
    final String storedMetisUserEmail = "storedEmail@example.com";
    final String storedMetisUserEmailToUpdate = "toUpdate@example.com";
    MetisUserModel metisUser = new MetisUserModel();
    metisUser.setAccountRole(AccountRole.METIS_ADMIN);
    metisUser.setEmail(storedMetisUserEmail);
    metisUser.setMetisUserAccessToken(
        new MetisUserAccessToken(EXAMPLE_EMAIL, EXAMPLE_ACCESS_TOKEN, new Date()));
    MetisUserModel storedMetisUser = new MetisUserModel();
    storedMetisUser.setEmail(storedMetisUserEmailToUpdate);
    when(psqlMetisUserDao.getMetisUserByAccessToken(EXAMPLE_ACCESS_TOKEN)).thenReturn(metisUser);
    when(psqlMetisUserDao.getMetisUserByEmail(storedMetisUserEmail)).thenReturn(storedMetisUser);

    assertTrue(authenticationService
        .hasPermissionToRequestUserUpdate(EXAMPLE_ACCESS_TOKEN, storedMetisUserEmail));
  }

  @Test
  void hasPermissionToRequestUserUpdateUserIsEuropeanaDataOfficerAndToUpdateNonAdmin()
      throws Exception {
    final String storedMetisUserEmail = "storedEmail@example.com";
    final String storedMetisUserEmailToUpdate = "toUpdate@example.com";
    MetisUserModel metisUser = new MetisUserModel();
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    metisUser.setEmail(storedMetisUserEmail);
    metisUser.setMetisUserAccessToken(
        new MetisUserAccessToken(EXAMPLE_EMAIL, EXAMPLE_ACCESS_TOKEN, new Date()));
    MetisUserModel storedMetisUser = new MetisUserModel();
    storedMetisUser.setAccountRole(AccountRole.PROVIDER_VIEWER);
    storedMetisUser.setEmail(storedMetisUserEmailToUpdate);
    when(psqlMetisUserDao.getMetisUserByAccessToken(EXAMPLE_ACCESS_TOKEN)).thenReturn(metisUser);
    when(psqlMetisUserDao.getMetisUserByEmail(storedMetisUserEmailToUpdate))
        .thenReturn(storedMetisUser);

    assertFalse(authenticationService
        .hasPermissionToRequestUserUpdate(EXAMPLE_ACCESS_TOKEN, storedMetisUserEmailToUpdate));
  }

  @Test
  void hasPermissionToRequestUserUpdateUserToUpdateDoesNotExist() {
    final String storedMetisUserEmail = "storedEmail@example.com";
    MetisUserModel metisUser = new MetisUserModel();
    when(psqlMetisUserDao.getMetisUserByAccessToken(EXAMPLE_ACCESS_TOKEN)).thenReturn(metisUser);
    when(psqlMetisUserDao.getMetisUserByEmail(storedMetisUserEmail)).thenReturn(null);
    assertThrows(NoUserFoundException.class, () -> authenticationService
        .hasPermissionToRequestUserUpdate(EXAMPLE_ACCESS_TOKEN, storedMetisUserEmail));
  }

  @Test
  void expireAccessTokens() {
    authenticationService.expireAccessTokens();
    verify(psqlMetisUserDao).expireAccessTokens(any(Date.class));
  }

  @Test
  void deleteUser() {
    authenticationService.deleteUser(EXAMPLE_EMAIL);
    verify(psqlMetisUserDao).deleteMetisUser(EXAMPLE_EMAIL);
  }

  @Test
  void authenticateUser() throws Exception {
    when(psqlMetisUserDao.getMetisUserByAccessToken(EXAMPLE_ACCESS_TOKEN))
        .thenReturn(new MetisUserModel());
    authenticationService.authenticateUser(EXAMPLE_ACCESS_TOKEN);
    verify(psqlMetisUserDao).updateAccessTokenTimestampByAccessToken(EXAMPLE_ACCESS_TOKEN);
  }

  @Test
  void authenticateUserWrongCredentials() {
    when(psqlMetisUserDao.getMetisUserByAccessToken(EXAMPLE_ACCESS_TOKEN)).thenReturn(null);
    assertThrows(UserUnauthorizedException.class,
        () -> authenticationService.authenticateUser(EXAMPLE_ACCESS_TOKEN));
    verify(psqlMetisUserDao, times(1)).getMetisUserByAccessToken(anyString());
  }

  @Test
  void hasPermissionToRequestAllUsersIsAdmin() throws Exception {
    MetisUserModel metisUser = new MetisUserModel();
    metisUser.setAccountRole(AccountRole.METIS_ADMIN);
    when(psqlMetisUserDao.getMetisUserByAccessToken(EXAMPLE_ACCESS_TOKEN)).thenReturn(metisUser);
    assertTrue(authenticationService.hasPermissionToRequestAllUsers(EXAMPLE_ACCESS_TOKEN));
  }

  @Test
  void hasPermissionToRequestAllUsersIsEuropeanaDataOfficer() throws Exception {
    MetisUserModel metisUser = new MetisUserModel();
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(psqlMetisUserDao.getMetisUserByAccessToken(EXAMPLE_ACCESS_TOKEN)).thenReturn(metisUser);
    assertTrue(authenticationService.hasPermissionToRequestAllUsers(EXAMPLE_ACCESS_TOKEN));
  }

  @Test
  void hasPermissionToRequestAllUsersNotPermitted() throws Exception {
    MetisUserModel metisUser = new MetisUserModel();
    when(psqlMetisUserDao.getMetisUserByAccessToken(EXAMPLE_ACCESS_TOKEN)).thenReturn(metisUser);
    assertFalse(authenticationService.hasPermissionToRequestAllUsers(EXAMPLE_ACCESS_TOKEN));
  }

  @Test
  void getAllUsersIsAdmin() {
    MetisUserModel metisUser = new MetisUserModel();
    metisUser.setAccountRole(AccountRole.METIS_ADMIN);
    metisUser.setMetisUserAccessToken(
        new MetisUserAccessToken(EXAMPLE_EMAIL, EXAMPLE_ACCESS_TOKEN, new Date()));
    MetisUserModel metisUserAdmin = new MetisUserModel();
    metisUserAdmin.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    metisUserAdmin.setMetisUserAccessToken(
        new MetisUserAccessToken(EXAMPLE_EMAIL, EXAMPLE_ACCESS_TOKEN, new Date()));
    ArrayList<MetisUserModel> metisUsers = new ArrayList<>();
    metisUsers.add(metisUser);
    metisUsers.add(metisUserAdmin);
    when(psqlMetisUserDao.getAllMetisUsers()).thenReturn(metisUsers);
    List<MetisUser> allUsersRetrieved = authenticationService.getAllUsers();
    assertEquals(metisUsers.size(), allUsersRetrieved.size());
    assertEquals(metisUsers.stream().map(MetisUserModel::getEmail).collect(Collectors.toSet()),
            allUsersRetrieved.stream().map(MetisUser::getEmail).collect(Collectors.toSet()));
  }

  private MetisUserModel registerAndCaptureMetisUser() throws Exception {
    final ZCRMRecord zcrmRecordContactWithAccountInTheFields = getZCRMRecordContactWithAccountInTheFields();

    when(psqlMetisUserDao.getMetisUserByEmail(anyString())).thenReturn(null);
    when(zohoAccessClient.getZcrmRecordContactByEmail(anyString()))
        .thenReturn(zcrmRecordContactWithAccountInTheFields);
    when(zohoAccessClient.getZcrmRecordOrganizationByName(anyString()))
        .thenReturn(
            (ZCRMRecord) zcrmRecordContactWithAccountInTheFields.getFieldValue("Account_Name"));
    authenticationService.registerUser(EXAMPLE_EMAIL, EXAMPLE_PASSWORD);
    ArgumentCaptor<MetisUserModel> metisUserArgumentCaptor = ArgumentCaptor.forClass(MetisUserModel.class);
    verify(psqlMetisUserDao).createMetisUser(metisUserArgumentCaptor.capture());
    return metisUserArgumentCaptor.getValue();
  }

  private static ZCRMRecord getZCRMRecordContactWithAccountInTheFieldsNoOrganizationName() {
    final ZCRMRecord zcrmRecordContact = getZCRMRecordContact();
    zcrmRecordContact.setFieldValue("Account_Name", getZCRMRecordAccountNoOrganizationName());
    return zcrmRecordContact;
  }

  private static ZCRMRecord getZCRMRecordContactWithAccountInTheFields() {
    final ZCRMRecord zcrmRecordContact = getZCRMRecordContact();
    zcrmRecordContact.setFieldValue("Account_Name", getZCRMRecordAccount());
    return zcrmRecordContact;
  }

  private static ZCRMRecord getZCRMRecordContact() {
    final ZCRMRecord zcrmRecordContact = new ZCRMRecord("Contacts");
    zcrmRecordContact.setEntityId(1482250000004168044L);
    zcrmRecordContact.setFieldValue("Pick_List_3", "EUROPEANA_DATA_OFFICER");
    zcrmRecordContact.setFieldValue("Metis_user", true);

    return zcrmRecordContact;
  }


  private static ZCRMRecord getZCRMRecordAccount() {
    final ZCRMRecord zcrmRecordAccount = new ZCRMRecord("Accounts");
    zcrmRecordAccount.setEntityId(1482250000004168050L);
    zcrmRecordAccount.setLookupLabel("Europeana Foundation");
    zcrmRecordAccount.setFieldValue("Organisation_Role2", Collections.singletonList("Aggregator"));

    return zcrmRecordAccount;
  }

  private static ZCRMRecord getZCRMRecordAccountNoOrganizationName() {
    final ZCRMRecord zcrmRecordAccount = getZCRMRecordAccount();
    zcrmRecordAccount.setLookupLabel(null);

    return zcrmRecordAccount;
  }
}