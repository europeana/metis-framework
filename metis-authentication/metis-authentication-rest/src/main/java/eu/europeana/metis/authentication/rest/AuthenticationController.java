package eu.europeana.metis.authentication.rest;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.service.AuthenticationService;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.Credentials;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.NoUserFoundException;
import eu.europeana.metis.exception.UserAlreadyExistsException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.util.List;
import javax.ws.rs.QueryParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
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
  @RequestMapping(value = RestEndpoints.AUTHENTICATION_REGISTER, method = RequestMethod.POST)
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
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user cannot be
   * authenticated.</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.AUTHENTICATION_LOGIN, method = RequestMethod.POST, produces = {
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
   * @param oldPassword the old password of the user
   * @param newPassword the new password of the user
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user cannot be
   * authenticated.</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.AUTHENTICATION_UPDATE_PASSD, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateUserPassword(@RequestHeader("Authorization") String authorization,
      @QueryParam("oldPassword") String oldPassword,
      @QueryParam("newPassword") String newPassword)
      throws GenericMetisException {
    if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
      throw new BadContentException("oldPassword or newPassword not provided");
    }
    if (oldPassword.equals(newPassword)) {
      throw new BadContentException("newPassword must be different than oldPassword");
    }
    String accessToken = authenticationService
        .validateAuthorizationHeaderWithAccessToken(
            authorization);//Before any action, validate token
    MetisUser metisUser = authenticationService.authenticateUser(accessToken);
    authenticationService.authenticateUser(metisUser.getEmail(),
        oldPassword);//If no exception authentication with password succeeds
    authenticationService.updateUserPassword(metisUser, newPassword);
    LOGGER.info("User with access_token: {} updated password", accessToken);
  }

  /**
   * Delete a user from the system.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param userEmailToDelete the user email used to delete a user account
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user cannot be
   * authenticated or the user is unauthorized.</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.AUTHENTICATION_DELETE, method = RequestMethod.DELETE, produces = {
      MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@RequestHeader("Authorization") String authorization,
      @QueryParam("userEmailToDelete") String userEmailToDelete)
      throws GenericMetisException {
    String accessToken = authenticationService
        .validateAuthorizationHeaderWithAccessToken(authorization);

    if (!authenticationService.isUserAdmin(accessToken)) {
      throw new UserUnauthorizedException("Action allowed only from admin users");
    }
    authenticationService.deleteUser(userEmailToDelete);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("User with email: {} deleted", userEmailToDelete.replaceAll(
          CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
  }

  /**
   * Update a user by re-retrieving the user from the remote CRM.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param userEmailToUpdate the user email used to update a user account
   * @return updated {@link MetisUser}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoUserFoundException} if a user was not found in the system.</li>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user cannot be
   * authenticated or the user is unauthorized.</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.AUTHENTICATION_UPDATE, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  public MetisUser updateUser(@RequestHeader("Authorization") String authorization,
      @QueryParam("userEmailToUpdate") String userEmailToUpdate) throws GenericMetisException {
    String accessToken = authenticationService
        .validateAuthorizationHeaderWithAccessToken(authorization);
    if (!authenticationService
        .hasPermissionToRequestUserUpdate(accessToken, userEmailToUpdate)) {
      throw new UserUnauthorizedException(ACTION_NOT_ALLOWED_FOR_USER);
    }
    MetisUser metisUser = authenticationService.updateUserFromZoho(userEmailToUpdate);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("User with email: {} updated", userEmailToUpdate.replaceAll(
          CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
    return metisUser;
  }

  /**
   * Change the {@link AccountRole} of a user.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @param userEmailToMakeAdmin the email used to change a user's account to make administrator
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user cannot be
   * authenticated or the user is unauthorized.</li>
   * <li>{@link NoUserFoundException} if a user was not found in the system.</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.AUTHENTICATION_UPDATE_ROLE_ADMIN, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateUserToMakeAdmin(@RequestHeader("Authorization") String authorization,
      @QueryParam("userEmailToMakeAdmin") String userEmailToMakeAdmin)
      throws GenericMetisException {
    String accessToken = authenticationService
        .validateAuthorizationHeaderWithAccessToken(authorization);
    if (!authenticationService.isUserAdmin(accessToken)) {
      throw new UserUnauthorizedException(ACTION_NOT_ALLOWED_FOR_USER);
    }
    authenticationService.updateUserMakeAdmin(userEmailToMakeAdmin);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("User with email: {} made admin", userEmailToMakeAdmin.replaceAll(
          CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
  }

  /**
   * Retrieve a user by using an access token.
   *
   * @param authorization the String provided by an HTTP Authorization header <p> The expected input
   * should follow the rule Bearer accessTokenHere </p>
   * @return the corresponding {@link MetisUser}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the user cannot be
   * authenticated.</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.AUTHENTICATION_USER_BY_TOKEN, method = RequestMethod.GET, produces = {
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
   * <li>{@link BadContentException} if the authorization header is un-parsable or the user cannot be.</li>
   * <li>{@link UserUnauthorizedException} if the user was unauthorized.</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.AUTHENTICATION_USERS, method = RequestMethod.GET, produces = {
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
    return authenticationService.getAllUsers(accessToken);
  }
}
