package eu.europeana.enrichment.api.internal;

import org.mongojack.Id;

public class MongoSequence {

  @Id
  private String id;
  private Long nextConceptSequence = 1L;
  private Long nextAgentSequence = 1L;
  private Long nextPlaceSequence = 1L;
  private Long nextTimespanSequence = 1L;

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

    Long nextInSEquence;
    switch (contextualCategory) {
      case AGENT:
        nextInSEquence = nextAgentSequence;
        break;
      case CONCEPT:
        nextInSEquence = nextConceptSequence;
        break;
      case TIMESPAN:
        nextInSEquence = nextTimespanSequence;
        break;
      case PLACE:
        nextInSEquence = nextPlaceSequence;
        break;
      default:
        throw new IllegalArgumentException("Not supported contextual entity");
    }
    return nextInSEquence;
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
