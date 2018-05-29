package eu.europeana.metis.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.exception.UserUnauthorizedException;

/**
 * This class takes care of all authorization checks for the services.
 * 
 * @author jochen
 *
 */
@Service
public class Authorizer {

  private final DatasetDao datasetDao;

  /**
   * Constructor.
   * 
   * @param datasetDao The dataset DAO.
   */
  @Autowired
  public Authorizer(DatasetDao datasetDao) {
    this.datasetDao = datasetDao;
  }

  /**
   * Authorizes writing access to the default XSLT. Will return quietly if authorization succeeds.
   * 
   * @param metisUser The user wishing to gain access.
   * @throws UserUnauthorizedException In case the user is not authorized.
   */
  void authorizeWriteDefaultXslt(MetisUser metisUser) throws UserUnauthorizedException {
    if (metisUser == null || metisUser.getAccountRole() != AccountRole.METIS_ADMIN) {
      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);
    }
  }

  /**
   * Authorizes reading access to all datasets. Will return quietly if authorization succeeds.
   * 
   * @param metisUser The user wishing to gain access.
   * @throws UserUnauthorizedException In case the user is not authorized.
   */
  void authorizeReadAllDatasets(MetisUser metisUser) throws UserUnauthorizedException {
    if (metisUser == null || (metisUser.getAccountRole() != AccountRole.METIS_ADMIN
        && metisUser.getAccountRole() != AccountRole.EUROPEANA_DATA_OFFICER)) {
      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);
    }
  }

  /**
   * Authorizes reading access to an existing dataset. Will return quietly if authorization
   * succeeds.
   * 
   * @param metisUser The user wishing to gain access.
   * @param datasetId The ID of the dataset to which the user wishes to gain access.
   * @return The dataset in question.
   * @throws UserUnauthorizedException In case the user is not authorized.
   * @throws NoDatasetFoundException In case the dataset with the given ID could not be found.
   */
  Dataset authorizeReadExistingDatasetById(MetisUser metisUser, String datasetId)
      throws UserUnauthorizedException, NoDatasetFoundException {
    return authorizeExistingDatasetById(metisUser, datasetId, true);
  }

  /**
   * Authorizes writing access to an existing dataset. Will return quietly if authorization
   * succeeds.
   * 
   * @param metisUser The user wishing to gain access.
   * @param datasetId The ID of the dataset to which the user wishes to gain access.
   * @return The dataset in question.
   * @throws UserUnauthorizedException In case the user is not authorized.
   * @throws NoDatasetFoundException In case the dataset with the given ID could not be found.
   */
  Dataset authorizeWriteExistingDatasetById(MetisUser metisUser, String datasetId)
      throws UserUnauthorizedException, NoDatasetFoundException {
    return authorizeExistingDatasetById(metisUser, datasetId, false);
  }

  private Dataset authorizeExistingDatasetById(MetisUser metisUser, String datasetId,
      boolean allowView) throws UserUnauthorizedException, NoDatasetFoundException {
    return authorizeExistingDataset(metisUser, allowView, () -> {
      final Dataset dataset = datasetDao.getDatasetByDatasetId(datasetId);
      if (dataset == null) {
        throw new NoDatasetFoundException(
            String.format("No dataset found with datasetId: '%s' in METIS", datasetId));
      }
      return dataset;
    });
  }

  /**
   * Authorizes reading access to an existing dataset. Will return quietly if authorization
   * succeeds.
   * 
   * @param metisUser The user wishing to gain access.
   * @param datasetName The name of the dataset to which the user wishes to gain access.
   * @return The dataset in question.
   * @throws UserUnauthorizedException In case the user is not authorized.
   * @throws NoDatasetFoundException In case the dataset with the given name could not be found.
   */
  Dataset authorizeReadExistingDatasetByName(MetisUser metisUser, String datasetName)
      throws UserUnauthorizedException, NoDatasetFoundException {
    return authorizeExistingDataset(metisUser, true, () -> {
      final Dataset dataset = datasetDao.getDatasetByDatasetName(datasetName);
      if (dataset == null) {
        throw new NoDatasetFoundException(
            String.format("No dataset found with datasetName: '%s' in METIS", datasetName));
      }
      return dataset;
    });
  }

  private Dataset authorizeExistingDataset(MetisUser metisUser, boolean allowView,
      DatasetSupplier datasetSupplier) throws UserUnauthorizedException, NoDatasetFoundException {
    checkUserRoleForIndividualDatasetManagement(metisUser, allowView);
    final Dataset dataset = datasetSupplier.get();
    if (metisUser.getAccountRole() != AccountRole.METIS_ADMIN
        && !metisUser.getOrganizationId().equals(dataset.getOrganizationId())) {
      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);
    }
    return dataset;
  }

  /**
   * Authorizes the creation of a new dataset. Will return quietly if authorization succeeds.
   * 
   * @param metisUser The user wishing to gain access.
   * @throws UserUnauthorizedException In case the user is not authorized.
   */
  void authorizeWriteNewDataset(MetisUser metisUser) throws UserUnauthorizedException {
    checkUserRoleForIndividualDatasetManagement(metisUser, false);
  }

  private void checkUserRoleForIndividualDatasetManagement(MetisUser metisUser, boolean allowView)
      throws UserUnauthorizedException {
    if (metisUser == null || metisUser.getAccountRole() == null
        || (!allowView && metisUser.getAccountRole() == AccountRole.PROVIDER_VIEWER)) {
      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);
    }
  }

  @FunctionalInterface
  private interface DatasetSupplier {
    Dataset get() throws NoDatasetFoundException;
  }
}
