package eu.europeana.metis.authentication.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.exceptions.BadContentException;
import eu.europeana.metis.authentication.exceptions.NoOrganizationFoundException;
import eu.europeana.metis.authentication.exceptions.NoUserFoundException;
import eu.europeana.metis.authentication.exceptions.UserAlreadyExistsException;
import eu.europeana.metis.authentication.service.AuthenticationService;
import eu.europeana.metis.authentication.user.MetisUser;
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
  private AuthenticationService authenticationService;

  @Autowired
  public AuthenticationController(
      AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_REGISTER, method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  public void registerUser(@RequestHeader("Authorization") String authorization)
      throws BadContentException, NoUserFoundException, NoOrganizationFoundException, UserAlreadyExistsException {

    String[] credentials = authenticationService.validateAuthorizationHeader(authorization);
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
    String[] credentials = authenticationService.validateAuthorizationHeader(authorization);
    String email = credentials[0];
    String password = credentials[1];
    MetisUser metisUser = authenticationService.loginUser(email, password);
    LOGGER.info("User with email: {} and user id: {} logged in", metisUser.getEmail(),
        metisUser.getUserId());
    return metisUser;
  }

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_UPDATE_PASSWORD, method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_FORM_URLENCODED_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateUserPassword(@RequestHeader("Authorization") String authorization,
      @QueryParam("newPassword") String newPassword) throws BadContentException {
    String[] credentials = authenticationService.validateAuthorizationHeader(authorization);
    String email = credentials[0];
    String password = credentials[1];
    if (StringUtils.isEmpty(newPassword)) {
      throw new BadContentException("newPassword not provided");
    }
    authenticationService.updateUserPassword(email, password, newPassword);
    LOGGER.info("User with email: {} updated password", email);
  }

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_DELETE, method = RequestMethod.DELETE, consumes = {
      MediaType.APPLICATION_FORM_URLENCODED_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@RequestHeader("Authorization") String authorization,
      @QueryParam("userEmailToDelete") String userEmailToDelete) throws BadContentException {
    String[] credentials = authenticationService.validateAuthorizationHeader(authorization);
    String email = credentials[0];
    String password = credentials[1];

    if (!authenticationService.isUserAdmin(email, password)) {
      throw new BadContentException("Action allowed only from admin users");
    }
    authenticationService.deleteUser(userEmailToDelete);
    LOGGER.info("User with email: {} deleted", email);
  }

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_UPDATE, method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_FORM_URLENCODED_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateUser(@RequestHeader("Authorization") String authorization,
      @QueryParam("userEmailToUpdate") String userEmailToUpdate)
      throws BadContentException, NoUserFoundException, NoOrganizationFoundException {
    String[] credentials = authenticationService.validateAuthorizationHeader(authorization);
    String email = credentials[0];
    String password = credentials[1];
    if (!authenticationService.hasPermissionToRequestUserUpdate(email, password, userEmailToUpdate)) {
      throw new BadContentException("Action not allowed for user");
    }
    authenticationService.updateUserFromZoho(userEmailToUpdate);
    LOGGER.info("User with email: {} updated", email);
  }


}
