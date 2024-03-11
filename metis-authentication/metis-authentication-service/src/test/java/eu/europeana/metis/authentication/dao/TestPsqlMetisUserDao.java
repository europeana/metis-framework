package eu.europeana.metis.authentication.dao;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserAccessToken;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.Query;
import org.hibernate.query.SelectionQuery;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

class TestPsqlMetisUserDao {

  private static SessionFactory sessionFactory;
  private static Session session;
  private static Transaction transaction;
  private static PsqlMetisUserDao psqlMetisUserDao;

  @BeforeAll
  static void prepareBeforeClass() {
    sessionFactory = Mockito.mock(SessionFactory.class);
    psqlMetisUserDao = new PsqlMetisUserDao(sessionFactory);
    psqlMetisUserDao.setAccessTokenExpireTimeInMins(10);

    session = Mockito.mock(Session.class);
    transaction = Mockito.mock(Transaction.class);
  }

  @BeforeEach
  void prepare() {
    when(sessionFactory.openSession()).thenReturn(session);
  }

  @AfterEach
  void cleanUp() {
    Mockito.reset(session);
    Mockito.reset(transaction);
  }

  @Test
  void createMetisUser() {
    when(session.beginTransaction()).thenReturn(transaction);
    psqlMetisUserDao.createMetisUser(new MetisUser());

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

    final MetisUser metisUser = new MetisUser();
    assertThrows(TransactionException.class, () -> psqlMetisUserDao.createMetisUser(metisUser));

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
    psqlMetisUserDao.updateMetisUser(new MetisUser());

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).merge(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void updateMetisUserThrowsExceptionOnCommit() {
    when(session.beginTransaction()).thenReturn(transaction);
    doThrow(new RuntimeException("Exception")).when(transaction).commit();
    final MetisUser metisUser = new MetisUser();
    assertThrows(TransactionException.class, () -> psqlMetisUserDao.updateMetisUser(metisUser));

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).merge(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(transaction, times(1)).rollback();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void getMetisUserByEmail() {
    SelectionQuery<MetisUser> selectionQuery = Mockito.mock(SelectionQuery.class);
    when(session.createSelectionQuery(any(String.class), eq(MetisUser.class))).thenReturn(selectionQuery);
    ArrayList<MetisUser> metisUsers = new ArrayList<>(1);
    metisUsers.add(new MetisUser());
    when(selectionQuery.list()).thenReturn(metisUsers);

    psqlMetisUserDao.getMetisUserByEmail("email@email.com");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session);
    inOrder.verify(session, times(1)).createSelectionQuery(hqlArgumentCaptor.capture(), any());
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    assertTrue(hqlArgumentCaptor.getValue().contains("FROM MetisUser"));
    assertTrue(hqlArgumentCaptor.getValue().contains("WHERE email"));
  }

  @Test
  void getMetisUserByAccessToken() {
    SelectionQuery<MetisUserAccessToken> queryMetisUserAccessToken = Mockito.mock(SelectionQuery.class);
    when(session.createSelectionQuery(any(String.class), eq(MetisUserAccessToken.class))).thenReturn(queryMetisUserAccessToken);
    SelectionQuery<MetisUser> queryMetisUser = Mockito.mock(SelectionQuery.class);
    when(session.createSelectionQuery(any(String.class), eq(MetisUser.class))).thenReturn(queryMetisUser);
    ArrayList<MetisUserAccessToken> metisUserAccessTokens = new ArrayList<>(1);
    MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken();
    metisUserAccessToken.setEmail("email@email.com");
    metisUserAccessTokens.add(metisUserAccessToken);
    ArrayList<MetisUser> metisUsers = new ArrayList<>(1);
    metisUsers.add(new MetisUser());
    when(queryMetisUserAccessToken.list()).thenReturn(metisUserAccessTokens).thenReturn(metisUserAccessTokens);
    when(queryMetisUser.list()).thenReturn(metisUsers).thenReturn(metisUsers);

    psqlMetisUserDao.getMetisUserByAccessToken("qwertyuiop");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session);
    inOrder.verify(session, times(1)).createSelectionQuery(hqlArgumentCaptor.capture(), eq(MetisUserAccessToken.class));
    inOrder.verify(session, times(1)).createSelectionQuery(hqlArgumentCaptor.capture(), eq(MetisUser.class));
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    List<String> allCapturedValues = hqlArgumentCaptor.getAllValues();
    assertTrue(allCapturedValues.get(0).contains("FROM MetisUserAccessToken"));
    assertTrue(allCapturedValues.get(0).contains("WHERE accessToken"));
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
    final MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken();
    assertThrows(TransactionException.class, () -> psqlMetisUserDao.createUserAccessToken(metisUserAccessToken));

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(session, times(1)).persist(any(Object.class));
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(transaction, times(1)).rollback();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void expireAccessTokens() {
    final HibernateCriteriaBuilder builder = Mockito.mock(HibernateCriteriaBuilder.class);
    final JpaCriteriaQuery<MetisUserAccessToken> criteriaQuery = Mockito.mock(JpaCriteriaQuery.class);
    final Query<MetisUserAccessToken> query = Mockito.mock(Query.class);

    when(session.beginTransaction()).thenReturn(transaction);
    ArrayList<MetisUserAccessToken> metisUserAccessTokens = new ArrayList<>(1);
    MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken();
    metisUserAccessToken.setEmail("email@email.com");
    metisUserAccessToken.setAccessToken("qwertyuiop");
    Date now = new Date();
    metisUserAccessToken.setTimestamp(new Date(
        now.getTime() - ((psqlMetisUserDao.getAccessTokenExpireTimeInMins() + 1) * 60000L)));
    metisUserAccessTokens.add(metisUserAccessToken);
    when(session.getCriteriaBuilder()).thenReturn(builder);
    when(builder.createQuery(MetisUserAccessToken.class)).thenReturn(criteriaQuery);
    when(session.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getResultList()).thenReturn(metisUserAccessTokens).thenReturn(new ArrayList<>());
    when(query.setFirstResult(anyInt())).thenReturn(query);
    when(query.setMaxResults(anyInt())).thenReturn(query);
    when(session.createMutationQuery(any(String.class))).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1);

    psqlMetisUserDao.expireAccessTokens(now);

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session, transaction, query);
    inOrder.verify(session, times(1)).createMutationQuery(hqlArgumentCaptor.capture());
    inOrder.verify(query, times(1)).executeUpdate();
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    assertTrue(hqlArgumentCaptor.getValue().contains("DELETE FROM MetisUserAccessToken"));
    assertTrue(hqlArgumentCaptor.getValue().contains("WHERE accessToken"));
  }

  @Test
  void deleteMetisUser() {
    MutationQuery mutationQuery = Mockito.mock(MutationQuery.class);
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.createMutationQuery(any(String.class))).thenReturn(mutationQuery).thenReturn(mutationQuery);
    when(mutationQuery.executeUpdate()).thenReturn(1).thenReturn(1);

    psqlMetisUserDao.deleteMetisUser("email@email.com");

    InOrder inOrder = Mockito.inOrder(session, transaction);
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(session, times(2)).createMutationQuery(hqlArgumentCaptor.capture());
    verify(mutationQuery, times(2)).executeUpdate();
    List<String> allCapturedValues = hqlArgumentCaptor.getAllValues();
    assertTrue(allCapturedValues.get(0).contains("DELETE FROM MetisUserAccessToken"));
    assertTrue(allCapturedValues.get(0).contains("WHERE email"));
    assertTrue(allCapturedValues.get(1).contains("DELETE FROM MetisUser"));
    assertTrue(allCapturedValues.get(1).contains("WHERE email"));

  }

  @Test
  void updateAccessTokenTimestamp() {
    MutationQuery mutationQuery = Mockito.mock(MutationQuery.class);
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.createMutationQuery(any(String.class))).thenReturn(mutationQuery).thenReturn(mutationQuery);
    when(mutationQuery.executeUpdate()).thenReturn(1);

    psqlMetisUserDao.updateAccessTokenTimestamp("email@email.com");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);

    InOrder inOrder = Mockito.inOrder(session, transaction, mutationQuery);
    inOrder.verify(session, times(1)).createMutationQuery(hqlArgumentCaptor.capture());
    inOrder.verify(mutationQuery, times(1)).executeUpdate();
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    assertTrue(hqlArgumentCaptor.getValue().contains("UPDATE MetisUserAccessToken"));
    assertTrue(hqlArgumentCaptor.getValue().contains("SET timestamp"));
    assertTrue(hqlArgumentCaptor.getValue().contains("WHERE email"));
  }

  @Test
  void updateAccessTokenTimestampByAccessToken() {
    MutationQuery mutationQuery = Mockito.mock(MutationQuery.class);
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.createMutationQuery(any(String.class))).thenReturn(mutationQuery).thenReturn(mutationQuery);
    when(mutationQuery.executeUpdate()).thenReturn(1);

    psqlMetisUserDao.updateAccessTokenTimestampByAccessToken("qwertyuiop");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session, transaction, mutationQuery);
    inOrder.verify(session, times(1)).createMutationQuery(hqlArgumentCaptor.capture());
    inOrder.verify(mutationQuery, times(1)).executeUpdate();
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    assertTrue(hqlArgumentCaptor.getValue().contains("UPDATE MetisUserAccessToken"));
    assertTrue(hqlArgumentCaptor.getValue().contains("SET timestamp"));
    assertTrue(hqlArgumentCaptor.getValue().contains("WHERE accessToken"));
  }

  @Test
  void updateMetisUserToMakeAdmin() {
    MutationQuery mutationQuery = Mockito.mock(MutationQuery.class);
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.createMutationQuery(any(String.class))).thenReturn(mutationQuery).thenReturn(mutationQuery);
    when(mutationQuery.executeUpdate()).thenReturn(1);

    psqlMetisUserDao.updateMetisUserToMakeAdmin("email@email.com");

    ArgumentCaptor<String> hqlArgumentCaptor = ArgumentCaptor.forClass(String.class);
    InOrder inOrder = Mockito.inOrder(session, transaction, mutationQuery);
    inOrder.verify(session, times(1)).createMutationQuery(hqlArgumentCaptor.capture());
    inOrder.verify(mutationQuery, times(1)).executeUpdate();
    inOrder.verify(transaction, times(1)).commit();
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();

    assertTrue(hqlArgumentCaptor.getValue().contains("UPDATE MetisUser"));
    assertTrue(hqlArgumentCaptor.getValue().contains("SET accountRole"));
    assertTrue(hqlArgumentCaptor.getValue().contains("WHERE email"));
  }

  @Test
  void getAllMetisUsers() {
    final HibernateCriteriaBuilder builder = Mockito.mock(HibernateCriteriaBuilder.class);
    final JpaCriteriaQuery<MetisUser> criteriaQuery = Mockito.mock(JpaCriteriaQuery.class);
    final Query<MetisUser> query = Mockito.mock(Query.class);
    ArrayList<MetisUser> metisUsers = new ArrayList<>(1);
    metisUsers.add(new MetisUser());

    when(session.getCriteriaBuilder()).thenReturn(builder);
    when(builder.createQuery(MetisUser.class)).thenReturn(criteriaQuery);
    when(session.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getResultList()).thenReturn(metisUsers);

    psqlMetisUserDao.getAllMetisUsers();

    InOrder inOrder = Mockito.inOrder(session);
    inOrder.verify(session, times(1)).close();
    inOrder.verifyNoMoreInteractions();
  }
}
