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
    authorizer.authorizeReadAllDatasets(createUser(AccountRole.METIS_ADMIN));
    authorizer.authorizeReadAllDatasets(createUser(AccountRole.EUROPEANA_DATA_OFFICER));
    expectUnauthorizedException(() -> {
      authorizer.authorizeReadAllDatasets(createUser(AccountRole.PROVIDER_VIEWER));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeReadAllDatasets(createUser(null));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeReadAllDatasets(null);
    });
  }

  @Test
  public void testCreatingDefaultXslt() throws UserUnauthorizedException, NoDatasetFoundException {
    authorizer.authorizeWriteDefaultXslt(createUser(AccountRole.METIS_ADMIN));
    expectUnauthorizedException(() -> {
      authorizer.authorizeWriteDefaultXslt(createUser(AccountRole.EUROPEANA_DATA_OFFICER));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeWriteDefaultXslt(createUser(AccountRole.PROVIDER_VIEWER));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeWriteDefaultXslt(createUser(null));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeWriteDefaultXslt(null);
    });
  }

  @Test
  public void testCreatingNewDataset() throws UserUnauthorizedException, NoDatasetFoundException {
    authorizer.authorizeWriteNewDataset(createUser(AccountRole.METIS_ADMIN));
    authorizer.authorizeWriteNewDataset(createUser(AccountRole.EUROPEANA_DATA_OFFICER));
    expectUnauthorizedException(() -> {
      authorizer.authorizeWriteNewDataset(createUser(AccountRole.PROVIDER_VIEWER));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeWriteNewDataset(createUser(null));
    });
    expectUnauthorizedException(() -> {
      authorizer.authorizeWriteNewDataset(null);
    });
  }

  @Test
  public void testExistingDataset() throws UserUnauthorizedException, NoDatasetFoundException {
    testExistingDataset(authorizer::authorizeWriteExistingDatasetById, Dataset::getDatasetId,
        false);
    testExistingDataset(authorizer::authorizeReadExistingDatasetByName, Dataset::getDatasetName,
        true);
    testExistingDataset(authorizer::authorizeReadExistingDatasetById, Dataset::getDatasetId, true);
  }

  private <T> void testExistingDataset(ExistingDatasetAuthorizer<T> authorizeAction,
      Function<Dataset, T> getDatasetProperty, boolean allowRead)
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
    if (allowRead) {
      final Dataset result4 =
          authorizeAction.authorize(createUserForDataset(AccountRole.PROVIDER_VIEWER, dataset),
              getDatasetProperty.apply(dataset));
      assertSame(dataset, result4);
    }

    // Test unsuccesful authentications
    expectUnauthorizedException(() -> {
      authorizeAction.authorize(
          createUserNotForDataset(AccountRole.EUROPEANA_DATA_OFFICER, dataset),
          getDatasetProperty.apply(dataset));
    });
    if (!allowRead) {
      expectUnauthorizedException(() -> {
        authorizeAction.authorize(createUserForDataset(AccountRole.PROVIDER_VIEWER, dataset),
            getDatasetProperty.apply(dataset));
      });
    }
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
    testNonExistingDataset(authorizer::authorizeWriteExistingDatasetById, "", false);
    testNonExistingDataset(authorizer::authorizeReadExistingDatasetById, "", true);
    testNonExistingDataset(authorizer::authorizeReadExistingDatasetByName, "", true);
  }

  public <T> void testNonExistingDataset(ExistingDatasetAuthorizer<T> authorizeAction,
      T nonExistingValue, boolean allowRead)
      throws UserUnauthorizedException, NoDatasetFoundException {
    expectNoDatasetFoundException(() -> {
      authorizeAction.authorize(createUser(AccountRole.METIS_ADMIN), nonExistingValue);
    });
    expectNoDatasetFoundException(() -> {
      authorizeAction.authorize(createUser(AccountRole.EUROPEANA_DATA_OFFICER), nonExistingValue);
    });
    if (allowRead) {
      expectNoDatasetFoundException(() -> {
        authorizeAction.authorize(createUser(AccountRole.PROVIDER_VIEWER), nonExistingValue);
      });
    } else {
      expectUnauthorizedException(() -> {
        authorizeAction.authorize(createUser(AccountRole.PROVIDER_VIEWER), nonExistingValue);
      });
    }
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
