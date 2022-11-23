package eu.europeana.metis.authentication.utils;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performAction;
import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performThrowingFunction;

import com.zoho.api.authenticator.OAuthToken;
import com.zoho.api.authenticator.Token;
import com.zoho.api.authenticator.store.TokenStore;
import com.zoho.crm.api.UserSignature;
import eu.europeana.metis.authentication.user.MetisZohoOAuthToken;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * Metis specific psql handler for persisting Zoho related oauth tokes.
 * <p>Use the static method {@link MetisZohoOAuthPSQLHandler#initializeWithRefreshToken(String,
 * String, String, String)} if a refresh token needs to be injected in the database, before any other
 * operation.</p>
 * <p>Uses a single {@link SessionFactory} for all instances.
 * This class is used from zoho libraries internally. Make sure to call {@link
 * MetisZohoOAuthPSQLHandler#close()} when completed, but keep in mind that it is used inside the
 * zoho libraries.</p>
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-03-20
 */
public class MetisZohoOAuthPSQLHandler implements TokenStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetisZohoOAuthPSQLHandler.class);
  private static final String USER_IDENTIFIER_STRING = "userIdentifier";
  private static SessionFactory sessionFactory;
  private static String clientId;
  private static String clientSecret;

  public MetisZohoOAuthPSQLHandler(SessionFactory sessionFactory) {
    MetisZohoOAuthPSQLHandler.sessionFactory = sessionFactory;
  }

  /**
   * Initialize the provided user email id with the refresh token.
   * <p>If any of the parameters are blank, there would be no database insertion.</p>
   *
   * @param userEmailId the user email.
   * @param refreshToken the refresh token.
   * @param clientId the client id
   * @param clientSecret the client secret
   */
  public static void initializeWithRefreshToken(@Nullable String userEmailId,
      @Nullable String refreshToken, String clientId, String clientSecret) {
    MetisZohoOAuthPSQLHandler.clientId = clientId;
    MetisZohoOAuthPSQLHandler.clientSecret = clientSecret;
    if (StringUtils.isNotBlank(userEmailId) && StringUtils.isNotBlank(refreshToken)) {
      final MetisZohoOAuthToken metisZohoOAuthToken = new MetisZohoOAuthToken(userEmailId,
          StringUtils.EMPTY, refreshToken, 0L);
      try (Session dbSession = sessionFactory.openSession()) {
        performAction(dbSession, session -> {
          Transaction tx = session.beginTransaction();
          session.saveOrUpdate(metisZohoOAuthToken);
          String potentialErrorMessage = "Exception while saving zoho oauth user tokens";
          commitTransaction(tx, potentialErrorMessage);
        });
      }
    }
  }


  @Override
  public Token getToken(UserSignature userSignature, Token token) {
    final MetisZohoOAuthToken metisZohoOAuthToken;
    try (Session dbSession = sessionFactory.openSession()) {
      metisZohoOAuthToken = performThrowingFunction(dbSession, session -> {
        Transaction tx = session.beginTransaction();
        Query<?> query = session.createQuery(String
            .format("FROM MetisZohoOAuthToken WHERE %s = :%s", USER_IDENTIFIER_STRING,
                USER_IDENTIFIER_STRING));
        query.setParameter(USER_IDENTIFIER_STRING, userSignature.getEmail());
        if (query.list().isEmpty()) {
          return null;
        }
        final MetisZohoOAuthToken foundToken = (MetisZohoOAuthToken) query.list().get(0);
        String potentialErrorMessage = "Exception while retrieving zoho oauth user tokens";
        commitTransaction(tx, potentialErrorMessage);
        return foundToken;
      });
    }
    return metisZohoOAuthToken == null ? null : metisZohoOAuthToken
        .convertToZohoOAuthToken(MetisZohoOAuthPSQLHandler.clientId,
            MetisZohoOAuthPSQLHandler.clientSecret);
  }

  @Override
  public void saveToken(UserSignature userSignature, Token token) {
    OAuthToken oAuthToken = (OAuthToken) token;
    final MetisZohoOAuthToken metisZohoOAuthToken = new MetisZohoOAuthToken(
        userSignature.getEmail(), oAuthToken.getAccessToken(), oAuthToken.getRefreshToken(),
        Long.valueOf(oAuthToken.getExpiresIn()));
    try (Session dbSession = sessionFactory.openSession()) {
      performAction(dbSession, session -> {
        Transaction tx = session.beginTransaction();
        session.saveOrUpdate(metisZohoOAuthToken);
        String potentialErrorMessage = "Exception while saving zoho oauth user tokens";
        commitTransaction(tx, potentialErrorMessage);
      });
    }
  }

  @Override
  public void deleteToken(Token token) {
    String userIdentifier = ((OAuthToken)token).getUserMail();
    try (Session dbSession = sessionFactory.openSession()) {
      performAction(dbSession, session -> {
        Transaction tx = session.beginTransaction();
        Query<?> deleteQuery = session.createQuery(String
            .format("DELETE FROM MetisZohoOAuthToken WHERE %s = :%s", USER_IDENTIFIER_STRING,
                USER_IDENTIFIER_STRING));
        deleteQuery.setParameter(USER_IDENTIFIER_STRING, userIdentifier);
        int i = deleteQuery.executeUpdate();
        String potentialErrorMessage = "Exception while deleting zoho oauth user tokens";
        commitTransaction(tx, potentialErrorMessage);
        LOGGER
            .info("Removed {} Metis Zoho OAuth tokens with user identifier: {}", i, userIdentifier);
      });
    }
  }

  @Override
  public List<Token> getTokens() {
    //Nothing to do, not used method
    LOGGER.warn("Returning null, this method is not supported");
    return Collections.emptyList();
  }

  @Override
  public void deleteTokens() {
    //Nothing to do, not used method
    LOGGER.warn("Did nothing, this method is not supported");
  }

  /**
   * It will try to commit the transaction. If the transaction fails, it will rollback to previous
   * state.
   *
   * @param tx the transaction to commit
   */
  private static void commitTransaction(Transaction tx, String potentialErrorMessage) {
    try {
      tx.commit();
    } catch (RuntimeException e) {
      tx.rollback();
      LOGGER.error("Transaction commit failed with message '{}', rolling back..",
          potentialErrorMessage);
      throw new TransactionException(
          String.format("Transaction commit failed with message '%s'", potentialErrorMessage), e);
    }
  }

  /**
   * Call this method before application shutdown, to gracefully close the internal connections
   */
  public static void close() {
    if (sessionFactory != null && !sessionFactory.isClosed()) {
      sessionFactory.close();
    }
  }
}
