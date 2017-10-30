package eu.europeana.metis.authentication.dao;

import eu.europeana.metis.authentication.user.MetisUser;
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
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();

    try {
      session.persist(metisUser);
      tx.commit();
    } catch (Exception e) {
      tx.rollback();
      LOGGER.error("Could not save user, rolling back..");
      throw new TransactionException("Could not persist user in Database", e);
    } finally {
      session.flush();
      session.close();
    }
  }

}
