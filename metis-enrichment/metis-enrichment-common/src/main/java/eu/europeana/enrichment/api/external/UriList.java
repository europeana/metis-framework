package eu.europeana.enrichment.api.external;

import java.util.ArrayList;
import java.util.Collections;
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
    return uris == null ? null : Collections.unmodifiableList(uris);
  }

  public void setUris(List<String> uris) {
    this.uris = uris == null ? null : new ArrayList<>(uris);
  }
}
