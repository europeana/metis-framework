package eu.europeana.metis.core.search.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-15
 */
@JacksonXmlRootElement(localName = "Organizations")
public class OrganizationSearchListWrapper {
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "Organization")
  @JsonProperty("Organizations")
  private List<OrganizationSearchBean> organizationSearchBeanList = null;

  public OrganizationSearchListWrapper(){}

  public OrganizationSearchListWrapper(List<OrganizationSearchBean> organizationSearchBeanList) {
    this.organizationSearchBeanList = organizationSearchBeanList;
  }

  public List<OrganizationSearchBean> getOrganizationSearchBeanList() {
    return organizationSearchBeanList;
  }

  public void setOrganizationSearchBeanList(
      List<OrganizationSearchBean> organizationSearchBeanList) {
    this.organizationSearchBeanList = organizationSearchBeanList;
  }
}
