package eu.europeana.metis.search.common;

import org.apache.solr.client.solrj.beans.Field;

import java.util.List;

/**
 * Created by gmamakis on 21-2-17.
 */
public class OrganizationSearchBean {
    @Field("id")
    private String organizationId;

    @Field("englabel")
    private String engLabel;

    @Field("searchlabel")
    private List<String> searchLabels;

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getEngLabel() {
        return engLabel;
    }

    public void setEngLabel(String engLabel) {
        this.engLabel = engLabel;
    }

    public List<String> getSearchLabels() {
        return searchLabels;
    }

    public void setSearchLabels(List<String> searchLabels) {
        this.searchLabels = searchLabels;
    }
}
