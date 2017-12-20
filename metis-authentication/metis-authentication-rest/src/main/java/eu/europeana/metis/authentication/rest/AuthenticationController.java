package eu.europeana.metis.authentication.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.NoUserFoundException;
import eu.europeana.metis.exception.UserAlreadyExistsException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import eu.europeana.metis.authentication.service.AuthenticationService;
import eu.europeana.metis.authentication.user.MetisUser;
import java.util.List;
import javax.ws.rs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
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

  private AuthenticationService authenticationService;

  @Autowired
  public AuthenticationController(
      AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_REGISTER, method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  public void registerUser(@RequestHeader("Authorization") String authorization)
      throws BadContentException, NoUserFoundException, UserAlreadyExistsException {

    String[] credentials = authenticationService.validateAuthorizationHeaderWithCredentials(authorization);
    String email = credentials[0];
    String password = credentials[1];
    authenticationService.registerUser(email, password);
    LOGGER.info("User with email {} has been registered", email);
  }

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_LOGIN, method = RequestMethod.POST, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  public MetisUser loginUser(@RequestHeader("Authorization") String authorization)
      throws BadContentException {
    String[] credentials = authenticationService.validateAuthorizationHeaderWithCredentials(authorization);
    String email = credentials[0];
    String password = credentials[1];
    MetisUser metisUser = authenticationService.loginUser(email, password);
    LOGGER.info("User with email: {} and user id: {} logged in", metisUser.getEmail(),
        metisUser.getUserId());
    return metisUser;
  }

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_UPDATE_PASSD, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateUserPassword(@RequestHeader("Authorization") String authorization,
      @QueryParam("newPassword") String newPassword)
      throws BadContentException {
    String accessToken = authenticationService.validateAuthorizationHeaderWithAccessToken(authorization);
    if (StringUtils.isEmpty(newPassword)) {
      throw new BadContentException("newPassword not provided");
    }
    MetisUser metisUser = authenticationService.authenticateUser(accessToken);
    authenticationService.updateUserPassword(metisUser, newPassword);
    LOGGER.info("User with access_token: {} updated password", accessToken);
  }

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_DELETE, method = RequestMethod.DELETE, produces = {
      MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@RequestHeader("Authorization") String authorization,
      @QueryParam("userEmailToDelete") String userEmailToDelete)
      throws BadContentException, UserUnauthorizedException {
    String accessToken = authenticationService.validateAuthorizationHeaderWithAccessToken(authorization);

    if (!authenticationService.isUserAdmin(accessToken)) {
      throw new UserUnauthorizedException("Action allowed only from admin users");
    }
    authenticationService.deleteUser(userEmailToDelete);
    LOGGER.info("User with email: {} deleted", userEmailToDelete);
  }

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_UPDATE, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  public MetisUser updateUser(@RequestHeader("Authorization") String authorization,
      @QueryParam("userEmailToUpdate") String userEmailToUpdate)
      throws BadContentException, NoUserFoundException, UserUnauthorizedException {
    String accessToken = authenticationService.validateAuthorizationHeaderWithAccessToken(authorization);
    if (!authenticationService
        .hasPermissionToRequestUserUpdate(accessToken, userEmailToUpdate)) {
      throw new UserUnauthorizedException(ACTION_NOT_ALLOWED_FOR_USER);
    }
    MetisUser metisUser = authenticationService.updateUserFromZoho(userEmailToUpdate);
    LOGGER.info("User with email: {} updated", userEmailToUpdate);
    return metisUser;
  }

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_UPDATE_ROLE_ADMIN, method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateUserToMakeAdmin(@RequestHeader("Authorization") String authorization,
      @QueryParam("userEmailToMakeAdmin") String userEmailToMakeAdmin)
      throws BadContentException, UserUnauthorizedException, NoUserFoundException {
    String accessToken = authenticationService.validateAuthorizationHeaderWithAccessToken(authorization);
    if (!authenticationService.isUserAdmin(accessToken)) {
      throw new UserUnauthorizedException(ACTION_NOT_ALLOWED_FOR_USER);
    }
    authenticationService.updateUserMakeAdmin(userEmailToMakeAdmin);
    LOGGER.info("User with email: {} made admin", userEmailToMakeAdmin);
  }

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_USERS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  public List<MetisUser> getAllUsers(@RequestHeader("Authorization") String authorization)
      throws BadContentException, UserUnauthorizedException {
    String accessToken = authenticationService.validateAuthorizationHeaderWithAccessToken(authorization);
    if (!authenticationService
        .hasPermissionToRequestAllUsers(accessToken)) {
      throw new UserUnauthorizedException(ACTION_NOT_ALLOWED_FOR_USER);
    }
    return authenticationService.getAllUsers(accessToken);
  }
}
