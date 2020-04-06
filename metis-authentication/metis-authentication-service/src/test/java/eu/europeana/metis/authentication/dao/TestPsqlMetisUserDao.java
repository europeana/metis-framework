package eu.europeana.metis.authentication.dao;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import eu.europeana.metis.authentication.user.MetisUserModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.query.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-03
 */
class TestPsqlMetisUserDao {

  private static SessionFactory sessionFactory;
  private static Session session;
  private static Transaction transaction;
  private static org.hibernate.query.Query query;
  private static PsqlMetisUserDao psqlMetisUserDao;

  @BeforeAll
  static void prepareBeforeClass() {
    sessionFactory = Mockito.mock(SessionFactory.class);
    psqlMetisUserDao = new PsqlMetisUserDao(sessionFactory);
    psqlMetisUserDao.setAccessTokenExpireTimeInMins(10);

    session = Mockito.mock(Session.class);
    transaction = Mockito.mock(Transaction.class);
    query = Mockito.mock(Query.class);
  }

  @BeforeEach
  void prepare() {
    when(sessionFactory.openSession()).thenReturn(session);
  }

  @AfterEach
  void cleanUp() {
    Mockito.reset(session);
    Mockito.reset(transaction);
    Mockito.reset(query);
  }

  @Test
  void createMetisUser() {
    when(session.beginTransaction()).thenReturn(transaction);
    psqlMetisUserDao.createMetisUser(new MetisUserModel());

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).persist(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void createMetisUserThrowsExceptionOnCommit() {
    when(session.beginTransaction()).thenReturn(transaction);
    doThrow(new RuntimeException("Exception")).when(transaction).commit();

    assertThrows(TransactionException.class, () -> psqlMetisUserDao.createMetisUser(new MetisUserModel()));

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).persist(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(transaction, times(1)).rollback();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void updateMetisUser() {
    when(session.beginTransaction()).thenReturn(transaction);
    psqlMetisUserDao.updateMetisUser(new MetisUserModel());

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).update(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void updateMetisUserThrowsExceptionOnCommit() {
    when(session.beginTransaction()).thenReturn(transaction);
    doThrow(new RuntimeException("Exception")).when(transaction).commit();
    assertThrows(TransactionException.class, () -> psqlMetisUserDao.updateMetisUser(new MetisUserModel()));

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).update(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(transaction, times(1)).rollback();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void getMetisUserByEmail() {
    when(session.createQuery(any(String.class))).thenReturn(query);
    ArrayList<MetisUserModel> metisUsers = new ArrayList<>(1);
    metisUsers.add(new MetisUserModel());
    when(query.list()).thenReturn(metisUsers);

    psqlMetisUserDao.getMetisUserByEmail("email@email.com");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session);
    inOrder.verify(session, times(1)).createQuery(hqlArgumentCaptor.capture());
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    assertTrue(hqlArgumentCaptor.getValue().contains("FROM MetisUser"));
    assertTrue(hqlArgumentCaptor.getValue().contains("WHERE email"));
  }

  @Test
  void getMetisUserByAccessToken() {
    when(session.createQuery(any(String.class))).thenReturn(query);
    ArrayList<MetisUserAccessToken> metisUserAccessTokens = new ArrayList<>(1);
    MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken();
    metisUserAccessToken.setEmail("email@email.com");
    metisUserAccessTokens.add(metisUserAccessToken);
    ArrayList<MetisUserModel> metisUsers = new ArrayList<>(1);
    metisUsers.add(new MetisUserModel());
    when(query.list()).thenReturn(metisUserAccessTokens).thenReturn(metisUserAccessTokens)
        .thenReturn(metisUsers).thenReturn(metisUsers);

    psqlMetisUserDao.getMetisUserByAccessToken("qwertyuiop");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session);
    inOrder.verify(session, times(2)).createQuery(hqlArgumentCaptor.capture());
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    List<String> allCapturedValues = hqlArgumentCaptor.getAllValues();
    assertTrue(allCapturedValues.get(0).contains("FROM MetisUserAccessToken"));
    assertTrue(allCapturedValues.get(0).contains("WHERE access_token"));
    assertTrue(allCapturedValues.get(1).contains("FROM MetisUser"));
    assertTrue(allCapturedValues.get(1).contains("WHERE email"));
  }

  @Test
  void createUserAccessToken() {
    when(session.beginTransaction()).thenReturn(transaction);
    psqlMetisUserDao.createUserAccessToken(new MetisUserAccessToken());

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).persist(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void createUserAccessTokenThrowsExceptionOnCommit() {
    when(session.beginTransaction()).thenReturn(transaction);
    doThrow(new RuntimeException("Exception")).when(transaction).commit();
    assertThrows(TransactionException.class, () -> psqlMetisUserDao.createUserAccessToken(new MetisUserAccessToken()));

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).persist(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(transaction, times(1)).rollback();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void expireAccessTokens() {
    final CriteriaBuilder builder = Mockito.mock(CriteriaBuilder.class);
    final CriteriaQuery<MetisUserAccessToken> criteriaQuery = Mockito.mock(CriteriaQuery.class);
    final Query<MetisUserAccessToken> query = Mockito.mock(Query.class);

    when(session.beginTransaction()).thenReturn(transaction);
    ArrayList<MetisUserAccessToken> metisUserAccessTokens = new ArrayList<>(1);
    MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken();
    metisUserAccessToken.setEmail("email@email.com");
    metisUserAccessToken.setAccessToken("qwertyuiop");
    Date now = new Date();
    metisUserAccessToken.setTimestamp(new Date(
        now.getTime() - ((psqlMetisUserDao.getAccessTokenExpireTimeInMins() + 1) * 60000)));
    metisUserAccessTokens.add(metisUserAccessToken);
    when(session.getCriteriaBuilder()).thenReturn(builder);
    when(builder.createQuery(MetisUserAccessToken.class)).thenReturn(criteriaQuery);
    when(session.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getResultList()).thenReturn(metisUserAccessTokens).thenReturn(new ArrayList<>());
    when(query.setFirstResult(anyInt())).thenReturn(query);
    when(query.setMaxResults(anyInt())).thenReturn(query);
    when(session.createQuery(any(String.class))).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1);

    psqlMetisUserDao.expireAccessTokens(now);

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session, transaction, query);
    inOrder.verify(session, times(1)).createQuery(hqlArgumentCaptor.capture());
    inOrder.verify(query, times(1)).executeUpdate();
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    assertTrue(hqlArgumentCaptor.getValue().contains("DELETE FROM MetisUserAccessToken"));
    assertTrue(hqlArgumentCaptor.getValue().contains("WHERE access_token"));
  }

  @Test
  void deleteMetisUser() {
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.createQuery(any(String.class))).thenReturn(query).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1).thenReturn(1);

    psqlMetisUserDao.deleteMetisUser("email@email.com");

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(session, times(2)).createQuery(hqlArgumentCaptor.capture());
    verify(query, times(2)).executeUpdate();
    List<String> allCapturedValues = hqlArgumentCaptor.getAllValues();
    assertTrue(allCapturedValues.get(0).contains("DELETE FROM MetisUserAccessToken"));
    assertTrue(allCapturedValues.get(0).contains("WHERE email"));
    assertTrue(allCapturedValues.get(1).contains("DELETE FROM MetisUser"));
    assertTrue(allCapturedValues.get(1).contains("WHERE email"));

  }

