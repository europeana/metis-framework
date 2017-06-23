package eu.europeana.metis.core.rest.client;

import eu.europeana.metis.core.dataset.Dataset;
import java.util.List;

/**
 * Created by gmamakis on 10-2-17.
 */
public class DatasetListResponse {

    private List<Dataset> results;
    private int listSize;
    private String nextPage;

    public String getNextPage() {
        return nextPage;
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

    public List<Dataset> getResults() {
        return results;
    }

    public void setResults(List<Dataset> results) {
        this.results = results;
    }

    public int getListSize() { return listSize; }

    public void setListSize(int listSize) {
        this.listSize = listSize;
    }
}
