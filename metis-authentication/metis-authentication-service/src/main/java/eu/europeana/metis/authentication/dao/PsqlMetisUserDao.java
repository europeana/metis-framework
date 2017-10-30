package eu.europeana.metis.authentication.dao;

import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserToken;
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
    if (query.list().size() > 0) {
      metisUser = (MetisUser) query.list().get(0);
    }
    session.flush();
    session.close();
    return metisUser;
  }

  public void createUserAccessToken(MetisUserToken metisUserToken) {
    createObjectInDB(metisUserToken);
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

}
