package eu.europeana.enrichment.api.external;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

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
