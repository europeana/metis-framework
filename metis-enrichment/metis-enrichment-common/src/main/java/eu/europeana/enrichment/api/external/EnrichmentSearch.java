package eu.europeana.enrichment.api.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper class for SearchValue class.
 * <p>Required for proper (un)marshalling xml</p>
 *
 * @author Joana Sousa joana.sousa@europeana.eu
 *
 */
@JsonSerialize
@JsonRootName(value = EnrichmentSearch.API_NAME)
@XmlRootElement(name = EnrichmentSearch.API_NAME)
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(value = EnrichmentSearch.API_NAME)
public class EnrichmentSearch {

    static final String API_NAME = "searchTerms";

    @XmlElement(name = "searchValue")
    @JsonProperty("searchValue")
    private List<SearchValue> searchValues;

    public EnrichmentSearch() {
      // Required for XML mapping.
    }

    public List<SearchValue> getSearchValues() {
      return searchValues == null ? null : Collections.unmodifiableList(searchValues);
    }

    public void setSearchValues(List<SearchValue> searchValues) {
      this.searchValues = searchValues == null ? null : new ArrayList<>(searchValues);
    }

}
