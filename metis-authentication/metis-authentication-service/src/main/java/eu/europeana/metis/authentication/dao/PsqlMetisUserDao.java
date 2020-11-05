package eu.europeana.metis.authentication.dao;

import static eu.europeana.metis.network.SonarqubeNullcheckAvoidanceUtils.performAction;
import static eu.europeana.metis.network.SonarqubeNullcheckAvoidanceUtils.performFunction;

import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import eu.europeana.metis.authentication.user.MetisUserModel;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Dataset Access Object for datasets using Postgresql.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@Repository
public class PsqlMetisUserDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(PsqlMetisUserDao.class);
  private static final long ONE_MINUTE_IN_MILLIS = 60_000;
  private static final int DEFAULT_EXPIRE_TIME_IN_MINS = 10;
  private static final int DEFAULT_PAGE_SIZE_FOR_ACCESS_TOKENS = 100;
  private static final String ACCESS_TOKEN_STRING = "accessToken";
  private static final String EMAIL_STRING = "email";
  private static final String USER_ID_STRING = "user_id";
  private static final String TIMESTAMP_STRING = "timestamp";
  private static final String ACCESS_ROLE_STRING = "accessRole";

  private int accessTokenExpireTimeInMins = DEFAULT_EXPIRE_TIME_IN_MINS;
  private final SessionFactory sessionFactory;

  /**
   * Constructor {@link PsqlMetisUserDao}.
   *
   * @param sessionFactory {@link SessionFactory} required
   */
  @Autowired
  public PsqlMetisUserDao(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /**
   * Stores a {@link MetisUserModel} in the database.
   *
   * @param metisUser the {@link MetisUserModel} to store
   */
  public void createMetisUser(MetisUserModel metisUser) {
    createObjectInDB(metisUser);
  }

  /**
   * Re write a {@link MetisUserModel} in the database.
   *
   * @param metisUser the {@link MetisUserModel} to update
   */
  public void updateMetisUser(MetisUserModel metisUser) {
    try (Session sessionObject = sessionFactory.openSession()) {
      performAction(sessionObject, session -> {
        Transaction tx = session.beginTransaction();
        session.update(metisUser);
        commitTransaction(tx, "Could not update user");
      });
    }
  }

  /**
   * Retrieve a {@link MetisUserModel} from the database using an email.
   *
   * @param email the email to use.
   * @return the {@link MetisUserModel}
   */
  public MetisUserModel getMetisUserByEmail(String email) {
    return getMetisUserByField(EMAIL_STRING, email);
  }

  /**
   * Retrieve a {@link MetisUserModel} from the database using an email.
   *
   * @param userId the userId to use.
   * @return the {@link MetisUserModel}
   */
  public MetisUserModel getMetisUserByUserId(String userId) {
    return getMetisUserByField(USER_ID_STRING, userId);
  }

  private MetisUserModel getMetisUserByField(String fieldName, String fieldValue) {
    try (Session sessionObject = sessionFactory.openSession()) {
      return performFunction(sessionObject, session->{
        MetisUserModel metisUser;
        Query<?> query = session.createQuery(String.format(
                "FROM MetisUserModel WHERE %s = :%s", fieldName, fieldName));
        query.setParameter(fieldName, fieldValue);
        metisUser = null;
        if (!query.list().isEmpty()) {
          metisUser = (MetisUserModel) query.list().get(0);
        }
        return metisUser;
      });
    }
  }

  /**
   * Retrieve the {@link MetisUserModel} from the database using an access token.
   *
   * @param accessToken the access token to retrieve the user
   * @return {@link MetisUserModel}
   */
  public MetisUserModel getMetisUserByAccessToken(String accessToken) {
    try (Session sessionObject = sessionFactory.openSession()) {
      return performFunction(sessionObject, session -> {
        MetisUserModel metisUser;
        Query<?> query = session.createQuery(String.format(
                "FROM MetisUserAccessToken WHERE access_token = :%s", ACCESS_TOKEN_STRING));
        query.setParameter(ACCESS_TOKEN_STRING, accessToken);
        MetisUserAccessToken metisUserAccessToken = null;
        if (!query.list().isEmpty()) {
          metisUserAccessToken = (MetisUserAccessToken) query.list().get(0);
        }
        metisUser = null;
        if (metisUserAccessToken != null) {
          query = session
                  .createQuery(String.format("FROM MetisUserModel WHERE email = :%s", EMAIL_STRING));
          query.setParameter(EMAIL_STRING, metisUserAccessToken.getEmail());

          if (!query.list().isEmpty()) {
            metisUser = (MetisUserModel) query.list().get(0);
          }
        }
        return metisUser;
      });
    }
  }

  /**
   * Store a {@link MetisUserAccessToken} in the database
   *
   * @param metisUserAccessToken {@link MetisUserAccessToken}
   */
  public void createUserAccessToken(MetisUserAccessToken metisUserAccessToken) {
    createObjectInDB(metisUserAccessToken);
  }

  /**
   * Stores an {@link Object} in the database.
   *
   * @param o {@link Object}
   */
  private void createObjectInDB(Object o) {
    try (Session sessionObject = sessionFactory.openSession()) {
      performAction(sessionObject, session->{
        Transaction tx = session.beginTransaction();
        session.persist(o);
        commitTransaction(tx, "Could not create Object in database.");
      });
    }
  }

  /**
   * Goes through all the access tokens in the database and removes the ones that are expired.
   * <p>Requests access tokens from the database by pages and checks the stored timestamps and the
   * timestamp provided. If the expire time has passed it will remove the access token from the
   * database.</p>
   *
   * @param date the {@link Date} to compare the stored timestamp with
   */
  public void expireAccessTokens(Date date) {
    try (Session sessionObject = sessionFactory.openSession()) {
      performAction(sessionObject, session -> {
        Transaction tx = session.beginTransaction();

        int offset = 0;
        int pageSize = DEFAULT_PAGE_SIZE_FOR_ACCESS_TOKENS;
        List<?> metisUserAccessTokens;
        do {
          CriteriaBuilder builder = session.getCriteriaBuilder();
          CriteriaQuery<MetisUserAccessToken> criteriaQuery = builder
                  .createQuery(MetisUserAccessToken.class);
          criteriaQuery.from(MetisUserAccessToken.class);
          Query<MetisUserAccessToken> query = session.createQuery(criteriaQuery);
          query.setFirstResult(offset).setMaxResults(pageSize);
          metisUserAccessTokens = query.getResultList();
          if (!metisUserAccessTokens.isEmpty()) {
            removeTokensBasedOnExpiryDate(date, session, metisUserAccessTokens);
          }
          offset += pageSize;
        } while (!metisUserAccessTokens.isEmpty());
        commitTransaction(tx,
                "Something when wrong when trying to expire metis authentication tokens");
      });
    }
  }

  private void removeTokensBasedOnExpiryDate(Date date, Session session,
      List<?> metisUserAccessTokens) {
    for (Object object : metisUserAccessTokens) {
      MetisUserAccessToken metisUserAccessToken = (MetisUserAccessToken) object;
      long accessTokenInMillis = metisUserAccessToken.getTimestamp().getTime();
      Date afterAddingTenMins = new Date(
          accessTokenInMillis + (getAccessTokenExpireTimeInMins() * ONE_MINUTE_IN_MILLIS));
      if (afterAddingTenMins.compareTo(date) <= 0) {
        //Remove access token
        Query<?> deleteQuery = session.createQuery(String.format(
                "DELETE FROM MetisUserAccessToken WHERE access_token=:%s", ACCESS_TOKEN_STRING));
        deleteQuery.setParameter(ACCESS_TOKEN_STRING, metisUserAccessToken.getAccessToken());
        int i = deleteQuery.executeUpdate();
        LOGGER.info("Removed {} Access Token: {}", i, metisUserAccessToken.getAccessToken());
      }
    }
  }

  public void setAccessTokenExpireTimeInMins(int accessTokenExpireTimeInMins) {
    synchronized (this) {
      this.accessTokenExpireTimeInMins = accessTokenExpireTimeInMins;
    }
  }

  int getAccessTokenExpireTimeInMins() {
    synchronized (this) {
      return accessTokenExpireTimeInMins;
    }
  }

  /**
   * Removes a {@link MetisUserModel} from the database by using the user's email.
   *
   * @param email to find the {@link MetisUserModel}
   */
  public void deleteMetisUser(String email) {
    try (Session sessionObject = sessionFactory.openSession()) {
      performAction(sessionObject, session-> {
        Transaction tx = session.beginTransaction();
        //Remove tokens
        Query<?> deleteQuery = session.createQuery(String.format(
                "DELETE FROM MetisUserAccessToken WHERE email=:%s", EMAIL_STRING));
        deleteQuery.setParameter(EMAIL_STRING, email);
        int i = deleteQuery.executeUpdate();
        LOGGER.info("Removed {} Access Token with email: {}", i, email);

        deleteQuery = session.createQuery(String.format(
                "DELETE FROM MetisUserModel WHERE email=:%s", EMAIL_STRING));
        deleteQuery.setParameter(EMAIL_STRING, email);
        i = deleteQuery.executeUpdate();
        LOGGER.info("Removed {} User with email: {}", i, email);

        commitTransaction(tx, "Could not delete user.");
      });
    }
  }

  /**
   * Updates the timestamp of a stored {@link MetisUserAccessToken} using the email.
   *
   * @param email to find the stored {@link MetisUserAccessToken}
   */
  public void updateAccessTokenTimestamp(String email) {
    try (Session sessionObject = sessionFactory.openSession()) {
      performAction(sessionObject, session -> {
        Transaction tx = session.beginTransaction();
        Query<?> updateQuery = session.createQuery(String.format(
                "UPDATE MetisUserAccessToken SET timestamp=:%s WHERE email=:%s",
                TIMESTAMP_STRING, EMAIL_STRING));
        updateQuery.setParameter(TIMESTAMP_STRING, new Date());
        updateQuery.setParameter(EMAIL_STRING, email);
        int i = updateQuery.executeUpdate();
        LOGGER.info("Updated {} Access Token with email: {}", i, email);
        commitTransaction(tx, "Could not update authentication access token timestamp.");
      });
    }
  }

  /**
   * Updates the timestamp of a stored {@link MetisUserAccessToken} using the access token.
   *
   * @param accessToken to find the stored {@link MetisUserAccessToken}
   */
  public void updateAccessTokenTimestampByAccessToken(String accessToken) {
    try (Session sessionObject = sessionFactory.openSession()) {
      performAction(sessionObject, session -> {
        Transaction tx = session.beginTransaction();
        Query<?> updateQuery = session.createQuery(String.format(
                "UPDATE MetisUserAccessToken SET timestamp=:%s WHERE access_token=:%s",
                TIMESTAMP_STRING, ACCESS_TOKEN_STRING));
        updateQuery.setParameter(TIMESTAMP_STRING, new Date());
        updateQuery.setParameter(ACCESS_TOKEN_STRING, accessToken);
        int i = updateQuery.executeUpdate();
        LOGGER.info("Updated {} Access Token timestamp: {}", i, accessToken);
        commitTransaction(tx, "Could not update authentication access token timestamp.");
      });
    }
  }

  /**
   * Updates a users {@link AccountRole} to administrator
   *
   * @param userEmailToMakeAdmin the email to change it's {@link AccountRole}
   */
  public void updateMetisUserToMakeAdmin(String userEmailToMakeAdmin) {
    try (Session sessionObject = sessionFactory.openSession()) {
      performAction(sessionObject, session -> {
        Transaction tx = session.beginTransaction();
        Query<?> updateQuery = session.createQuery(String.format(
                "UPDATE MetisUserModel SET account_role=:%s WHERE email=:%s",
                ACCESS_ROLE_STRING, EMAIL_STRING));
        updateQuery.setParameter(ACCESS_ROLE_STRING, AccountRole.METIS_ADMIN.name());
        updateQuery.setParameter(EMAIL_STRING, userEmailToMakeAdmin);
        int i = updateQuery.executeUpdate();
        LOGGER.info("Updated {} MetisUserModel with email: {}, made METIS_ADMIN", i,
                userEmailToMakeAdmin);
        commitTransaction(tx, "Could not upgrade role of user.");
      });
    }
  }

  /**
   * It will try to commit the transaction. If the transaction fails, it will rollback to previous
   * state.
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
   * Retrieves a list of all the {@link MetisUserModel}s.
   *
   * @return list of all {@link MetisUserModel}s
   */
  public List<MetisUserModel> getAllMetisUsers() {
    try (Session sessionObject = sessionFactory.openSession()) {
      return performFunction(sessionObject, session -> {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<MetisUserModel> criteriaQuery = builder.createQuery(MetisUserModel.class);
        criteriaQuery.from(MetisUserModel.class);
        Query<MetisUserModel> query = session.createQuery(criteriaQuery);
        return query.getResultList();
      });
    }
  }
}
