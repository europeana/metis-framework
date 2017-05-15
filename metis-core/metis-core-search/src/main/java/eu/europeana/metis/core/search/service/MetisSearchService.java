package eu.europeana.metis.core.search.service;

import eu.europeana.metis.core.search.common.OrganizationSearchBean;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
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
        solrQuery.setQuery("searchlabel:*"+ ClientUtils.escapeQueryChars(searchTerm)+"*");
        solrQuery.setFields("id", "organization_id", "englabel");
        solrQuery.setStart(0).setRows(10);
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
    public void addOrganizationForSearch(String id, String organizationId, String engLabel, List<String> searchLabels) throws IOException, SolrServerException {
        OrganizationSearchBean searchBean = new OrganizationSearchBean();
        searchBean.setId(id);
        searchBean.setOrganizationId(organizationId);
        searchBean.setEngLabel(engLabel);
        searchBean.setSearchLabels(searchLabels);
        solrClient.addBean(searchBean);
        solrClient.commit();
    }

    public String findSolrIdByOrganizationId(String organizationId)
        throws IOException, SolrServerException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("organization_id:"+ ClientUtils.escapeQueryChars(organizationId));
        solrQuery.setFields("id");
        QueryResponse resp = solrClient.query(solrQuery);
        return resp.getBeans(OrganizationSearchBean.class).get(0).getId();
    }

    public void deleteFromSearch(String id) throws IOException, SolrServerException {
        solrClient.deleteByQuery("id:"+id);
        solrClient.commit();
    }

    public void deleteFromSearchByOrganizationId(String organizationId)
        throws IOException, SolrServerException {
        solrClient.deleteByQuery("organization_id:"+organizationId);
        solrClient.commit();
    }
}
