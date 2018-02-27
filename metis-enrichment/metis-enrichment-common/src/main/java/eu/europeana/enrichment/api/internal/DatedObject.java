package eu.europeana.enrichment.api.internal;

import java.util.Date;

public interface DatedObject {

	void setCreated(Date created);

	Date getCreated();

	void setModified(Date modified);

	Date getModified();

}
