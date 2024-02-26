package eu.europeana.metis.authentication.service;

import eu.europeana.metis.authentication.dao.PsqlMetisUserDao;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.Credentials;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import eu.europeana.metis.authentication.user.MetisUserView;
import eu.europeana.metis.authentication.utils.MetisUserUtils;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.NoUserFoundException;
import eu.europeana.metis.exception.UserAlreadyExistsException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import eu.europeana.metis.utils.CommonStringValues;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * Service that handles all related operations to authentication including  communication between a psql database and Zoho.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-12-05
 */
@Service
public class AuthenticationService {

  public static final Supplier<BadContentException> COULD_NOT_CONVERT_EXCEPTION_SUPPLIER = () -> new BadContentException(
      "Could not convert internal user");
  private static final int LOG_ROUNDS = 13;
  private static final int CREDENTIAL_FIELDS_NUMBER = 2;
  @SuppressWarnings("java:S6418") // It is not an actual token
  private static final String ACCESS_TOKEN_CHARACTER_BASKET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static final int ACCESS_TOKEN_LENGTH = 32;
  private static final Pattern TOKEN_MATCHING_PATTERN = Pattern.compile("^[" + ACCESS_TOKEN_CHARACTER_BASKET + "]*$");
  private final PsqlMetisUserDao psqlMetisUserDao;


  /**
   * Constructor of class with required parameters
   *
   * @param psqlMetisUserDao the psql object to access postgres database
   */
  @Autowired
  public AuthenticationService(PsqlMetisUserDao psqlMetisUserDao) {
    this.psqlMetisUserDao = psqlMetisUserDao;

  }

  private static MetisUserView convert(MetisUser metisUser) throws BadContentException {
    return Optional.ofNullable(metisUser).map(MetisUserView::new)
                   .orElseThrow(COULD_NOT_CONVERT_EXCEPTION_SUPPLIER);
  }

  private static List<MetisUserView> convert(List<MetisUser> records) {
    return Optional.ofNullable(records).stream().flatMap(Collection::stream).map(MetisUserView::new)
                   .toList();
  }

  /**
   * Registers a user in the system.
   *
   * @param email the unique email of the user
   * @param password the password of the user
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if any other problem occurred while constructing the
   * user.</li>
   * <li>{@link NoUserFoundException} if user was not found in the system.</li>
   * <li>{@link UserAlreadyExistsException} if user with the same email already exists in the
   * system.</li>
   * </ul>
   */
  public void registerUser(String email, String password) throws GenericMetisException {

    MetisUser storedMetisUser = psqlMetisUserDao.getMetisUserByEmail(email);
    if (Objects.nonNull(storedMetisUser)) {
      throw new UserAlreadyExistsException(
          String.format("User with email: %s already exists", email));
    }

    MetisUser metisUser = constructMetisUserFromZoho(email);
    String hashedPassword = generatePasswordHashing(password);
    metisUser.setPassword(hashedPassword);

    psqlMetisUserDao.createMetisUser(metisUser);
  }

  /**
   * Re-fetches a user by email from the remote CRM and updates its information in the system.
   *
   * @param email the email to check for updating
   * @return the updated {@link MetisUserView}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if any other problem occurred while constructing the
   * user.</li>
   * <li>{@link NoUserFoundException} if the user was not found in the system.</li>
   * </ul>
   */
  public MetisUserView updateUserFromZoho(String email) throws GenericMetisException {
    MetisUser storedMetisUser = psqlMetisUserDao.getMetisUserByEmail(email);
    if (Objects.isNull(storedMetisUser)) {
      throw new NoUserFoundException(String.format("User with email: %s does not exist", email));
    }

    MetisUser metisUser = constructMetisUserFromZoho(email);
    //Keep previous information, that are not in Zoho
    metisUser.setPassword(storedMetisUser.getPassword());
    metisUser.setMetisUserAccessToken(storedMetisUser.getMetisUserAccessToken());
    if (storedMetisUser.getAccountRole() == AccountRole.METIS_ADMIN) {
      metisUser.setAccountRole(AccountRole.METIS_ADMIN);
    }

    psqlMetisUserDao.updateMetisUser(metisUser);
    return convert(storedMetisUser);
  }

  /**
   * Validates an HTTP Authorization header.
   *
   * @param authorization the String provided by an HTTP Authorization header
   * <p>
   * The expected input should follow the rule Basic Base64Encoded(email:password)
   * </p>
   * @return a credentials object containing the email and password decoded
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the content of the authorization String is
   * un-parsable</li>
   * </ul>
   */
  public Credentials validateAuthorizationHeaderWithCredentials(String authorization)
      throws GenericMetisException {
    if (StringUtils.isBlank(authorization)) {
      throw new BadContentException("Authorization header was empty");
    }
    Credentials credentials = decodeAuthorizationHeaderWithCredentials(authorization);
    if (credentials == null) {
      throw new BadContentException(
          "Username or password not provided, or not properly defined in the Authorization Header");
    }
    return credentials;
  }

