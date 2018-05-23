package eu.europeana.metis.core.service;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.exception.UserUnauthorizedException;

public class AuthorizerTest {

  private DatasetDao datasetDao;
  private Authorizer authorizer;

  @Before
  public void prepare() {
    datasetDao = mock(DatasetDao.class);
    authorizer = new Authorizer(datasetDao);
  }

  private Dataset createAndRegisterDataset() {
    final Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    doReturn(dataset).when(datasetDao).getDatasetByDatasetId(dataset.getDatasetId());
    doReturn(dataset).when(datasetDao).getDatasetByDatasetName(dataset.getDatasetName());
    return dataset;
  }

  private static MetisUser createUser(AccountRole accountRole) {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(accountRole);
    return metisUser;
  }

  private static MetisUser createUserForDataset(AccountRole accountRole, Dataset dataset) {
    final MetisUser metisUser = createUser(accountRole);
    metisUser.setOrganizationId(dataset.getOrganizationId());
    return metisUser;
  }

  private static MetisUser createUserNotForDataset(AccountRole accountRole, Dataset dataset) {
    final MetisUser metisUser = createUser(accountRole);
    metisUser.setOrganizationId("not_" + dataset.getOrganizationId());
    return metisUser;
  }

  private static void expectUnauthorizedException(TestAction action)
      throws NoDatasetFoundException {
    try {
      action.test();
      fail();
    } catch (UserUnauthorizedException e) {
      // Success
    }
  }

  private static void expectNoDatasetFoundException(TestAction action)
      throws UserUnauthorizedException {
    try {
      action.test();
      fail();
    } catch (NoDatasetFoundException e) {
      // Success
    }
  }

  @Test
  public void testGetAllDatasets() throws UserUnauthorizedException, NoDatasetFoundException {
    authorizer.authorizeAllDatasets(createUser(AccountRole.METIS_ADMIN));
    authorizer.authorizeAllDatasets(createUser(AccountRole.EUROPEANA_DATA_OFFICER));
    expectUnauthorizedException(() -> {
      authorizer.authorizeAllDatasets(createUser(AccountRole.PROVIDER_VIEWER));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeAllDatasets(createUser(null));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeAllDatasets(null);
    });
  }

  @Test
  public void testCreatingDefaultXslt() throws UserUnauthorizedException, NoDatasetFoundException {
    authorizer.authorizeDefaultXslt(createUser(AccountRole.METIS_ADMIN));
    expectUnauthorizedException(() -> {
      authorizer.authorizeDefaultXslt(createUser(AccountRole.EUROPEANA_DATA_OFFICER));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeDefaultXslt(createUser(AccountRole.PROVIDER_VIEWER));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeDefaultXslt(createUser(null));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeDefaultXslt(null);
    });
  }

  @Test
  public void testCreatingNewDataset() throws UserUnauthorizedException, NoDatasetFoundException {
    authorizer.authorizeNewDataset(createUser(AccountRole.METIS_ADMIN));
    authorizer.authorizeNewDataset(createUser(AccountRole.EUROPEANA_DATA_OFFICER));
    expectUnauthorizedException(() -> {
      authorizer.authorizeNewDataset(createUser(AccountRole.PROVIDER_VIEWER));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeNewDataset(createUser(null));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeNewDataset(null);
    });
  }

  @Test
  public void testExistingDataset() throws UserUnauthorizedException, NoDatasetFoundException {
    testExistingDataset(authorizer::authorizeExistingDatasetById, Dataset::getDatasetId);
    testExistingDataset(authorizer::authorizeExistingDatasetByName, Dataset::getDatasetName);
  }

  private <T> void testExistingDataset(ExistingDatasetAuthorizer<T> authorizeAction,
      Function<Dataset, T> getDatasetProperty)
      throws UserUnauthorizedException, NoDatasetFoundException {

    // Create dataset
    final Dataset dataset = createAndRegisterDataset();

    // Test successful authentications
    final Dataset result1 = authorizeAction.authorize(
        createUserForDataset(AccountRole.METIS_ADMIN, dataset), getDatasetProperty.apply(dataset));
    assertSame(dataset, result1);
    final Dataset result2 =
        authorizeAction.authorize(createUserNotForDataset(AccountRole.METIS_ADMIN, dataset),
            getDatasetProperty.apply(dataset));
    assertSame(dataset, result2);
    final Dataset result3 =
        authorizeAction.authorize(createUserForDataset(AccountRole.EUROPEANA_DATA_OFFICER, dataset),
            getDatasetProperty.apply(dataset));
    assertSame(dataset, result3);

    // Test unsuccesful authentications
    expectUnauthorizedException(() -> {
      authorizeAction.authorize(
          createUserNotForDataset(AccountRole.EUROPEANA_DATA_OFFICER, dataset),
          getDatasetProperty.apply(dataset));
    });
    expectUnauthorizedException(() -> {
      authorizeAction.authorize(createUserForDataset(AccountRole.PROVIDER_VIEWER, dataset),
          getDatasetProperty.apply(dataset));
    });
    expectUnauthorizedException(() -> {
      authorizeAction.authorize(createUserNotForDataset(AccountRole.PROVIDER_VIEWER, dataset),
          getDatasetProperty.apply(dataset));
    });
    expectUnauthorizedException(() -> {
      authorizeAction.authorize(createUserForDataset(null, dataset),
          getDatasetProperty.apply(dataset));
    });
    expectUnauthorizedException(() -> {
      authorizeAction.authorize(createUserNotForDataset(null, dataset),
          getDatasetProperty.apply(dataset));
    });
    expectUnauthorizedException(() -> {
      authorizeAction.authorize(null, getDatasetProperty.apply(dataset));
    });
  }

  @Test
  public void testNonExistingDatasetForId()
      throws UserUnauthorizedException, NoDatasetFoundException {
    testNonExistingDataset(authorizer::authorizeExistingDatasetById, "");
    testNonExistingDataset(authorizer::authorizeExistingDatasetByName, "");
  }

  public <T> void testNonExistingDataset(ExistingDatasetAuthorizer<T> authorizeAction,
      T nonExistingValue) throws UserUnauthorizedException, NoDatasetFoundException {
    expectNoDatasetFoundException(() -> {
      authorizeAction.authorize(createUser(AccountRole.METIS_ADMIN), nonExistingValue);
    });
    expectNoDatasetFoundException(() -> {
      authorizeAction.authorize(createUser(AccountRole.EUROPEANA_DATA_OFFICER), nonExistingValue);
    });
    expectUnauthorizedException(() -> {
      authorizeAction.authorize(createUser(AccountRole.PROVIDER_VIEWER), nonExistingValue);
    });
    expectUnauthorizedException(() -> {
      authorizeAction.authorize(createUser(null), nonExistingValue);
    });
    expectUnauthorizedException(() -> {
      authorizeAction.authorize(null, nonExistingValue);
    });
  }

  private interface TestAction {
    void test() throws UserUnauthorizedException, NoDatasetFoundException;
  }

  private interface ExistingDatasetAuthorizer<T> {
    Dataset authorize(MetisUser metisUser, T property)
        throws UserUnauthorizedException, NoDatasetFoundException;
  }
}
