package eu.europeana.metis.core.rest.client;

import eu.europeana.metis.core.organization.Organization;

import java.util.List;

/**
 * Created by gmamakis on 10-2-17.
 */
public class OrganizationListResponse {

    private String resultCount;
    private List<Organization> results;

    public String getResultCount() {
        return resultCount;
    }

    public void setResultCount(String resultCount) {
        this.resultCount = resultCount;
    }

    public List<Organization> getResults() {
        return results;
    }

    public void setResults(List<Organization> results) {
        this.results = results;
    }
}