  /**
   * Validates an HTTP Authorization header.
   *
   * @param authorization the String provided by an HTTP Authorization header
   * <p>
   * The expected input should follow the rule Bearer accessTokenHere
   * </p>
   * @return the string representation of the access token
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the content of the authorization String is
   * un-parsable</li>
   * </ul>
   */
  public String validateAuthorizationHeaderWithAccessToken(String authorization)
      throws GenericMetisException {
    if (StringUtils.isBlank(authorization)) {
      throw new UserUnauthorizedException("Authorization header was empty");
    }
    String accessToken = decodeAuthorizationHeaderWithAccessToken(authorization);
    if (StringUtils.isBlank(accessToken)) {
      throw new UserUnauthorizedException("Access token not provided properly");
    }
    //Check that the token is of valid structure
    if (accessToken.length() != ACCESS_TOKEN_LENGTH || !TOKEN_MATCHING_PATTERN.matcher(accessToken)
                                                                              .matches()) {
      throw new UserUnauthorizedException("Access token invalid");
    }
    return accessToken;
  }

  /**
   * Login functionality, which checks if the user with email exists and generates an access token.
   *
   * @param email the unique email used to login
   * @param password the password of corresponding to the email
   * @return {@link MetisUserView}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authentication of the user fails</li>
   * </ul>
   */
  public MetisUserView loginUser(String email, String password) throws GenericMetisException {
    MetisUser storedMetisUser = authenticateUser(email, password);

    if (storedMetisUser.getMetisUserAccessToken() == null) {
      MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken(email,
          generateAccessToken(), new Date());
      psqlMetisUserDao.createUserAccessToken(metisUserAccessToken);
      storedMetisUser.setMetisUserAccessToken(metisUserAccessToken);
    } else {
      psqlMetisUserDao.updateAccessTokenTimestamp(email);
    }
    return convert(storedMetisUser);
  }

  /**
   * Update the {@link MetisUser} password.
   *
   * @param email the unique email used to login
   * @param newPassword the new password
   */
  public void updateUserPassword(String email, String newPassword) {
    MetisUser storedMetisUser = psqlMetisUserDao.getMetisUserByEmail(email);
    String hashedPassword = generatePasswordHashing(newPassword);
    storedMetisUser.setPassword(hashedPassword);
    psqlMetisUserDao.updateMetisUser(storedMetisUser);
  }

  /**
   * Update a user's {@link AccountRole} and make it an administrator.
   *
   * @param userEmailToMakeAdmin the email to update the {@code AccountRole}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoUserFoundException} if the user to update was not found in the system</li>
   * </ul>
   */
  public void updateUserMakeAdmin(String userEmailToMakeAdmin) throws GenericMetisException {
    if (Objects.isNull(psqlMetisUserDao.getMetisUserByEmail(userEmailToMakeAdmin))) {
      throw new NoUserFoundException(
          String.format("User with email %s does not exist", userEmailToMakeAdmin));
    }
    psqlMetisUserDao.updateMetisUserToMakeAdmin(userEmailToMakeAdmin);
  }

  /**
   * Check if a user has an administrator {@link AccountRole}.
   *
   * @param accessToken the access token of the user in the system
   * @return true for administrator, false for non-administrator
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authentication of the user fails</li>
   * </ul>
   */
  public boolean isUserAdmin(String accessToken) throws GenericMetisException {
    MetisUser storedMetisUser = authenticateUserInternal(accessToken);
    return storedMetisUser.getAccountRole() == AccountRole.METIS_ADMIN;
  }

  /**
   * Checks if a user has permission to request a users update.
   *
   * @param accessToken the access token used to verify the user that requests an update
   * @param userEmailToUpdate the email that should be updated
   * @return true for authorized, false for unauthorized
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authentication of the user fails</li>
   * <li>{@link NoUserFoundException} if a user was not found in the system</li>
   * </ul>
   */
  public boolean hasPermissionToRequestUserUpdate(String accessToken, String userEmailToUpdate)
      throws GenericMetisException {
    MetisUser storedMetisUserToUpdate = psqlMetisUserDao
        .getMetisUserByEmail(userEmailToUpdate);
    if (Objects.isNull(storedMetisUserToUpdate)) {
      throw new NoUserFoundException(
          String.format("User with email: %s does not exist", userEmailToUpdate));
    }
    MetisUser storedMetisUser = authenticateUserInternal(accessToken);
    return storedMetisUser.getAccountRole() == AccountRole.METIS_ADMIN || storedMetisUser.getEmail()
                                                                                         .equals(
                                                                                             storedMetisUserToUpdate.getEmail());
  }

  /**
   * Checks and removes access tokens in the system based on the current date.
   */
  public void expireAccessTokens() {
    Date now = new Date();
    psqlMetisUserDao.expireAccessTokens(now);
  }

  /**
   * Delete a user based on an email
   *
   * @param email the email used to remove a user
   */
  public void deleteUser(String email) {
    psqlMetisUserDao.deleteMetisUser(email);
  }

