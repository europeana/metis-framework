/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.core.service;

import eu.europeana.metis.core.common.AltLabel;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.OrganizationRole;
import eu.europeana.metis.core.common.PrefLabel;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.OrganizationDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
import eu.europeana.metis.core.exceptions.OrganizationAlreadyExistsException;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.search.common.OrganizationSearchBean;
import eu.europeana.metis.core.search.service.MetisSearchService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Organization service
 * Created by ymamakis on 2/17/16.
 */
@Service
public class OrganizationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationService.class);

  private OrganizationDao organizationDao;
  private DatasetDao datasetDao;
  private MetisSearchService searchService;

  @Autowired
  public OrganizationService(OrganizationDao organizationDao,
      DatasetDao datasetDao,
      MetisSearchService searchService) {
    this.organizationDao = organizationDao;
    this.datasetDao = datasetDao;
    this.searchService = searchService;
  }

  public void createOrganization(Organization org) throws IOException, SolrServerException {
    organizationDao.create(org);
    saveSearchTermsInSolr(org);
  }

  public void updateOrganization(Organization org) throws SolrServerException, IOException {
    organizationDao.update(org);
    updateSearchTermsInSolr(org);
  }

  public void updateOrganizationDatasetNamesList(String organizationId, String datasetName) {
    organizationDao.updateOrganizationDatasetNamesList(organizationId, datasetName);
  }

  public void removeOrganizationDatasetNameFromList(String organizationId, String datasetName) {
    organizationDao.removeOrganizationDatasetNameFromList(organizationId, datasetName);
  }

  public void deleteOrganization(Organization org) throws IOException, SolrServerException {
    organizationDao.delete(org);
    searchService.deleteFromSearch(org.getId().toString());
  }

  public void deleteOrganizationByOrganizationId(String organizationId)
      throws IOException, SolrServerException {
    organizationDao.deleteByOrganizationId(organizationId);
    searchService.deleteFromSearchByOrganizationId(organizationId);
  }

  public List<Organization> getAllOrganizations(String nextPage) {
    return organizationDao.getAllOrganizations(nextPage);
  }

  public List<Organization> getAllOrganizationsByOrganizationRole(
      List<OrganizationRole> organizationRoles, String nextPage) {
    return organizationDao.getAllOrganizationsByOrganizationRole(organizationRoles, nextPage);
  }

  public List<Organization> getAllOrganizationsByCountry(Country country, String nextPage) {
    return organizationDao.getAllOrganizationsByCountry(country, nextPage);
  }

  public List<Dataset> getAllDatasetsByOrganizationId(String organizationId, String nextPage)
      throws NoOrganizationFoundException {
    getOrganizationByOrganizationId(organizationId);
    return datasetDao.getAllDatasetsByOrganizationId(organizationId, nextPage);
  }

  public Organization getOrganizationByOrganizationId(String organizationId)
      throws NoOrganizationFoundException {

    Organization organization = organizationDao.getOrganizationByOrganizationId(organizationId);
    if (organization == null) {
      throw new NoOrganizationFoundException(
          String.format("No organization found with organization id: %s in METIS", organizationId));
    }
    return organization;
  }

  public int getOrganizationsPerRequestLimit() {
    return organizationDao.getOrganizationsPerRequest();
  }

  public boolean isOptedInIIIF(String organizationId) throws NoOrganizationFoundException {
    Organization organization = organizationDao
        .getOrganizationOptInIIIFByOrganizationId(organizationId);
    if (organization == null) {
      throw new NoOrganizationFoundException(
          String.format("No organization found with organization id: %s in METIS", organizationId));
    }
    return organization.isOptInIIIF();
  }

  public List<OrganizationSearchBean> suggestOrganizations(String searchTerm)
      throws IOException, SolrServerException {
    return searchService.getSuggestions(searchTerm);
  }

  public void checkRestrictionsOnCreate(Organization organization)
      throws BadContentException, OrganizationAlreadyExistsException {
    if (StringUtils.isEmpty(organization.getOrganizationId())) {
      throw new BadContentException("OrganizationId cannot be null");
    }
    if (existsOrganizaitonByOrganizationId(organization.getOrganizationId())) {
      throw new OrganizationAlreadyExistsException(organization.getOrganizationId());
    }
    LOGGER.info("Organization not found, so it can be created");

    if (organization.getDatasetNames() != null
        && !organization.getDatasetNames().isEmpty()) {
      throw new BadContentException("The field 'datasetNames' is not allowed on creation");
    }
  }

  public void checkRestrictionsOnUpdate(Organization organization, String organizationId)
      throws BadContentException, NoOrganizationFoundException {
    if (!StringUtils.isEmpty(organization.getOrganizationId()) && !organization
        .getOrganizationId().equals(organizationId)) {
      throw new BadContentException(
          String.format("OrganinazationId in body %s is different from parameter %s",
              organization.getOrganizationId(), organizationId));
    }
    if (organization.getDatasetNames() != null
        && !organization.getDatasetNames().isEmpty()) {
      throw new BadContentException("The field 'datasetNames' is not allowed on update");
    }
    organization.setOrganizationId(organizationId);

    //Check if it exist and if not throws exception
    getOrganizationByOrganizationId(organization.getOrganizationId());
  }

  private boolean existsOrganizaitonByOrganizationId(String organizationId) {
    return organizationDao.existsOrganizationByOrganizationId(organizationId);
  }

  private void saveSearchTermsInSolr(Organization org) throws IOException, SolrServerException {
    String id = org.getId().toString();
    String englabel = org.getName();
    String organizationId = org.getOrganizationId();
    List<String> searchLabel = getSearchLabels(org, englabel);
    searchService.addOrganizationForSearch(id, organizationId, englabel, searchLabel);
    LOGGER.info("Organization {} saved in solr", org.getOrganizationId());
  }

  private void updateSearchTermsInSolr(Organization org) throws IOException, SolrServerException {
    String id = searchService
        .findSolrIdByOrganizationId(org.getOrganizationId());
    String englabel = org.getName();
    String organizationId = org.getOrganizationId();
    List<String> searchLabel = getSearchLabels(org, englabel);
    searchService.addOrganizationForSearch(id, organizationId, englabel, searchLabel);
    LOGGER.info("Organization {} saved in solr", org.getOrganizationId());
  }

  private List<String> getSearchLabels(Organization org, String englabel) {
    List<String> searchLabel = new ArrayList<>();
    searchLabel.add(englabel);
    if (org.getPrefLabel() != null) {
      for (PrefLabel label : org.getPrefLabel()) {
        searchLabel.add(label.getLabel());
      }
    }
    if (org.getAltLabel() != null) {
      for (AltLabel label : org.getAltLabel()) {
        searchLabel.add(label.getLabel());
      }
    }
    return searchLabel;
  }
}
