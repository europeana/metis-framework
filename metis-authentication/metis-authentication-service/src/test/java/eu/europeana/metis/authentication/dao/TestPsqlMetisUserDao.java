package eu.europeana.metis.authentication.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-03
 */
public class TestPsqlMetisUserDao {

  private static SessionFactory sessionFactory;
  private static Session session;
  private static Transaction transaction;
  private static Query query;
  private static PsqlMetisUserDao psqlMetisUserDao;

  @BeforeClass
  public static void prepareBeforeClass() throws IOException {
    sessionFactory = Mockito.mock(SessionFactory.class);
    psqlMetisUserDao = new PsqlMetisUserDao(sessionFactory);
    psqlMetisUserDao.setAccessTokenExpireTimeInMins(10);

    session = Mockito.mock(Session.class);
    transaction = Mockito.mock(Transaction.class);
    query = Mockito.mock(Query.class);
  }

  @Before
  public void prepare() {
    when(sessionFactory.openSession()).thenReturn(session);
  }

  @After
  public void cleanUp() {
    Mockito.reset(session);
    Mockito.reset(transaction);
    Mockito.reset(query);
  }

  @Test
  public void createMetisUser() {
    when(session.beginTransaction()).thenReturn(transaction);
    psqlMetisUserDao.createMetisUser(new MetisUser());

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).persist(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = TransactionException.class)
  public void createMetisUserThrowsExceptionOnCommit() {
    when(session.beginTransaction()).thenReturn(transaction);
    doThrow(new RuntimeException("Exception")).when(transaction).commit();
    psqlMetisUserDao.createMetisUser(new MetisUser());

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).persist(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(transaction, times(1)).rollback();
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void updateMetisUser() {
    when(session.beginTransaction()).thenReturn(transaction);
    psqlMetisUserDao.updateMetisUser(new MetisUser());

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).update(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = TransactionException.class)
  public void updateMetisUserThrowsExceptionOnCommit() {
    when(session.beginTransaction()).thenReturn(transaction);
    doThrow(new RuntimeException("Exception")).when(transaction).commit();
    psqlMetisUserDao.updateMetisUser(new MetisUser());

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).update(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(transaction, times(1)).rollback();
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void getMetisUserByEmail() {
    when(session.createQuery(any(String.class))).thenReturn(query);
    ArrayList<MetisUser> metisUsers = new ArrayList<>(1);
    metisUsers.add(new MetisUser());
    when(query.list()).thenReturn(metisUsers);

    psqlMetisUserDao.getMetisUserByEmail("email@email.com");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session);
    inOrder.verify(session, times(1)).createQuery(hqlArgumentCaptor.capture());
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    Assert.assertTrue(hqlArgumentCaptor.getValue().contains("FROM MetisUser"));
    Assert.assertTrue(hqlArgumentCaptor.getValue().contains("WHERE email"));
  }

  @Test
  public void getMetisUserByAccessToken() {
    when(session.createQuery(any(String.class))).thenReturn(query);
    ArrayList<MetisUserAccessToken> metisUserAccessTokens = new ArrayList<>(1);
    MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken();
    metisUserAccessToken.setEmail("email@email.com");
    metisUserAccessTokens.add(metisUserAccessToken);
    ArrayList<MetisUser> metisUsers = new ArrayList<>(1);
    metisUsers.add(new MetisUser());
    when(query.list()).thenReturn(metisUserAccessTokens).thenReturn(metisUserAccessTokens)
        .thenReturn(metisUsers).thenReturn(metisUsers);

    psqlMetisUserDao.getMetisUserByAccessToken("qwertyuiop");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session);
    inOrder.verify(session, times(2)).createQuery(hqlArgumentCaptor.capture());
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    List<String> allCapturedValues = hqlArgumentCaptor.getAllValues();
    Assert.assertTrue(allCapturedValues.get(0).contains("FROM MetisUserAccessToken"));
    Assert.assertTrue(allCapturedValues.get(0).contains("WHERE access_token"));
    Assert.assertTrue(allCapturedValues.get(1).contains("FROM MetisUser"));
    Assert.assertTrue(allCapturedValues.get(1).contains("WHERE email"));
  }

