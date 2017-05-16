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

package eu.europeana.metis.core.dataset;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

/**
 * List of Datasets wrapper
 * Created by ymamakis on 3/2/16.
 */
public class DatasetListWrapper {

    @JacksonXmlElementWrapper(localName = "Datasets")
    @JacksonXmlProperty(localName = "Dataset")
    private List<Dataset> datasets;
    private String nextPage;
    private int listSize;

    public void setDatasetsAndLastPage(List<Dataset> datasets,
        int datasetsPerRequestLimit) {
        if (datasets != null && datasets.size() != 0) {
            if (datasets.size() < datasetsPerRequestLimit) {
                nextPage = null;
            } else {
                nextPage = datasets.get(datasets.size() - 1).getId().toString();
            }
            listSize = datasets.size();
        } else {
            nextPage = null;
        }
        this.datasets = datasets;
    }

    public List<Dataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<Dataset> datasets) {
        this.datasets = datasets;
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
