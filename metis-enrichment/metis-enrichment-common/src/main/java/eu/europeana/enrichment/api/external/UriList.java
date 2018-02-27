/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved by the
 * European Commission; You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at: http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, without warranties or conditions of any kind, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
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
