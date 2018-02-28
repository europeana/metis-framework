package eu.europeana.enrichment.api.internal;

public enum ContextualCategory {

  AGENT("agent", "AgentImpl"), 
  TIMESPAN("timespan", "TimespanImpl"), 
  PLACE("place", "PlaceImpl"),
  CONCEPT("concept", "ConceptImpl");

  private String label;

  private String entityClass;

  private ContextualCategory(String label, String entityClass) {
    this.label = label;
    this.entityClass = entityClass;
  }

  public String getLabel() {
    return label;
  }

  public String getEntityClass() {
    return entityClass;
  }

}
