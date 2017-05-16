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
package eu.europeana.metis.core.dao;

import com.mongodb.WriteResult;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.OrganizationRole;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.organization.Organization;
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
  private int organizationsPerRequest = 5;

  @Autowired
  private MorphiaDatastoreProvider provider;

  public OrganizationDao(int organizationsPerRequest) {
    this.organizationsPerRequest = organizationsPerRequest;
  }

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
    ops.set("name", organization.getName());
    if (organization.getOrganizationRoles() != null) {
      ops.set("organizationRoles", organization.getOrganizationRoles());
    } else {
      ops.unset("organizationRoles");
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
    ops.set("modified", new Date());
    provider.getDatastore().update(q, ops);
    UpdateResults updateResults = provider.getDatastore().update(q, ops);
    LOGGER.info("Organization '" + organization.getOrganizationId() + "' updated in Mongo");
    return String.valueOf(updateResults.getUpdatedCount());
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

  public boolean deleteByOrganizationId(String organizationId) {
    Query<Organization> query = provider.getDatastore().createQuery(Organization.class);
    query.filter("organizationId", organizationId);
    WriteResult delete = provider.getDatastore().delete(query);
    LOGGER.info("Organization '" + organizationId + "' deleted from Mongo");
    return delete.getN() == 1;
  }

  public List<Organization> getAllOrganizations(String nextPage) {
    Query<Organization> query = provider.getDatastore().createQuery(Organization.class);
    query.order("_id").limit(organizationsPerRequest);
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList();
  }

  public List<Organization> getAllOrganizationsByCountry(Country country, String nextPage) {
    Query<Organization> query = provider.getDatastore().createQuery(Organization.class);
    query.filter("country", country).order("_id").limit(organizationsPerRequest);
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList();
  }


  public List<Organization> getAllOrganizationsByOrganizationRole(List<OrganizationRole> organizationRoles, String nextPage) {
    Query<Organization> query = provider.getDatastore().createQuery(Organization.class);
    query.field("organizationRoles")
        .hasAnyOf(organizationRoles).order("_id").limit(organizationsPerRequest);
    if (StringUtils.isNotEmpty(nextPage)) {
      query.field("_id").greaterThan(new ObjectId(nextPage));
    }
    return query.asList();
  }

  public Organization getByOrganizationId(String organizationId) {
    return provider.getDatastore().find(Organization.class).filter("organizationId", organizationId)
        .get();
  }

  public Organization getOrganizationOptInIIIFByOrganizationId(String organizationId)
  {
    Query<Organization> query = provider.getDatastore().createQuery(Organization.class);
    query.field("organizationId").equal(organizationId).project("optInIIIF", true);
    return query.get();
  }

  public int getOrganizationsPerRequest() {
    return organizationsPerRequest;
  }

  public void setOrganizationsPerRequest(int organizationsPerRequest) {
    this.organizationsPerRequest = organizationsPerRequest;
  }

  public MorphiaDatastoreProvider getProvider() {
    return provider;
  }

  public void setProvider(MorphiaDatastoreProvider provider) {
    this.provider = provider;
  }
}
