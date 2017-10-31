package eu.europeana.metis.authentication.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.exceptions.BadContentException;
import eu.europeana.metis.authentication.exceptions.NoOrganizationFoundException;
import eu.europeana.metis.authentication.exceptions.NoUserFoundException;
import eu.europeana.metis.authentication.exceptions.UserAlreadyExistsException;
import eu.europeana.metis.authentication.service.AuthenticationService;
import eu.europeana.metis.authentication.user.MetisUser;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_REGISTER, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_FORM_URLENCODED_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  public void registerUser(@RequestParam Map<String, String> body)
      throws BadContentException, NoUserFoundException, NoOrganizationFoundException, UserAlreadyExistsException {
    if (body == null) {
      throw new BadContentException("Body was empty");
    }
    String email = body.get("email");
    String password = body.get("password");
    if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password)) {
      throw new BadContentException("Username or password not provided");
    }
    authenticationService.registerUser(email, password);
    LOGGER.info("User with email {} has been registered", email);
  }

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_LOGIN, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_FORM_URLENCODED_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  public MetisUser loginUser(@RequestParam Map<String, String> body) throws BadContentException {
    if (body == null) {
      throw new BadContentException("Body was empty");
    }
    String email = body.get("email");
    String password = body.get("password");
    if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password)) {
      throw new BadContentException("Username or password not provided");
    }
    MetisUser metisUser = authenticationService.loginUser(email, password);
    LOGGER.info("User with email: {} and user id: {} logged in", metisUser.getEmail(),
        metisUser.getUserId());
    return metisUser;
  }

  @RequestMapping(value = RestEndpoints.AUTHENTICATION_DELETE, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_FORM_URLENCODED_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@RequestParam Map<String, String> body) throws BadContentException {
    if (body == null) {
      throw new BadContentException("Body was empty");
    }
    String userEmailToDelete = body.get("userEmailToDelete");
    String email = body.get("email");
    String password = body.get("password");
    if (!authenticationService.isUserAdmin(email, password)) {
      throw new BadContentException("Action allowed only from admin users");
    }
    authenticationService.deleteUser(userEmailToDelete);
    LOGGER.info("User with email: {} deleted", email);
  }
}
