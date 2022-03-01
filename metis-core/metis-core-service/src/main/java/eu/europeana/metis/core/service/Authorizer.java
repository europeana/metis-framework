package eu.europeana.metis.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import eu.europeana.metis.utils.CommonStringValues;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUserView;
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
   * @param metisUserView The user wishing to gain access.
   * @throws UserUnauthorizedException In case the user is not authorized.
   */
  void authorizeWriteDefaultXslt(MetisUserView metisUserView) throws UserUnauthorizedException {
    if (metisUserView == null || metisUserView.getAccountRole() != AccountRole.METIS_ADMIN) {
      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);
    }
  }

  /**
   * Authorizes reading access to all datasets. Will return quietly if authorization succeeds.
   * 
   * @param metisUserView The user wishing to gain access.
   * @throws UserUnauthorizedException In case the user is not authorized.
   */
  void authorizeReadAllDatasets(MetisUserView metisUserView) throws UserUnauthorizedException {
    if (metisUserView == null || (metisUserView.getAccountRole() != AccountRole.METIS_ADMIN
        && metisUserView.getAccountRole() != AccountRole.EUROPEANA_DATA_OFFICER)) {
      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);
    }
  }

  /**
   * Authorizes reading access to an existing dataset. Will return quietly if authorization
   * succeeds.
   * 
   * @param metisUserView The user wishing to gain access.
   * @param datasetId The ID of the dataset to which the user wishes to gain access.
   * @return The dataset in question.
   * @throws UserUnauthorizedException In case the user is not authorized.
   * @throws NoDatasetFoundException In case the dataset with the given ID could not be found.
   */
  Dataset authorizeReadExistingDatasetById(MetisUserView metisUserView, String datasetId)
      throws UserUnauthorizedException, NoDatasetFoundException {
    return authorizeExistingDatasetById(metisUserView, datasetId, true);
  }

  /**
   * Authorizes writing access to an existing dataset. Will return quietly if authorization
   * succeeds.
   * 
   * @param metisUserView The user wishing to gain access.
   * @param datasetId The ID of the dataset to which the user wishes to gain access.
   * @return The dataset in question.
   * @throws UserUnauthorizedException In case the user is not authorized.
   * @throws NoDatasetFoundException In case the dataset with the given ID could not be found.
   */
  Dataset authorizeWriteExistingDatasetById(MetisUserView metisUserView, String datasetId)
      throws UserUnauthorizedException, NoDatasetFoundException {
    return authorizeExistingDatasetById(metisUserView, datasetId, false);
  }

  private Dataset authorizeExistingDatasetById(MetisUserView metisUserView, String datasetId,
      boolean allowView) throws UserUnauthorizedException, NoDatasetFoundException {
    return authorizeExistingDataset(metisUserView, allowView, () -> {
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
   * @param metisUserView The user wishing to gain access.
   * @param datasetName The name of the dataset to which the user wishes to gain access.
   * @return The dataset in question.
   * @throws UserUnauthorizedException In case the user is not authorized.
   * @throws NoDatasetFoundException In case the dataset with the given name could not be found.
   */
  Dataset authorizeReadExistingDatasetByName(MetisUserView metisUserView, String datasetName)
      throws UserUnauthorizedException, NoDatasetFoundException {
    return authorizeExistingDataset(metisUserView, true, () -> {
      final Dataset dataset = datasetDao.getDatasetByDatasetName(datasetName);
      if (dataset == null) {
        throw new NoDatasetFoundException(
            String.format("No dataset found with datasetName: '%s' in METIS", datasetName));
      }
      return dataset;
    });
  }

  private Dataset authorizeExistingDataset(MetisUserView metisUserView, boolean allowView,
      DatasetSupplier datasetSupplier) throws UserUnauthorizedException, NoDatasetFoundException {
    checkUserRoleForIndividualDatasetManagement(metisUserView, allowView);
    final Dataset dataset = datasetSupplier.get();
    if (metisUserView.getAccountRole() != AccountRole.METIS_ADMIN
        && !metisUserView.getOrganizationId().equals(dataset.getOrganizationId())) {
      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);
    }
    return dataset;
  }

  /**
   * Authorizes the creation of a new dataset. Will return quietly if authorization succeeds.
   * 
   * @param metisUserView The user wishing to gain access.
   * @throws UserUnauthorizedException In case the user is not authorized.
   */
  void authorizeWriteNewDataset(MetisUserView metisUserView) throws UserUnauthorizedException {
    checkUserRoleForIndividualDatasetManagement(metisUserView, false);
  }

  private void checkUserRoleForIndividualDatasetManagement(MetisUserView metisUserView, boolean allowView)
      throws UserUnauthorizedException {
    if (metisUserView == null || metisUserView.getAccountRole() == null
        || (!allowView && metisUserView.getAccountRole() == AccountRole.PROVIDER_VIEWER)) {
      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);
    }
  }

  @FunctionalInterface
  private interface DatasetSupplier {
    Dataset get() throws NoDatasetFoundException;
  }
}
