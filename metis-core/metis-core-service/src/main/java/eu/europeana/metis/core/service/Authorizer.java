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
  void authorizeDefaultXslt(MetisUser metisUser) throws UserUnauthorizedException {
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
  void authorizeAllDatasets(MetisUser metisUser) throws UserUnauthorizedException {
    if (metisUser == null || (metisUser.getAccountRole() != AccountRole.METIS_ADMIN
        && metisUser.getAccountRole() != AccountRole.EUROPEANA_DATA_OFFICER)) {
      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);
    }
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
  Dataset authorizeExistingDatasetById(MetisUser metisUser, String datasetId)
      throws UserUnauthorizedException, NoDatasetFoundException {
    return authorizeExistingDataset(metisUser, () -> {
      final Dataset dataset = datasetDao.getDatasetByDatasetId(datasetId);
      if (dataset == null) {
        throw new NoDatasetFoundException(
            String.format("No dataset found with datasetId: '%s' in METIS", datasetId));
      }
      return dataset;
    });
  }

  /**
   * Authorizes writing access to an existing dataset. Will return quietly if authorization
   * succeeds.
   * 
   * @param metisUser The user wishing to gain access.
   * @param datasetName The name of the dataset to which the user wishes to gain access.
   * @return The dataset in question.
   * @throws UserUnauthorizedException In case the user is not authorized.
   * @throws NoDatasetFoundException In case the dataset with the given name could not be found.
   */
  Dataset authorizeExistingDatasetByName(MetisUser metisUser, String datasetName)
      throws UserUnauthorizedException, NoDatasetFoundException {
    return authorizeExistingDataset(metisUser, () -> {
      final Dataset dataset = datasetDao.getDatasetByDatasetName(datasetName);
      if (dataset == null) {
        throw new NoDatasetFoundException(
            String.format("No dataset found with datasetName: '%s' in METIS", datasetName));
      }
      return dataset;
    });
  }

  private Dataset authorizeExistingDataset(MetisUser metisUser, DatasetSupplier datasetSupplier)
      throws UserUnauthorizedException, NoDatasetFoundException {
    checkUserRoleForIndividualDatasetManagement(metisUser);
    Dataset dataset = datasetSupplier.get();
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
  void authorizeNewDataset(MetisUser metisUser) throws UserUnauthorizedException {
    checkUserRoleForIndividualDatasetManagement(metisUser);
  }

  private void checkUserRoleForIndividualDatasetManagement(MetisUser metisUser)
      throws UserUnauthorizedException {
    if (metisUser == null || metisUser.getAccountRole() == null
        || metisUser.getAccountRole() == AccountRole.PROVIDER_VIEWER) {
      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);
    }
  }

  private static interface DatasetSupplier {
    Dataset get() throws NoDatasetFoundException;
  }
}
