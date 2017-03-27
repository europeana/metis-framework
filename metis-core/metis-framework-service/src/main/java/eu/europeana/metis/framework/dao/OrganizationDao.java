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
package eu.europeana.metis.framework.dao;

import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.framework.common.Role;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.exceptions.NoOrganizationExceptionFound;
import eu.europeana.metis.framework.mongo.MongoProvider;
import eu.europeana.metis.framework.organization.Organization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Organization DAO
 * Created by ymamakis on 2/17/16.
 */
public class OrganizationDao implements MetisDao<Organization, String> {

  private final Logger LOGGER = LoggerFactory.getLogger(OrganizationDao.class);

  @Autowired
  private MongoProvider provider;

  @Override
  public String create(Organization organization) {
    Key<Organization> organizationKey = provider.getDatastore().save(organization);
    LOGGER.info("Organization '" + organization.getOrganizationId() + "' created in Mongo");
    return organizationKey.getId().toString();
  }

  @Override
  public String update(Organization organization) {
    Query<Organization> q = provider.getDatastore().find(Organization.class)
        .filter("organizationId", organization.getOrganizationId());
    UpdateOperations<Organization> ops = provider.getDatastore()
        .createUpdateOperations(Organization.class);
    if (organization.getHarvestingMetadata() != null) {
      ops.set("harvestingMetadata", organization.getHarvestingMetadata());
    } else {
      ops.unset("harvestingMetadata");
    }
    if (organization.getOrganizationUri() != null) {
      ops.set("organizationUri", organization.getOrganizationUri());
    } else {
      ops.unset("organizationUri");
    }
    if (organization.getDatasets() != null || organization.getDatasets().size() != 0) {
      ops.set("datasets", organization.getDatasets());
    } else {
      ops.unset("datasets");
    }
    ops.set("name", organization.getName());
    if (organization.getRoles() != null) {
      ops.set("roles", organization.getRoles());
    } else {
      ops.unset("roles");
    }
    if (organization.getCreatedByLdapId() != null) {
      ops.set("createdByLdapId", organization.getCreatedByLdapId());
    }

    if (organization.getUpdatedByLdapId() != null) {
      ops.set("updatedByLdapId", organization.getUpdatedByLdapId());
    } else {
      ops.unset("updatedByLdapId");
    }

    if (organization.getPrefLabel() != null) {
      ops.set("prefLabel", organization.getPrefLabel());
    } else {
      ops.unset("prefLabel");
    }

    if (organization.getAltLabel() != null) {
      ops.set("altLabel", organization.getAltLabel());
    } else {
      ops.unset("altLabel");
    }

    if (organization.getSameAs() != null) {
      ops.set("sameAs", organization.getSameAs());
    } else {
      ops.unset("sameAs");
    }

    if (organization.getDescription() != null) {
      ops.set("description", organization.getDescription());
    } else {
      ops.unset("description");
    }

    if (organization.getLogoLocation() != null) {
      ops.set("logoLocation", organization.getLogoLocation());
    } else {
      ops.unset("logoLocation");
    }
    if (organization.getDomain() != null) {
      ops.set("domain", organization.getDomain());
    } else {
      ops.unset("domain");
    }
    if (organization.getSector() != null) {
      ops.set("sector", organization.getSector());
    } else {
      ops.unset("sector");
    }
    if (organization.getGeographicLevel() != null) {
      ops.set("geographicLevel", organization.getGeographicLevel());
    } else {
      ops.unset("geographicLevel");
    }
    if (organization.getWebsite() != null) {
      ops.set("website", organization.getWebsite());
    } else {
      ops.unset("website");
    }
    if (organization.getCountry() != null) {
      ops.set("country", organization.getCountry());
    } else {
      ops.unset("country");
    }
    if (organization.getLanguage() != null) {
      ops.set("language", organization.getLanguage());
    } else {
      ops.unset("language");
    }

    ops.set("acronym", organization.getAcronym());
    ops.set("created", organization.getCreated());
    ops.set("modified", new Date());
    provider.getDatastore().update(q, ops);
    UpdateResults updateResults = provider.getDatastore().update(q, ops);
    LOGGER.info("Organization '" + organization.getOrganizationId() + "' updated in Mongo");
    Object newId = updateResults.getNewId();
    return newId != null ? updateResults.getNewId().toString() : organization.getId().toString();
  }

  @Override
  public Organization getById(String id) {
    return provider.getDatastore().find(Organization.class).filter("_id", new ObjectId(id)).get();
  }

  @Override
  public boolean delete(Organization organization) {
    provider.getDatastore().delete(organization);
    LOGGER.info("Organization '" + organization.getName() + "' deleted from Mongo");
    return true;
  }

  /**
   * Retrieve all the organizations
   *
   * @return A list of all the organizations
   */
  public List<Organization> getAll() {
    return provider.getDatastore().find(Organization.class).asList();
  }

  /**
   * Retrieve all the organizations
   *
   * @return A list of all the organizations
   */
  public List<Organization> getAllByCountry(Country country) {
    return provider.getDatastore().find(Organization.class).filter("country", country).asList();
  }


  public List<Organization> getAllProviders(Role... roles) {

    return provider.getDatastore().find(Organization.class).field("roles")
        .hasAnyOf(Arrays.asList(roles)).asList();
  }

  /**
   * Get an organization by its organization Id
   *
   * @param organizationId The organization id
   * @return The organization
   */
  public Organization getByOrganizationId(String organizationId) {
    return provider.getDatastore().find(Organization.class).filter("organizationId", organizationId)
        .get();
  }

  /**
   * Get all the datasets for an organization
   *
   * @param organizationId The id to retrieve the datasets for
   * @return The datasets for this organization
   */
  public List<Dataset> getAllDatasetsByOrganization(String organizationId)
      throws NoOrganizationExceptionFound {
    Organization org = getByOrganizationId(organizationId);
    if (org != null) {
      return org.getDatasets();
    }
    throw new NoOrganizationExceptionFound("No organization found with id: " + organizationId);
  }

  /**
   * Get all the organizations referred to by a dataset
   * @param datasetId The datasetId to search the organizations for
   * @param dataProviderId The dataprovider id for the dataset <code>{@link Dataset#dataProvider}</code>
   * @return The full list of organizations for this dataset
   */
  public List<Organization> getAllOrganizationsFromDataset(String datasetId, String dataProviderId){
    List<Organization> orgs = provider.getDatastore().find(Organization.class).filter("datasets.$id",datasetId).asList();
    if(orgs==null){
      orgs = new ArrayList<>();
    }
    if(StringUtils.isNotEmpty(dataProviderId)) {
      orgs.add(getById(dataProviderId));
    }
    return orgs;
  }
}
