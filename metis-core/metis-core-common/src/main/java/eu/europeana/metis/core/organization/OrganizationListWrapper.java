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

package eu.europeana.metis.core.organization;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

/**
 * An organization List wrapper
 * Created by ymamakis on 3/2/16.
 */
public class OrganizationListWrapper {

  @JacksonXmlElementWrapper(localName = "Organizations")
  @JacksonXmlProperty(localName = "Organization")
  private List<Organization> organizations;
  private String nextPage;
  private int listSize;

  public void setOrganizationsAndLastPage(List<Organization> organizations,
      int organizationPerRequestLimit) {
    if (organizations != null && organizations.size() != 0) {
      if (organizations.size() < organizationPerRequestLimit) {
        nextPage = null;
      } else {
        nextPage = organizations.get(organizations.size() - 1).getId().toString();
      }
      listSize = organizations.size();
    } else {
      nextPage = null;
    }
    this.organizations = organizations;
  }

  public List<Organization> getOrganizations() {
    return organizations;
  }

  public void setOrganizations(List<Organization> organizations) {
    this.organizations = organizations;
    if (organizations != null)
      this.listSize = organizations.size();
  }

  public String getNextPage() {
    return nextPage;
  }

  public void setNextPage(String nextPage) {
    this.nextPage = nextPage;
  }

  public int getListSize() {
    return listSize;
  }

  public void setListSize(int listSize) {
    this.listSize = listSize;
  }
}
