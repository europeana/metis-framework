package eu.europeana.metis.core.rest.client;

import eu.europeana.metis.core.search.common.OrganizationSearchBean;

import java.util.List;

/**
 * Created by gmamakis on 28-3-17.
 */
public class Suggestions {
    List<OrganizationSearchBean> suggestions;

    public List<OrganizationSearchBean> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<OrganizationSearchBean> suggestions) {
        this.suggestions = suggestions;
    }
}
