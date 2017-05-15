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
import eu.europeana.metis.core.dao.OrganizationDao;
import eu.europeana.metis.core.dao.ZohoClient;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
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

  @Autowired
  private OrganizationDao orgDao;

  @Autowired
  private ZohoClient restClient;

  @Autowired
  private MetisSearchService searchService;

  public void createOrganization(Organization org) throws IOException, SolrServerException {
    orgDao.create(org);
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
    orgDao.update(org);
    updateInSolr(org);
  }

  public void deleteOrganization(Organization org) throws IOException, SolrServerException {
    orgDao.delete(org);
    searchService.deleteFromSearch(org.getId().toString());
  }

  public void deleteOrganizationByOrganizationId(String organizationId)
      throws IOException, SolrServerException {
    orgDao.deleteByOrganizationId(organizationId);
    searchService.deleteFromSearchByOrganizationId(organizationId);
  }

  public List<Organization> getAllOrganizations(String nextPage)
      throws NoOrganizationFoundException {
    List<Organization> organizations = orgDao.getAll(nextPage);
    if ((organizations == null || organizations.size() == 0) && StringUtils.isNotEmpty(nextPage)) {
      return organizations;
    } else if (organizations == null || organizations.size() == 0) {
      throw new NoOrganizationFoundException("No organizations found!");
    }
    return organizations;
  }

  public List<Organization> getAllProviders(OrganizationRole... organizationRoles) {
    return orgDao.getAllProviders(organizationRoles);
  }

  public List<Organization> getAllOrganizationsByCountry(Country country, String nextPage)
      throws NoOrganizationFoundException {
    List<Organization> organizations = orgDao.getAllOrganizationsByCountry(country, nextPage);
    if (organizations == null || organizations.size() == 0) {
      throw new NoOrganizationFoundException("No organizations found!");
    }
    return organizations;
  }

  /**
   * Get all the datasets of an organization
   *
   * @param orgId The organization id to search on
   * @return The datasets for that organization
   */
  public List<Dataset> getDatasetsByOrganization(String orgId) throws NoOrganizationFoundException {
    return orgDao.getAllDatasetsByOrganization(orgId);
  }


  /**
   * Get an organization by id
   *
   * @param id The id to search for
   * @return The organization with the requested id
   */
  public Organization getOrganizationById(String id) throws NoOrganizationFoundException {
    Organization organization = orgDao.getById(id);
    if (organization == null) {
      throw new NoOrganizationFoundException("No organization found with id: " + id + " in METIS");
    }
    return organization;
  }

  public Organization getOrganizationByOrganizationId(String id)
      throws NoOrganizationFoundException {

    Organization organization = orgDao.getByOrganizationId(id);
    if (organization == null) {
      throw new NoOrganizationFoundException(
          "No organization found with organization id: " + id + " in METIS");
    }
    return organization;
  }

  /**
   * Get an organization from CRM
   *
   * @param id The organization to retrieve from CRM
   * @return The organization as its kept in CRM
   */
  public Organization getOrganizationByIdFromCRM(String id)
      throws ParseException, IOException, NoOrganizationFoundException {
    Organization organization = restClient.getOrganizationById(id);
    if (organization == null) {
      throw new NoOrganizationFoundException(
          "No organization found with organization id: " + id + " in CRM");
    }
    return organization;
  }

  /**
   * Get all organizations from CRM
   *
   * @return GEt the list of all the organizations for CRM
   */
  public List<Organization> getAllOrganizationsFromCRM()
      throws ParseException, IOException, NoOrganizationFoundException {
    List<Organization> organizations = restClient.getAllOrganizations();
    if (organizations == null || organizations.size() == 0) {
      throw new NoOrganizationFoundException("No organization found in CRM");
    }
    return organizations;
  }

  /**
   * Check whether an organization has opted in or not
   *
   * @param organizationId The organization id to check for
   * @return true if opted in false otherwise
   */
  public boolean isOptedInForIIIF(String organizationId) {
    Organization org = orgDao.getById(organizationId);
    return org != null && org.isOptInIIIF();
  }

  /**
   * Return organizations based on a specific search term
   *
   * @param searchTerm The search term
   * @return The list of organizations that correspond to the term. For performance reasons only the
   * id and the english name are returned
   */
  public List<OrganizationSearchBean> suggestOrganizations(String searchTerm)
      throws IOException, SolrServerException {
    return searchService.getSuggestions(searchTerm);
  }

  /**
   * Get the organizations refered to by a dataset
   *
   * @param datasetId The dataset Id to search for
   * @param providerId The ddata provider for this dataset <code>{@link
   * Dataset#dataProvider}</code>
   */
  public List<Organization> getByDatasetId(String datasetId, String providerId) {
    return orgDao.getAllOrganizationsFromDataset(datasetId, providerId);
  }
}
