package eu.europeana.metis.core.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that encapsulates a list of {@link String} ID objects.
 */
public class ListOfIds {

  private List<String> ids;

  public List<String> getIds() {
    return ids == null ? Collections.emptyList() : new ArrayList<>(ids);
  }

  public void setIds(List<String> ids) {
    this.ids = new ArrayList<>(ids);
  }
}
