package eu.europeana.metis.authentication.dao;

import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
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

  private SessionFactory sessionFactory;

  @Autowired
  public PsqlMetisUserDao(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void createMetisUser(MetisUser metisUser) {
    createObjectInDB(metisUser);
  }

  public MetisUser getMetisUserByEmail(String email) {
    Session session = sessionFactory.openSession();

    String hql = String.format("FROM MetisUser WHERE email = '%s'", email);
    Query query = session.createQuery(hql);
    MetisUser metisUser = null;
    if (!query.list().isEmpty()) {
      metisUser = (MetisUser) query.list().get(0);
    }
    session.flush();
    session.close();
    return metisUser;
  }

  public void createUserAccessToken(MetisUserAccessToken metisUserAccessToken) {
    createObjectInDB(metisUserAccessToken);
  }

  private void createObjectInDB(Object o) {
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();

    try {
      session.persist(o);
      tx.commit();
    } catch (Exception e) {
      tx.rollback();
      LOGGER.error("Could not persist object, rolling back..");
      throw new TransactionException("Could not persist object in database", e);
    } finally {
      session.flush();
      session.close();
    }
  }

  public void expireAccessTokens(Date now) {
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    final long oneMinuteInMillis = 60000;

    int offset = 0;
    int pageSize = 100;
    List<MetisUserAccessToken> metisUserAccessTokens;
    do {
      Criteria criteria = session.createCriteria(MetisUserAccessToken.class).setFirstResult(offset)
          .setMaxResults(pageSize);
      metisUserAccessTokens = criteria.list();
      if (!metisUserAccessTokens.isEmpty()) {
        for (Object object : metisUserAccessTokens) {
          MetisUserAccessToken metisUserAccessToken = (MetisUserAccessToken) object;
          long accessTokenInMillis = metisUserAccessToken.getTimestamp().getTime();
          Date afterAddingTenMins = new Date(accessTokenInMillis + (10 * oneMinuteInMillis));
          if (afterAddingTenMins.compareTo(now) <= 0) {
            //Remove access token
            String hql = String.format("DELETE FROM MetisUserAccessToken WHERE access_token='%s'",
                metisUserAccessToken.getAccessToken());
            Query deleteQuery = session.createQuery(hql);
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
}
