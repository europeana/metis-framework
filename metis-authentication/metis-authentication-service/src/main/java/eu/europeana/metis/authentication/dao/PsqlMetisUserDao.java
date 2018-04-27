package eu.europeana.metis.authentication.dao;

import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
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
   * Stores a {@link MetisUser} in the database.
   *
   * @param metisUser the {@link MetisUser} to store
   */
  public void createMetisUser(MetisUser metisUser) {
    createObjectInDB(metisUser);
  }

  /**
   * Re write a {@link MetisUser} in the database.
   *
   * @param metisUser the {@link MetisUser} to update
   */
  public void updateMetisUser(MetisUser metisUser) {
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();

    try {
      session.update(metisUser);
      tx.commit();
    } catch (RuntimeException e) {
      tx.rollback();
      LOGGER.error("Could not persist object, rolling back..");
      throw new TransactionException("Could not persist object in database", e);
    } finally {
      session.flush();
      session.close();
    }
  }

  /**
   * Retrieve a {@link MetisUser} from the database using an email.
   *
   * @param email the email to use.
   * @return the {@link MetisUser}
   */
  public MetisUser getMetisUserByEmail(String email) {
    Session session = sessionFactory.openSession();

    Query query = session
        .createQuery(String.format("FROM MetisUser WHERE email = :%s", EMAIL_STRING));
    query.setString(EMAIL_STRING, email);
    MetisUser metisUser = null;
    if (!query.list().isEmpty()) {
      metisUser = (MetisUser) query.list().get(0);
    }
    session.flush();
    session.close();
    return metisUser;
  }

  /**
   * Retrieve the {@link MetisUser} from the database using an access token.
   *
   * @param accessToken the access token to retrieve the user
   * @return {@link MetisUser}
   */
  public MetisUser getMetisUserByAccessToken(String accessToken) {
    Session session = sessionFactory.openSession();

    Query query = session
        .createQuery(String
            .format("FROM MetisUserAccessToken WHERE access_token = :%s", ACCESS_TOKEN_STRING));
    query.setString(ACCESS_TOKEN_STRING, accessToken);
    MetisUserAccessToken metisUserAccessToken = null;
    if (!query.list().isEmpty()) {
      metisUserAccessToken = (MetisUserAccessToken) query.list().get(0);
    }
    MetisUser metisUser = null;
    if (metisUserAccessToken != null) {
      query = session.createQuery(String.format("FROM MetisUser WHERE email = :%s", EMAIL_STRING));
      query.setString(EMAIL_STRING, metisUserAccessToken.getEmail());

      if (!query.list().isEmpty()) {
        metisUser = (MetisUser) query.list().get(0);
      }
    }
    session.flush();
    session.close();
    return metisUser;
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
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();

    try {
      session.persist(o);
      tx.commit();
    } catch (RuntimeException e) {
      tx.rollback();
      LOGGER.error("Could not persist object, rolling back..");
      throw new TransactionException("Could not persist object in database", e);
    } finally {
      session.flush();
      session.close();
    }
  }

  /**
   * Goes through all the access tokens in the database and removes the ones that are expired.
   * <p>Requests access tokens from the database by pages and checks the stored timestamps and the timestamp provided.
   * If the expire time has passed it will remove the access token from the database.</p>
   *
   * @param date the {@link Date} to compare the stored timestamp with
   */
  public void expireAccessTokens(Date date) {
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();

    int offset = 0;
    int pageSize = DEFAULT_PAGE_SIZE_FOR_ACCESS_TOKENS;
    List<?> metisUserAccessTokens;
    do {
      Criteria criteria = session.createCriteria(MetisUserAccessToken.class).setFirstResult(offset)
          .setMaxResults(pageSize);
      metisUserAccessTokens = criteria.list();
      if (!metisUserAccessTokens.isEmpty()) {
        for (Object object : metisUserAccessTokens) {
          MetisUserAccessToken metisUserAccessToken = (MetisUserAccessToken) object;
          long accessTokenInMillis = metisUserAccessToken.getTimestamp().getTime();
          Date afterAddingTenMins = new Date(
              accessTokenInMillis + (getAccessTokenExpireTimeInMins() * ONE_MINUTE_IN_MILLIS));
          if (afterAddingTenMins.compareTo(date) <= 0) {
            //Remove access token
            Query deleteQuery = session
                .createQuery(String
                    .format("DELETE FROM MetisUserAccessToken WHERE access_token=:%s",
                        ACCESS_TOKEN_STRING));
            deleteQuery.setString(ACCESS_TOKEN_STRING, metisUserAccessToken.getAccessToken());
            int i = deleteQuery.executeUpdate();
            LOGGER.info("Removed {} Access Token: {}", i, metisUserAccessToken.getAccessToken());
          }
        }
      }
      offset += pageSize;
    } while (!metisUserAccessTokens.isEmpty());
    tx.commit();
    session.flush();
    session.close();
  }

  public void setAccessTokenExpireTimeInMins(int accessTokenExpireTimeInMins) {
    synchronized (this) {
      this.accessTokenExpireTimeInMins = accessTokenExpireTimeInMins;
    }
  }

  public int getAccessTokenExpireTimeInMins() {
    synchronized (this) {
      return accessTokenExpireTimeInMins;
    }
  }

  /**
   * Removes a {@link MetisUser} from the database by using the user's email.
   *
   * @param email to find the {@link MetisUser}
   */
  public void deleteMetisUser(String email) {
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    //Remove tokens
    Query deleteQuery = session.createQuery(
        String.format("DELETE FROM MetisUserAccessToken WHERE email=:%s", EMAIL_STRING));
    deleteQuery.setString(EMAIL_STRING, email);
    int i = deleteQuery.executeUpdate();
    LOGGER.info("Removed {} Access Token with email: {}", i, email);

    deleteQuery = session
        .createQuery(String.format("DELETE FROM MetisUser WHERE email=:%s", EMAIL_STRING));
    deleteQuery.setString(EMAIL_STRING, email);
    i = deleteQuery.executeUpdate();
    LOGGER.info("Removed {} User with email: {}", i, email);

    tx.commit();
    session.flush();
    session.close();
  }

  /**
   * Updates the timestamp of a stored {@link MetisUserAccessToken} using the email.
   *
   * @param email to find the stored {@link MetisUserAccessToken}
   */
  public void updateAccessTokenTimestamp(String email) {
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    Query updateQuery = session
        .createQuery(String.format("UPDATE MetisUserAccessToken SET timestamp=:%s WHERE email=:%s",
            TIMESTAMP_STRING, EMAIL_STRING));
    updateQuery.setTimestamp(TIMESTAMP_STRING, new Date());
    updateQuery.setString(EMAIL_STRING, email);
    int i = updateQuery.executeUpdate();
    LOGGER.info("Updated {} Access Token with email: {}", i, email);
    tx.commit();
    session.flush();
    session.close();
  }

  /**
   * Updates the timestamp of a stored {@link MetisUserAccessToken} using the access token.
   *
   * @param accessToken to find the stored {@link MetisUserAccessToken}
   */
  public void updateAccessTokenTimestampByAccessToken(String accessToken) {
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    Query updateQuery = session.createQuery(
        String.format("UPDATE MetisUserAccessToken SET timestamp=:%s WHERE access_token=:%s",
            TIMESTAMP_STRING, ACCESS_TOKEN_STRING));
    updateQuery.setTimestamp(TIMESTAMP_STRING, new Date());
    updateQuery.setString(ACCESS_TOKEN_STRING, accessToken);
    int i = updateQuery.executeUpdate();
    LOGGER.info("Updated {} Access Token timestamp: {}", i, accessToken);
    tx.commit();
    session.flush();
    session.close();
  }

  /**
   * Updates a users {@link AccountRole} to administrator
   *
   * @param userEmailToMakeAdmin the email to change it's {@link AccountRole}
   */
  public void updateMetisUserToMakeAdmin(String userEmailToMakeAdmin) {
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    Query updateQuery = session.createQuery(String
        .format("UPDATE MetisUser SET account_role=:%s WHERE email=:%s", ACCESS_ROLE_STRING,
            EMAIL_STRING));
    updateQuery.setString(ACCESS_ROLE_STRING, AccountRole.METIS_ADMIN.name());
    updateQuery.setString(EMAIL_STRING, userEmailToMakeAdmin);
    int i = updateQuery.executeUpdate();
    LOGGER.info("Updated {} MetisUser with email: {}, made METIS_ADMIN", i, userEmailToMakeAdmin);
    tx.commit();
    session.flush();
    session.close();
  }

  /**
   * Retrieves a list of all the {@link MetisUser}s.
   *
   * @return list of all {@link MetisUser}s
   */
  public List<MetisUser> getAllMetisUsers() {
    Session session = sessionFactory.openSession();
    List<?> metisUsersObjects = session.createCriteria(MetisUser.class).list();
    List<MetisUser> metisUsers = new ArrayList<>(metisUsersObjects.size());
    for (Object object : metisUsersObjects) {
      metisUsers.add((MetisUser) object);
    }
    session.flush();
    session.close();
    return metisUsers;
  }
}
