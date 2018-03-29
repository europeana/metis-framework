package eu.europeana.enrichment.api.internal;

import java.util.Date;

/**
 * Technical Interface used for specifying non-functional requirements  
 * @author GordeaS
 *
 */
public interface DatedObject {

  void setCreated(Date created);

  Date getCreated();

  void setModified(Date modified);

  Date getModified();

}