  @Test
  void updateAccessTokenTimestamp() {
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.createQuery(any(String.class))).thenReturn(query).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1);

    psqlMetisUserDao.updateAccessTokenTimestamp("email@email.com");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);

    InOrder inOrder = Mockito.inOrder(session, transaction, query);
    inOrder.verify(session, times(1)).createQuery(hqlArgumentCaptor.capture());
    inOrder.verify(query, times(1)).executeUpdate();
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    assertTrue(hqlArgumentCaptor.getValue().contains("UPDATE MetisUserAccessToken"));
    assertTrue(hqlArgumentCaptor.getValue().contains("SET timestamp"));
    assertTrue(hqlArgumentCaptor.getValue().contains("WHERE email"));
  }

  @Test
  void updateAccessTokenTimestampByAccessToken() {
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.createQuery(any(String.class))).thenReturn(query).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1);

    psqlMetisUserDao.updateAccessTokenTimestampByAccessToken("qwertyuiop");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session, transaction, query);
    inOrder.verify(session, times(1)).createQuery(hqlArgumentCaptor.capture());
    inOrder.verify(query, times(1)).executeUpdate();
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    assertTrue(hqlArgumentCaptor.getValue().contains("UPDATE MetisUserAccessToken"));
    assertTrue(hqlArgumentCaptor.getValue().contains("SET timestamp"));
    assertTrue(hqlArgumentCaptor.getValue().contains("WHERE access_token"));
  }

  @Test
  void updateMetisUserToMakeAdmin() {
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.createQuery(any(String.class))).thenReturn(query).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1);

    psqlMetisUserDao.updateMetisUserToMakeAdmin("email@email.com");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session, transaction, query);
    inOrder.verify(session, times(1)).createQuery(hqlArgumentCaptor.capture());
    inOrder.verify(query, times(1)).executeUpdate();
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    assertTrue(hqlArgumentCaptor.getValue().contains("UPDATE MetisUser"));
    assertTrue(hqlArgumentCaptor.getValue().contains("SET account_role"));
    assertTrue(hqlArgumentCaptor.getValue().contains("WHERE email"));
  }

  @Test
  void getAllMetisUsers() {
    final CriteriaBuilder builder = Mockito.mock(CriteriaBuilder.class);
    final CriteriaQuery<MetisUserModel> criteriaQuery = Mockito.mock(CriteriaQuery.class);
    final Query<MetisUserModel> query = Mockito.mock(Query.class);
    ArrayList<MetisUserModel> metisUsers = new ArrayList<>(1);
    metisUsers.add(new MetisUserModel());

    when(session.getCriteriaBuilder()).thenReturn(builder);
    when(builder.createQuery(MetisUserModel.class)).thenReturn(criteriaQuery);
    when(session.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getResultList()).thenReturn(metisUsers);

    psqlMetisUserDao.getAllMetisUsers();

    InOrder inOrder = Mockito.inOrder(session);
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }
}
