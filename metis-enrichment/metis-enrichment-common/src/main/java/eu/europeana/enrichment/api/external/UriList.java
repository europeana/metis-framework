package eu.europeana.enrichment.api.external;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * @author ymamakis
 */
@JsonSerialize
@XmlRootElement
public class UriList {
    private List<String> uris;

    public List<String> getUris() {
        return uris;
    }

    public void setUris(List<String> uris) {
        this.uris = uris;
    }
    
    
}
