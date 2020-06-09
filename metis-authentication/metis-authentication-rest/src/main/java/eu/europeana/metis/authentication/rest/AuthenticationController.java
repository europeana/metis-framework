package eu.europeana.metis.authentication.rest;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.service.AuthenticationService;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.Credentials;
import eu.europeana.metis.authentication.user.EmailParameter;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.OldNewPasswordParameters;
import eu.europeana.metis.authentication.user.UserIdParameter;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.NoUserFoundException;
import eu.europeana.metis.exception.UserAlreadyExistsException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Contains all the calls that are related to user authentication.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@Controller
public class AuthenticationController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);
  private static final String ACTION_NOT_ALLOWED_FOR_USER = "Action not allowed";

  private final AuthenticationService authenticationService;

  /**
   * Contains all the REST API methods for the metis-authentication.
   *
   * @param authenticationService the service required for {@link AuthenticationController}
   */
  @Autowired
  public AuthenticationController(
      AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  /**
   * Register a user using an authorization header.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Basic Base64Encoded(email:password) </p>
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if the authorization header is un-parsable or there is problem
   * while constructing the user.</li>
   * <li>{@link NoUserFoundException} if the user was not found in the remote CRM.</li>
   * <li>{@link UserAlreadyExistsException} if the user already exists in the system.</li>
   * </ul>
   */
  @PostMapping(value = RestEndpoints.AUTHENTICATION_REGISTER)
  @ResponseStatus(HttpStatus.CREATED)
  public void registerUser(@RequestHeader("Authorization") String authorization)
      throws GenericMetisException {

    Credentials credentials = authenticationService
        .validateAuthorizationHeaderWithCredentials(authorization);
    authenticationService.registerUser(credentials.getEmail(), credentials.getPassword());
    LOGGER.info("User with email {} has been registered", credentials.getEmail());
  }

  /**
   * Login functionality, which checks if the user with email exists and generates an access token.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Basic Base64Encoded(email:password) </p>
   * @return {@link MetisUser}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if the authorization header is un-parsable or there is problem
   * while constructing the user.</li>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user
   * cannot be authenticated.</li>
   * </ul>
   */
  @PostMapping(value = RestEndpoints.AUTHENTICATION_LOGIN, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  public MetisUser loginUser(@RequestHeader("Authorization") String authorization)
      throws GenericMetisException {
    Credentials credentials = authenticationService
        .validateAuthorizationHeaderWithCredentials(authorization);
    MetisUser metisUser = authenticationService
        .loginUser(credentials.getEmail(), credentials.getPassword());
    LOGGER.info("User with email: {} and user id: {} logged in", metisUser.getEmail(),
        metisUser.getUserId());
    return metisUser;
  }

  /**
   * Update a users password by authentication with an access token.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param oldAndNewPasswordParameters contains the old and new password
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user
   * cannot be authenticated.</li>
   * </ul>
   */
  @PutMapping(value = RestEndpoints.AUTHENTICATION_UPDATE_PASSD, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateUserPassword(@RequestHeader("Authorization") String authorization,
      @RequestBody OldNewPasswordParameters oldAndNewPasswordParameters)
      throws GenericMetisException {
    if (oldAndNewPasswordParameters == null || StringUtils
        .isBlank(oldAndNewPasswordParameters.getOldPassword()) || StringUtils
        .isBlank(oldAndNewPasswordParameters.getNewPassword())) {
      throw new BadContentException("oldPassword or newPassword not provided");
    }
    if (oldAndNewPasswordParameters.getOldPassword()
        .equals(oldAndNewPasswordParameters.getNewPassword())) {
      throw new BadContentException("newPassword must be different than oldPassword");
    }
    String accessToken = authenticationService
        .validateAuthorizationHeaderWithAccessToken(
            authorization);//Before any action, validate token
    MetisUser metisUser = authenticationService.authenticateUser(accessToken);
    authenticationService.authenticateUser(metisUser.getEmail(),
        oldAndNewPasswordParameters
            .getOldPassword());//If no exception authentication with password succeeds
    authenticationService
        .updateUserPassword(metisUser.getEmail(), oldAndNewPasswordParameters.getNewPassword());
    LOGGER.info("User with access_token: {} updated password", accessToken);
  }

  /**
   * Delete a user from the system.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param emailParameter the class that contains the email parameter to act upon
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user
   * cannot be authenticated or the user is unauthorized.</li>
   * </ul>
   */
  @DeleteMapping(value = RestEndpoints.AUTHENTICATION_DELETE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@RequestHeader("Authorization") String authorization,
      @RequestBody EmailParameter emailParameter)
      throws GenericMetisException {
    if (emailParameter == null || StringUtils.isBlank(emailParameter.getEmail())) {
      throw new BadContentException("userEmailToDelete is empty");
    }
    String accessToken = authenticationService
        .validateAuthorizationHeaderWithAccessToken(authorization);

    if (!authenticationService.isUserAdmin(accessToken)) {
      throw new UserUnauthorizedException("Action allowed only from admin users");
    }
    authenticationService.deleteUser(emailParameter.getEmail());
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("User with email: {} deleted", emailParameter.getEmail().replaceAll(
          CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
  }

  /**
   * Update a user by re-retrieving the user from the remote CRM.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param emailParameter the class that contains the email parameter to act upon
   * @return updated {@link MetisUser}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoUserFoundException} if a user was not found in the system.</li>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user
   * cannot be authenticated or the user is unauthorized.</li>
   * </ul>
   */
  @PutMapping(value = RestEndpoints.AUTHENTICATION_UPDATE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  public MetisUser updateUser(@RequestHeader("Authorization") String authorization,
      @RequestBody EmailParameter emailParameter)
      throws GenericMetisException {
    if (emailParameter == null || StringUtils.isBlank(emailParameter.getEmail())) {
      throw new BadContentException("email parameter is empty");
    }
    String accessToken = authenticationService
        .validateAuthorizationHeaderWithAccessToken(authorization);
    if (!authenticationService
        .hasPermissionToRequestUserUpdate(accessToken, emailParameter.getEmail())) {
      throw new UserUnauthorizedException(ACTION_NOT_ALLOWED_FOR_USER);
    }
    MetisUser metisUser = authenticationService.updateUserFromZoho(emailParameter.getEmail());
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("User with email: {} updated", emailParameter.getEmail().replaceAll(
          CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
    return metisUser;
  }

  /**
   * Change the {@link AccountRole} of a user.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param emailParameter the class that contains the email parameter to act upon
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user
   * cannot be authenticated or the user is unauthorized.</li>
   * <li>{@link NoUserFoundException} if a user was not found in the system.</li>
   * </ul>
   */
  @PutMapping(value = RestEndpoints.AUTHENTICATION_UPDATE_ROLE_ADMIN, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateUserToMakeAdmin(@RequestHeader("Authorization") String authorization,
      @RequestBody EmailParameter emailParameter)
      throws GenericMetisException {
    if (emailParameter == null || StringUtils.isBlank(emailParameter.getEmail())) {
      throw new BadContentException("userEmailToMakeAdmin is empty");
    }
    String accessToken = authenticationService
        .validateAuthorizationHeaderWithAccessToken(authorization);
    if (!authenticationService.isUserAdmin(accessToken)) {
      throw new UserUnauthorizedException(ACTION_NOT_ALLOWED_FOR_USER);
    }
    authenticationService.updateUserMakeAdmin(emailParameter.getEmail());
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("User with email: {} made admin", emailParameter.getEmail().replaceAll(
          CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
  }

  /**
   * Get a user using a user identifier.
   * <p>POST method is used to pass the user identifier through the body</p>
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param userIdParameter the class that contains the userId parameter to act upon
   * @return the metis user
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user
   * cannot be authenticated.</li>
   * </ul>
   */
  @PostMapping(value = RestEndpoints.AUTHENTICATION_USER_BY_USER_ID, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  public MetisUser getUserByUserId(@RequestHeader("Authorization") String authorization,
      @RequestBody UserIdParameter userIdParameter) throws GenericMetisException {
    String accessToken = authenticationService
        .validateAuthorizationHeaderWithAccessToken(authorization);
    MetisUser metisUser = authenticationService
        .getMetisUserByUserIdOnlyWithPublicFields(accessToken, userIdParameter.getUserId());
    LOGGER.info("User with email: {} and user id: {} found", metisUser.getEmail(),
        metisUser.getUserId());
    return metisUser;
  }

  /**
   * Retrieve a user by using an access token.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @return the corresponding {@link MetisUser}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user
   * cannot be authenticated.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.AUTHENTICATION_USER_BY_TOKEN, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  public MetisUser getUserByAccessToken(@RequestHeader("Authorization") String authorization)
      throws GenericMetisException {
    String accessToken = authenticationService
        .validateAuthorizationHeaderWithAccessToken(authorization);
    MetisUser metisUser = authenticationService.authenticateUser(accessToken);
    LOGGER.info("User with email: {} and user id: {} authenticated", metisUser.getEmail(),
        metisUser.getUserId());
    return metisUser;
  }

  /**
   * Retrieve a list of all the users in the system.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @return the list with all the {@link MetisUser}s
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if the authorization header is un-parsable or the user cannot
   * be.</li>
   * <li>{@link UserUnauthorizedException} if the user was unauthorized.</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.AUTHENTICATION_USERS, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  public List<MetisUser> getAllUsers(@RequestHeader("Authorization") String authorization)
      throws GenericMetisException {
    String accessToken = authenticationService
        .validateAuthorizationHeaderWithAccessToken(authorization);
    if (!authenticationService
        .hasPermissionToRequestAllUsers(accessToken)) {
      throw new UserUnauthorizedException(ACTION_NOT_ALLOWED_FOR_USER);
    }
    return authenticationService.getAllUsers();
  }
}
