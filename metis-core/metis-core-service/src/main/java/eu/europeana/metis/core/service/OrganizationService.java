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
import eu.europeana.metis.core.dao.ZohoClient;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
import eu.europeana.metis.core.exceptions.OrganizationAlreadyExistsException;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.search.common.OrganizationSearchBean;
import eu.europeana.metis.core.search.service.MetisSearchService;
import java.io.IOException;
import java.text.ParseException;
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

  private final Logger LOGGER = LoggerFactory.getLogger(OrganizationService.class);

  private OrganizationDao organizationDao;
  private DatasetDao datasetDao;
  private ZohoClient restClient;
  private MetisSearchService searchService;

  @Autowired
  public OrganizationService(OrganizationDao organizationDao,
      DatasetDao datasetDao, ZohoClient restClient,
      MetisSearchService searchService) {
    this.organizationDao = organizationDao;
    this.datasetDao = datasetDao;
    this.restClient = restClient;
    this.searchService = searchService;
  }

  public void createOrganization(Organization org) throws IOException, SolrServerException {
    organizationDao.create(org);
    saveInSolr(org);
  }

  /**
   * Saves organization search labels in solr
   */
  private void saveInSolr(Organization org) throws IOException, SolrServerException {
    String id = org.getId().toString();
    String englabel = org.getName();
    String organizationId = org.getOrganizationId();
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
    searchService.addOrganizationForSearch(id, organizationId, englabel, searchLabel);
    LOGGER.info("Organization " + org.getOrganizationId() + " saved in solr");
  }

  private void updateInSolr(Organization org) throws IOException, SolrServerException {
    String id = searchService
        .findSolrIdByOrganizationId(org.getOrganizationId());
    String englabel = org.getName();
    String organizationId = org.getOrganizationId();
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
    searchService.addOrganizationForSearch(id, organizationId, englabel, searchLabel);
    LOGGER.info("Organization " + org.getOrganizationId() + " saved in solr");
  }

  public void updateOrganization(Organization org) throws SolrServerException, IOException {
    organizationDao.update(org);
    updateInSolr(org);
  }

  public void updateOrganizationDatasetNamesList(String organizationId, String datasetName) {
    organizationDao.updateOrganizationDatasetNamesList(organizationId, datasetName);
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

  public List<Organization> getAllOrganizations(String nextPage)
      throws NoOrganizationFoundException {
    List<Organization> organizations = organizationDao.getAllOrganizations(nextPage);
    if ((organizations == null || organizations.size() == 0) && StringUtils.isNotEmpty(nextPage)) {
      return organizations;
    } else if (organizations == null || organizations.size() == 0) {
      throw new NoOrganizationFoundException("No organizations found!");
    }
    return organizations;
  }

  public List<Organization> getAllOrganizationsByOrganizationRole(
      List<OrganizationRole> organizationRoles, String nextPage)
      throws NoOrganizationFoundException {
    List<Organization> organizations = organizationDao
        .getAllOrganizationsByOrganizationRole(organizationRoles, nextPage);
    if (organizations == null || organizations.size() == 0) {
      throw new NoOrganizationFoundException("No organizations found!");
    }
    return organizations;
  }

  public List<Organization> getAllOrganizationsByCountry(Country country, String nextPage)
      throws NoOrganizationFoundException {
    List<Organization> organizations = organizationDao
        .getAllOrganizationsByCountry(country, nextPage);
    if (organizations == null || organizations.size() == 0) {
      throw new NoOrganizationFoundException("No organizations found!");
    }
    return organizations;
  }

  public List<Dataset> getAllDatasetsByOrganizationId(String organizationId, String nextPage)
      throws NoOrganizationFoundException {
    getOrganizationByOrganizationId(organizationId);
    return datasetDao.getAllDatasetsByOrganizationId(organizationId, nextPage);
  }

  /**
   * Get an organization by id
   *
   * @param id The id to search for
   * @return The organization with the requested id
   */
  public Organization getOrganizationById(String id) throws NoOrganizationFoundException {
    Organization organization = organizationDao.getById(id);
    if (organization == null) {
      throw new NoOrganizationFoundException("No organization found with id: " + id + " in METIS");
    }
    return organization;
  }

  public Organization getOrganizationByOrganizationId(String organizationId)
      throws NoOrganizationFoundException {

    Organization organization = organizationDao.getByOrganizationId(organizationId);
    if (organization == null) {
      throw new NoOrganizationFoundException(
          "No organization found with organization id: " + organizationId + " in METIS");
    }
    return organization;
  }

  public Organization getOrganizationByIdFromCRM(String organizationId)
      throws IOException, ParseException, NoOrganizationFoundException {
    Organization organization = restClient.getOrganizationById(organizationId);
    if (organization == null) {
      throw new NoOrganizationFoundException(
          "No organization found with organization id: " + organizationId + " in CRM");
    }
    return organization;
  }

  public List<Organization> getAllOrganizationsFromCRM()
      throws ParseException, IOException, NoOrganizationFoundException {
    List<Organization> organizations = restClient.getAllOrganizations();
    if (organizations == null || organizations.size() == 0) {
      throw new NoOrganizationFoundException("No organization found in CRM");
    }
    return organizations;
  }

  public boolean isOptedInIIIF(String organizationId) throws NoOrganizationFoundException {
    Organization organization = organizationDao
        .getOrganizationOptInIIIFByOrganizationId(organizationId);
    if (organization == null) {
      throw new NoOrganizationFoundException(
          "No organization found with organization id: " + organizationId + " in METIS");
    }
    return organization.isOptInIIIF();
  }

  public List<OrganizationSearchBean> suggestOrganizations(String searchTerm)
      throws IOException, SolrServerException {
    return searchService.getSuggestions(searchTerm);
  }

  public void checkRestrictionsOnCreate(Organization organization)
      throws BadContentException, OrganizationAlreadyExistsException {
    try {
      Organization storedOrganization = getOrganizationByOrganizationId(
          organization.getOrganizationId());
      if (storedOrganization != null) {
        throw new OrganizationAlreadyExistsException(organization.getOrganizationId());
      }
    } catch (NoOrganizationFoundException e) {
      LOGGER.info("Organization not found, so it can be created");
    }
    if (StringUtils.isEmpty(organization.getOrganizationId())) {
      throw new BadContentException("OrganizationId cannot be null");
    } else if (organization.getDatasetNames() != null
        && organization.getDatasetNames().size() != 0) {
      throw new BadContentException("The field 'datasetNames' is not allowed on creation");
    }
  }

  public void checkRestrictionsOnUpdate(Organization organization, String organizationId)
      throws BadContentException, NoOrganizationFoundException {
    if (!StringUtils.isEmpty(organization.getOrganizationId()) && !organization
        .getOrganizationId().equals(organizationId)) {
      throw new BadContentException(
          "OrganinazationId in body " + organization.getOrganizationId()
              + " is different from parameter " + organizationId);
    } else if (organization.getDatasetNames() != null
        && organization.getDatasetNames().size() != 0) {
      throw new BadContentException("The field 'datasetNames' is not allowed on update");
    }
    organization.setOrganizationId(organizationId);

    //Check if it exist and if not throws exception
    getOrganizationByOrganizationId(organization.getOrganizationId());
  }

  public int getOrganizationsPerRequestLimit() {
    return organizationDao.getOrganizationsPerRequest();
  }
}