  @Test
  public void createUserAccessToken() {
    when(session.beginTransaction()).thenReturn(transaction);
    psqlMetisUserDao.createUserAccessToken(new MetisUserAccessToken());

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).persist(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = TransactionException.class)
  public void createUserAccessTokenThrowsExceptionOnCommit() {
    when(session.beginTransaction()).thenReturn(transaction);
    doThrow(new RuntimeException("Exception")).when(transaction).commit();
    psqlMetisUserDao.createUserAccessToken(new MetisUserAccessToken());

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).persist(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(transaction, times(1)).rollback();
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void expireAccessTokens() {
    Criteria criteria = Mockito.mock(Criteria.class);
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.createCriteria(MetisUserAccessToken.class)).thenReturn(criteria);
    ArrayList<MetisUserAccessToken> metisUserAccessTokens = new ArrayList<>(1);
    MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken();
    metisUserAccessToken.setEmail("email@email.com");
    metisUserAccessToken.setAccessToken("qwertyuiop");
    Date now = new Date();
    metisUserAccessToken.setTimestamp(new Date(
        now.getTime() - ((psqlMetisUserDao.getAccessTokenExpireTimeInMins() + 1) * 60000)));
    metisUserAccessTokens.add(metisUserAccessToken);
    when(criteria.list()).thenReturn(metisUserAccessTokens).thenReturn(new ArrayList<>());
    when(criteria.setFirstResult(anyInt())).thenReturn(criteria);
    when(criteria.setMaxResults(anyInt())).thenReturn(criteria);
    when(session.createQuery(any(String.class))).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1);

    psqlMetisUserDao.expireAccessTokens(now);

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session, transaction, query);
    inOrder.verify(session, times(1)).createQuery(hqlArgumentCaptor.capture());
    inOrder.verify(query, times(1)).executeUpdate();
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    Assert.assertTrue(hqlArgumentCaptor.getValue().contains("DELETE FROM MetisUserAccessToken"));
    Assert.assertTrue(hqlArgumentCaptor.getValue().contains("WHERE access_token"));
  }

  @Test
  public void deleteMetisUser() {
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.createQuery(any(String.class))).thenReturn(query).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1).thenReturn(1);

    psqlMetisUserDao.deleteMetisUser("email@email.com");

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(session, times(2)).createQuery(hqlArgumentCaptor.capture());
    verify(query, times(2)).executeUpdate();
    List<String> allCapturedValues = hqlArgumentCaptor.getAllValues();
    Assert.assertTrue(allCapturedValues.get(0).contains("DELETE FROM MetisUserAccessToken"));
    Assert.assertTrue(allCapturedValues.get(0).contains("WHERE email"));
    Assert.assertTrue(allCapturedValues.get(1).contains("DELETE FROM MetisUser"));
    Assert.assertTrue(allCapturedValues.get(1).contains("WHERE email"));

  }

  @Test
  public void updateAccessTokenTimestamp() {
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.createQuery(any(String.class))).thenReturn(query).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1);

    psqlMetisUserDao.updateAccessTokenTimestamp("email@email.com");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);

    InOrder inOrder = Mockito.inOrder(session, transaction, query);
    inOrder.verify(session, times(1)).createQuery(hqlArgumentCaptor.capture());
    inOrder.verify(query, times(1)).executeUpdate();
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    Assert.assertTrue(hqlArgumentCaptor.getValue().contains("UPDATE MetisUserAccessToken"));
    Assert.assertTrue(hqlArgumentCaptor.getValue().contains("SET timestamp"));
    Assert.assertTrue(hqlArgumentCaptor.getValue().contains("WHERE email"));
  }

  @Test
  public void updateAccessTokenTimestampByAccessToken()
  {
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.createQuery(any(String.class))).thenReturn(query).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1);

    psqlMetisUserDao.updateAccessTokenTimestampByAccessToken("qwertyuiop");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session, transaction, query);
    inOrder.verify(session, times(1)).createQuery(hqlArgumentCaptor.capture());
    inOrder.verify(query, times(1)).executeUpdate();
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    Assert.assertTrue(hqlArgumentCaptor.getValue().contains("UPDATE MetisUserAccessToken"));
    Assert.assertTrue(hqlArgumentCaptor.getValue().contains("SET timestamp"));
    Assert.assertTrue(hqlArgumentCaptor.getValue().contains("WHERE access_token"));
  }

  @Test
  public void updateMetisUserToMakeAdmin()
  {
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.createQuery(any(String.class))).thenReturn(query).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1);

    psqlMetisUserDao.updateMetisUserToMakeAdmin("email@email.com");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session, transaction, query);
    inOrder.verify(session, times(1)).createQuery(hqlArgumentCaptor.capture());
    inOrder.verify(query, times(1)).executeUpdate();
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    Assert.assertTrue(hqlArgumentCaptor.getValue().contains("UPDATE MetisUser"));
    Assert.assertTrue(hqlArgumentCaptor.getValue().contains("SET account_role"));
    Assert.assertTrue(hqlArgumentCaptor.getValue().contains("WHERE email"));
  }

  @Test
  public void getAllMetisUsers()
  {
    Criteria criteria = Mockito.mock(Criteria.class);
    ArrayList<MetisUser> metisUsers = new ArrayList<>(1);
    metisUsers.add(new MetisUser());

    when(session.createCriteria(MetisUser.class)).thenReturn(criteria);
    when(criteria.list()).thenReturn(metisUsers);

    psqlMetisUserDao.getAllMetisUsers();

    InOrder inOrder = Mockito.inOrder(session);
    inOrder.verify(session, times(1)).flush();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }
}
