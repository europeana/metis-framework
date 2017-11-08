package eu.europeana.metis.authentication.service;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europeana.metis.authentication.dao.PsqlMetisUserDao;
import eu.europeana.metis.authentication.dao.ZohoAccessClientDao;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.NoUserFoundException;
import eu.europeana.metis.exception.UserAlreadyExistsException;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@Service
public class AuthenticationService {

  private static final int LOG_ROUNDS = 13;
  private ZohoAccessClientDao zohoAccessClientDao;
  private PsqlMetisUserDao psqlMetisUserDao;

  @Autowired
  public AuthenticationService(
      ZohoAccessClientDao zohoAccessClientDao,
      PsqlMetisUserDao psqlMetisUserDao) {
    this.zohoAccessClientDao = zohoAccessClientDao;
    this.psqlMetisUserDao = psqlMetisUserDao;
  }

  public void registerUser(String email, String password)
      throws BadContentException, NoUserFoundException, UserAlreadyExistsException {

    MetisUser storedMetisUser = psqlMetisUserDao.getMetisUserByEmail(email);
    if (storedMetisUser != null) {
      throw new UserAlreadyExistsException(
          String.format("User with email: %s already exists", email));
    }

    MetisUser metisUser = constructMetisUserFromZoho(email);
    String hashedPassword = generatePasswordHashing(password);
    metisUser.setPassword(hashedPassword);

    psqlMetisUserDao.createMetisUser(metisUser);
  }

  public void updateUserFromZoho(String email)
      throws BadContentException, NoUserFoundException {
    MetisUser storedMetisUser = psqlMetisUserDao.getMetisUserByEmail(email);
    if (storedMetisUser == null) {
      throw new NoUserFoundException(
          String.format("User with email: %s does not exist", email));
    }

    MetisUser metisUser = constructMetisUserFromZoho(email);
    metisUser.setPassword(storedMetisUser.getPassword());
    if (storedMetisUser.getAccountRole() == AccountRole.METIS_ADMIN) {
      metisUser.setAccountRole(AccountRole.METIS_ADMIN);
    }

    psqlMetisUserDao.updateMetisUser(metisUser);
  }

  private MetisUser constructMetisUserFromZoho(String email)
      throws BadContentException, NoUserFoundException {
    //Get user from zoho
    JsonNode userByEmailJsonNode;
    try {
      userByEmailJsonNode = zohoAccessClientDao.getUserByEmail(email);
    } catch (IOException e) {
      throw new BadContentException(
          String.format("Cannot retrieve user with email %s, from Zoho", email), e);
    }
    if (userByEmailJsonNode == null) {
      throw new NoUserFoundException("User was not found in Zoho");
    }

    //Construct User
    MetisUser metisUser;
    try {
      metisUser = new MetisUser(userByEmailJsonNode);
    } catch (ParseException e) {
      throw new BadContentException("Bad content while constructing metisUser");
    }
    if (StringUtils.isEmpty(metisUser.getOrganizationName()) || !metisUser.isMetisUserFlag()
        || metisUser.getAccountRole() == null) {
      throw new BadContentException(
          "Bad content while constructing metisUser, user does not have all the required fields defined properly in Zoho(Organization Name, Metis user, Account Role)");
    }

    //Get Organization Id related to user
    JsonNode organizationJsonNode;
    try {
      organizationJsonNode = zohoAccessClientDao
          .getOrganizationByOrganizationName(metisUser.getOrganizationName());
    } catch (IOException e) {
      throw new BadContentException(
          String.format("Cannot retrieve organization with orgnaization name %s, from Zoho",
              metisUser.getOrganizationName()), e);
    }
    metisUser.setOrganizationIdFromJsonNode(organizationJsonNode);
    return metisUser;
  }

  private String generatePasswordHashing(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
  }

  private boolean isPasswordValid(MetisUser metisUser, String passwordToTry) {
    return BCrypt.checkpw(passwordToTry, metisUser.getPassword());
  }

  public String[] validateAuthorizationHeaderWithCredentials(String authorization) throws BadContentException {
    if (StringUtils.isEmpty(authorization)) {
      throw new BadContentException("Authorization header was empty");
    }
    String[] credentials = decodeAuthorizationHeaderWithCredentials(authorization);
    if (credentials.length < 2) {
      throw new BadContentException("Username or password not provided, or not properly defined in the Authorization Header");
    }
    return credentials;
  }

  public String validateAuthorizationHeaderWithAccessToken(String authorization)
      throws BadContentException {
    if (StringUtils.isEmpty(authorization)) {
      throw new BadContentException("Authorization header was empty");
    }
    String accessToken = decodeAuthorizationHeaderWithAccessToken(authorization);
    if (StringUtils.isEmpty(accessToken)) {
      throw new BadContentException("Access token not provided properly");
    }
    return accessToken;
  }

