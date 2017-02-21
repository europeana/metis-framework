package eu.europeana.metis.search.service;

import eu.europeana.metis.search.common.OrganizationSearchBean;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * Search service for Metis
 * Created by gmamakis on 21-2-17.
 */
public class MetisSearchService {

    @Autowired
    private SolrClient solrClient;

    /**
     * Get suggestion for a given organization based on its search term
     * @param searchTerm The search term
     * @return The suggestions
     * @throws IOException
     * @throws SolrServerException
     */
    public List<OrganizationSearchBean> getSuggestions(String searchTerm) throws IOException, SolrServerException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("searchlabel:*"+searchTerm+"*");
        solrQuery.setFields("id","endlabel");
        QueryResponse resp = solrClient.query(solrQuery);
        return resp.getBeans(OrganizationSearchBean.class);
    }

    /**
     * Persist an organization for searching
     * @param id The organization id - required for retrieval
     * @param engLabel The english label
     * @param searchLabels The search labels - including the english label
     * @throws IOException
     * @throws SolrServerException
     */
    public void addOrganizationForSearch(String id, String engLabel, List<String> searchLabels) throws IOException, SolrServerException {
        OrganizationSearchBean searchBean = new OrganizationSearchBean();
        searchBean.setEngLabel(engLabel);
        searchBean.setOrganizationId(id);
        searchBean.setSearchLabels(searchLabels);
        solrClient.addBean(searchBean,50);
    }

    public void deleteFromSearch(String id) throws IOException, SolrServerException {
        solrClient.deleteByQuery("id:"+id);
    }
}
