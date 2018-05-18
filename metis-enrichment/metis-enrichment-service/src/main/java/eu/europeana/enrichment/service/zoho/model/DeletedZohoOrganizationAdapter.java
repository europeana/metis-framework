package eu.europeana.enrichment.service.zoho.model;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europeana.enrichment.service.exception.ZohoAccessException;
import eu.europeana.metis.authentication.dao.ZohoApi2Fields;
import eu.europeana.metis.authentication.dao.ZohoApiFields;

/**
 * This class provides interface to deleted Zoho orgnization object.
 * 
 * @author GrafR
 *
 */
public class DeletedZohoOrganizationAdapter{

  private final String id;
  private final String name;

  public DeletedZohoOrganizationAdapter(JsonNode response) {
    id = response.path(ZohoApiFields.ID).asText();
    name = response.path(ZohoApi2Fields.DISPLAY_NAME).asText();
  }

  @Override
  public String toString() {
    return getZohoId() + ", " + getOrganizationName();
  }

  public String getZohoId() {
    return id;
  }

  public String getOrganizationName() {
    return name;
  }
}