  /**
   * Authenticates a user using an {@code email} and {@code password}.
   *
   * @param email the email identify of the user
   * @param password the previously stored password
   * @return the object of the metis user in the system
   * @throws UserUnauthorizedException if user does not exist or password invalid
   */
  public MetisUser authenticateUser(String email, String password)
      throws UserUnauthorizedException {
    MetisUser storedMetisUser = psqlMetisUserDao.getMetisUserByEmail(email);
    if (Objects.isNull(storedMetisUser) || !isPasswordValid(storedMetisUser, password)) {
      throw new UserUnauthorizedException("Wrong credentials");
    }
    return storedMetisUser;
  }

  /**
   * Authenticates a user using an access token.
   *
   * @param accessToken the access token used to authenticate a user
   * @return {@link MetisUserView}
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authentication of the user fails</li>
   * </ul>
   */
  public MetisUserView authenticateUser(String accessToken) throws GenericMetisException {
    return convert(authenticateUserInternal(accessToken));
  }

  /**
   * Checks if a user, using an access token, has permission to request a list of all the users.
   *
   * @param accessToken the access token used to authenticate the user
   * @return true if authorized, false if unauthorized
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authentication of the user fails</li>
   * </ul>
   */
  public boolean hasPermissionToRequestAllUsers(String accessToken) throws GenericMetisException {
    MetisUser storedMetisUser = authenticateUserInternal(accessToken);
    return storedMetisUser.getAccountRole() == AccountRole.METIS_ADMIN
        || storedMetisUser.getAccountRole() == AccountRole.EUROPEANA_DATA_OFFICER;
  }

  /**
   * Get a metis user using a user identifier.
   *
   * @param accessToken the access token used to authenticate the user asking the request
   * @param userIdToRetrieve the user identifier of the user to be retrieved from the database
   * @return the metis user
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if the authentication of the user fails</li>
   * </ul>
   */
  public MetisUserView getMetisUserByUserIdOnlyWithPublicFields(String accessToken,
      String userIdToRetrieve) throws GenericMetisException {
    authenticateUser(accessToken);
    return convert(psqlMetisUserDao.getMetisUserByUserId(userIdToRetrieve));
  }

  /**
   * Retrieves all {@link MetisUserView}'s from the system.
   *
   * @return the list of all metis users in the system
   */
  public List<MetisUserView> getAllUsers() {
    return convert(psqlMetisUserDao.getAllMetisUsers());
  }

  private MetisUser constructMetisUserFromZoho(String email) {

    //Construct Default User
    MetisUser metisUser = MetisUserUtils.checkFieldsAndPopulateMetisUser(email);
    return metisUser;
  }

  private String generatePasswordHashing(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
  }

  private boolean isPasswordValid(MetisUser metisUser, String passwordToTry) {
    return BCrypt.checkpw(passwordToTry, metisUser.getPassword());
  }

  private Credentials decodeAuthorizationHeaderWithCredentials(String authorization) {
    Credentials credentials = null;
    if (Objects.nonNull(authorization) && authorization.startsWith("Basic")) {
      // Authorization: Basic base64credentials
      String base64Credentials = authorization.substring("Basic".length()).trim();
      String credentialsString = new String(Base64.getDecoder().decode(base64Credentials),
          StandardCharsets.UTF_8);
      // credentials = username:password
      String[] splitCredentials = credentialsString.split(":", CREDENTIAL_FIELDS_NUMBER);
      if (splitCredentials.length == CREDENTIAL_FIELDS_NUMBER) {
        credentials = new Credentials(splitCredentials[0], splitCredentials[1]);
      }
    }
    return credentials;
  }

  private String decodeAuthorizationHeaderWithAccessToken(String authorization) {
    final String accessToken;
    if (Objects.nonNull(authorization) && authorization.startsWith("Bearer")) {
      // Authorization: Bearer accessToken
      accessToken = authorization.substring("Bearer".length()).trim();
    } else {
      accessToken = "";
    }
    return accessToken;
  }

  private MetisUser authenticateUserInternal(String accessToken) throws GenericMetisException {
    MetisUser storedMetisUser = psqlMetisUserDao.getMetisUserByAccessToken(accessToken);
    if (Objects.isNull(storedMetisUser)) {
      throw new UserUnauthorizedException(CommonStringValues.WRONG_ACCESS_TOKEN);
    }
    psqlMetisUserDao.updateAccessTokenTimestampByAccessToken(accessToken);
    return storedMetisUser;
  }

  String generateAccessToken() {
    final SecureRandom rnd = new SecureRandom();
    StringBuilder sb = new StringBuilder(ACCESS_TOKEN_LENGTH);
    for (int i = 0; i < ACCESS_TOKEN_LENGTH; i++) {
      sb.append(ACCESS_TOKEN_CHARACTER_BASKET
          .charAt(rnd.nextInt(ACCESS_TOKEN_CHARACTER_BASKET.length())));
    }
    return sb.toString();
  }
}

