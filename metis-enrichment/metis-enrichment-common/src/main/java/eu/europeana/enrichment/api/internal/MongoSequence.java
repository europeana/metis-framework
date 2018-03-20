package eu.europeana.enrichment.api.internal;

import org.mongojack.Id;

public class MongoSequence {

  @Id
  private String id;

  private Long nextConceptSequence = 1l;

  private Long nextAgentSequence = 1l;

  private Long nextPlaceSequence = 1l;

  private Long nextTimespanSequence = 1l;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getNextConceptSequence() {
    return nextConceptSequence;
  }

  public void setNextConceptSequence(Long nextConceptSequence) {
    this.nextConceptSequence = nextConceptSequence;
  }

  public Long getNextAgentSequence() {
    return nextAgentSequence;
  }

  public void setNextAgentSequence(Long nextAgentSequence) {
    this.nextAgentSequence = nextAgentSequence;
  }

  public Long getNextPlaceSequence() {
    return nextPlaceSequence;
  }

  public void setNextPlaceSequence(Long nextPlaceSequence) {
    this.nextPlaceSequence = nextPlaceSequence;
  }

  public Long getNextTimespanSequence() {
    return nextTimespanSequence;
  }

  public void setNextTimespanSequence(Long nextTimespanSequence) {
    this.nextTimespanSequence = nextTimespanSequence;
  }

  public Long getNextSequence(ContextualCategory contextualCategory) {

    switch (contextualCategory) {
      case AGENT:
        return nextAgentSequence;
      case CONCEPT:
        return nextConceptSequence;
      case TIMESPAN:
        return nextTimespanSequence;
      case PLACE:
        return nextPlaceSequence;
      default:
        throw new IllegalArgumentException("Not supported contextual entity");
    }
  }

  public void setNextSequence(Long nextSequence, ContextualCategory contextualCategory) {
    switch (contextualCategory) {
      case AGENT:
        nextAgentSequence = nextSequence;
        break;
      case CONCEPT:
        nextConceptSequence = nextSequence;
        break;
      case TIMESPAN:
        nextTimespanSequence = nextSequence;
        break;
      case PLACE:
        nextPlaceSequence = nextSequence;
        break;
      default:
        throw new IllegalArgumentException("Not supported contextual entity");
    }
  }
}
