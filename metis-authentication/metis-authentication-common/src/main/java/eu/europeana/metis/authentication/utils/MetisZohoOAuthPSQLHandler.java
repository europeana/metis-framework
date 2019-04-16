package eu.europeana.metis.authentication.utils;

import com.zoho.oauth.client.ZohoPersistenceHandler;
import com.zoho.oauth.common.ZohoOAuthException;
import com.zoho.oauth.contract.ZohoOAuthTokens;
import eu.europeana.metis.authentication.user.MetisZohoOAuthToken;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metis specific psql handler for persisting Zoho related oauth tokes.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-03-20
 */
public class MetisZohoOAuthPSQLHandler implements ZohoPersistenceHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetisZohoOAuthPSQLHandler.class);
  private static final String USER_IDENTIFIER_STRING = "userIdentifier";
  private static final SessionFactory sessionFactory;

  static {
    org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
    configuration.addAnnotatedClass(MetisZohoOAuthToken.class);
    configuration.configure();
    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
        configuration.getProperties()).build();
    sessionFactory = configuration.buildSessionFactory(serviceRegistry);
  }

  public MetisZohoOAuthPSQLHandler() {
    //Empty constructor is required from internal Zoho classes
  }

  @Override
  public void saveOAuthData(ZohoOAuthTokens zohoOAuthTokens) throws Exception {

    final MetisZohoOAuthToken metisZohoOAuthToken = new MetisZohoOAuthToken(
        zohoOAuthTokens.getUserMailId(), zohoOAuthTokens.getAccessToken(),
        zohoOAuthTokens.getRefreshToken(), zohoOAuthTokens.getExpiryTime());
    try (Session session = sessionFactory.openSession()) {
      Transaction tx = session.beginTransaction();

      session.saveOrUpdate(metisZohoOAuthToken);
      String potentialErrorMessage = "Exception while saving zoho oauth user tokens";
      commitTransaction(tx, potentialErrorMessage);
    }
  }

  @Override
  public ZohoOAuthTokens getOAuthTokens(String userIdentifier) throws Exception {
    MetisZohoOAuthToken metisZohoOAuthToken;
    Transaction tx;
    try (Session session = sessionFactory.openSession()) {
      tx = session.beginTransaction();

      Query query = session
          .createQuery(String
              .format("FROM MetisZohoOAuthToken WHERE %s = :%s", USER_IDENTIFIER_STRING,
                  USER_IDENTIFIER_STRING));
      query.setParameter(USER_IDENTIFIER_STRING, userIdentifier);
      if (!query.list().isEmpty()) {
        metisZohoOAuthToken = (MetisZohoOAuthToken) query.list().get(0);
      } else {
        throw new ZohoOAuthException("Given User not found in persistence.");
      }
      String potentialErrorMessage = "Exception while retrieving zoho oauth user tokens";
      commitTransaction(tx, potentialErrorMessage);
    }

    return metisZohoOAuthToken == null ? null : metisZohoOAuthToken.convertToZohoOAuthTokens();
  }

  @Override
  public void deleteOAuthTokens(String userIdentifier) {
    try (Session session = sessionFactory.openSession()) {
      Transaction tx = session.beginTransaction();
      Query deleteQuery = session.createQuery(
          String.format("DELETE FROM MetisZohoOAuthToken WHERE %s = :%s", USER_IDENTIFIER_STRING,
              USER_IDENTIFIER_STRING));
      deleteQuery.setParameter(USER_IDENTIFIER_STRING, userIdentifier);
      int i = deleteQuery.executeUpdate();
      String potentialErrorMessage = "Exception while deleting zoho oauth user tokens";
      commitTransaction(tx, potentialErrorMessage);
      LOGGER.info("Removed {} Metis Zoho OAuth tokens with user identifier: {}", i, userIdentifier);
    }
  }

  /**
   * It will try to commit the transaction. If the transaction fails, it will
   * rollback to previous state.
   *
   * @param tx the transaction to commit
   */
  private void commitTransaction(Transaction tx, String potentialErrorMessage) {
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
