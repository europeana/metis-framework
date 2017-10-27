package eu.europeana.metis.authentication.service;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europeana.metis.authentication.dao.PsqlMetisUserDao;
import eu.europeana.metis.authentication.dao.ZohoAccessClientDao;
import eu.europeana.metis.authentication.exceptions.BadContentException;
import eu.europeana.metis.authentication.exceptions.NoUserFoundException;
import eu.europeana.metis.authentication.user.MetisUser;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@Service
public class AuthenticationService {

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
      throws BadContentException, NoUserFoundException {
    JsonNode userByEmailJsonNode;
    try {
      userByEmailJsonNode = zohoAccessClientDao.getUserByEmail(email);
    } catch (IOException e) {
      throw new BadContentException(
          String.format("Cannot retrieve user with email %s, from Zoho", email), e);
    }
    if (userByEmailJsonNode == null)
      throw new NoUserFoundException("User was not found in Zoho");
    MetisUser metisUser = new MetisUser(userByEmailJsonNode);
    byte[] salt = getSalt();
    String hashedPassword = generatePasswordHashing(salt, password);
    metisUser.setSalt(salt);
    metisUser.setPassword(hashedPassword);

    //Store user in database.
    psqlMetisUserDao.createMetisUser(metisUser);
  }

  private String generatePasswordHashing(byte[] salt, String password) throws BadContentException {
    MessageDigest sha256;
    try {
      sha256 = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new BadContentException("Hashing algorithm not found", e);
    }
    sha256.update(salt);
    byte[] passwordHash = sha256.digest(password.getBytes());
    StringBuilder stringHashedPassword = new StringBuilder();
    for (byte aPasswordHash : passwordHash) {
      stringHashedPassword
          .append(Integer.toString((aPasswordHash & 0xff) + 0x100, 16).substring(1));
    }

    return stringHashedPassword.toString();
  }

  private byte[] getSalt()
  {
    final Random r = new SecureRandom();
    byte[] salt = new byte[32];
    r.nextBytes(salt);
    return salt;
  }

  private boolean isPasswordValid(MetisUser metisUser, String passwordToTry)
      throws BadContentException {
    String hashedPasswordToTry = generatePasswordHashing(metisUser.getSalt(), passwordToTry);
    return hashedPasswordToTry.equals(metisUser.getPassword());
  }
}