  private String[] decodeAuthorizationHeaderWithCredentials(String authorization) {
    if (authorization != null && authorization.startsWith("Basic")) {
      // Authorization: Basic base64credentials
      String base64Credentials = authorization.substring("Basic" .length()).trim();
      String credentials = new String(Base64.getDecoder().decode(base64Credentials),
          Charset.forName("UTF-8"));
      // credentials = username:password
      return credentials.split(":", 2);
    }
    return new String[0];
  }

  private String decodeAuthorizationHeaderWithAccessToken(String authorization) {
    if (authorization != null && authorization.startsWith("Bearer")) {
      // Authorization: Bearer accessToken
      return authorization.substring("Bearer".length()).trim();
    }
    return "";
  }

  public MetisUser loginUser(String email, String password)
      throws BadContentException {
    MetisUser storedMetisUser = authenticateUser(email, password);

    if (storedMetisUser.getMetisUserAccessToken() != null) {
      psqlMetisUserDao.updateAccessTokenTimestamp(email);
      return storedMetisUser;
    }
    MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken(email,
        generateAccessToken(), new Date());
    psqlMetisUserDao.createUserAccessToken(metisUserAccessToken);
    storedMetisUser.setMetisUserAccessToken(metisUserAccessToken);
    return storedMetisUser;
  }

  public void updateUserPassword(MetisUser metisUser, String newPassword) {
    String hashedPassword = generatePasswordHashing(newPassword);
    metisUser.setPassword(hashedPassword);
    psqlMetisUserDao.updateMetisUser(metisUser);
  }

  public void updateUserMakeAdmin(String userEmailToMakeAdmin)
      throws NoUserFoundException {
    if (psqlMetisUserDao.getMetisUserByEmail(userEmailToMakeAdmin) == null) {
      throw new NoUserFoundException(
          String.format("User with email %s does not exist", userEmailToMakeAdmin));
    }
    psqlMetisUserDao.updateMetisUserToMakeAdmin(userEmailToMakeAdmin);
  }

  public boolean isUserAdmin(String accessToken)
      throws BadContentException {
    MetisUser storedMetisUser = authenticateUser(accessToken);
    return storedMetisUser.getAccountRole() == AccountRole.METIS_ADMIN;
  }

  public boolean hasPermissionToRequestUserUpdate(String accessToken,
      String userEmailToUpdate)
      throws BadContentException {
    MetisUser storedMetisUser = authenticateUser(accessToken);
    MetisUser storedMetisUserToUpdate = psqlMetisUserDao.getMetisUserByEmail(userEmailToUpdate);
    return storedMetisUser.getAccountRole() == AccountRole.METIS_ADMIN || (
        storedMetisUser.getAccountRole() == AccountRole.EUROPEANA_DATA_OFFICER
            && storedMetisUserToUpdate.getAccountRole() != AccountRole.METIS_ADMIN);
  }

  private String generateAccessToken() {
    final String characterBasket = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    final SecureRandom rnd = new SecureRandom();
    final int accessTokenLength = 32;
    StringBuilder sb = new StringBuilder(accessTokenLength);
    for (int i = 0; i < accessTokenLength; i++) {
      sb.append(characterBasket.charAt(rnd.nextInt(characterBasket.length())));
    }
    return sb.toString();
  }

  public void expireAccessTokens() {
    Date now = new Date();
    psqlMetisUserDao.expireAccessTokens(now);
  }

  public void deleteUser(String email) {
    psqlMetisUserDao.deleteMetisUser(email);
  }

  private MetisUser authenticateUser(String email, String password)
      throws BadContentException {
    MetisUser storedMetisUser = psqlMetisUserDao.getMetisUserByEmail(email);
    if (storedMetisUser == null || !isPasswordValid(storedMetisUser, password)) {
      throw new BadContentException("Wrong credentials");
    }
    return storedMetisUser;
  }

  public MetisUser authenticateUser(String accessToken)
      throws BadContentException {
    MetisUser storedMetisUser = psqlMetisUserDao.getMetisUserByAccessToken(accessToken);
    if (storedMetisUser == null) {
      throw new BadContentException("Wrong access token");
    }
    psqlMetisUserDao.updateAccessTokenTimestampByAccessToken(accessToken);
    return storedMetisUser;
  }

  public boolean hasPermissionToRequestAllUsers(String accessToken)
      throws BadContentException {
    MetisUser storedMetisUser = authenticateUser(accessToken);
    return storedMetisUser.getAccountRole() == AccountRole.METIS_ADMIN
        || storedMetisUser.getAccountRole() == AccountRole.EUROPEANA_DATA_OFFICER;
  }

  public List<MetisUser> getAllUsers(String accessToken) {
    List<MetisUser> allMetisUsers = psqlMetisUserDao.getAllMetisUsers();
    MetisUser metisUserByEmail = psqlMetisUserDao.getMetisUserByAccessToken(accessToken);
    //Remove access tokens from a request coming from a role that is not METIS_ADMIN
    if (metisUserByEmail.getAccountRole() != AccountRole.METIS_ADMIN) {
      for (MetisUser metisUser :
          allMetisUsers) {
        metisUser.setMetisUserAccessToken(null);
      }
    }
    return allMetisUsers;
  }
}
