package eu.europeana.enrichment.api.external.model;

import static eu.europeana.enrichment.utils.EntityValuesConverter.convertListToLabel;
import static eu.europeana.enrichment.utils.EntityValuesConverter.convertListToResource;
import static eu.europeana.enrichment.utils.EntityValuesConverter.convertMultilingualMapToLabel;

import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Model class of a Concept(for example "painting")
 */
@XmlRootElement(namespace = "http://www.w3.org/2004/02/skos/core#", name = "Concept")
@XmlAccessorType(XmlAccessType.FIELD)
public class Concept extends EnrichmentBase {

  @XmlElement(name = "hiddenLabel", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Label> hiddenLabel;
  @XmlElement(name = "notation", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Label> notation;
  @XmlElement(name = "broader", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> broader;
  @XmlElement(name = "broadMatch", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> broadMatch;
  @XmlElement(name = "closeMatch", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> closeMatch;
  /** Supported for serving in dereference API, not in the wider aggregation workflow. **/
  @XmlElement(name = "definition", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Label> definitions;
  @XmlElement(name = "exactMatch", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> exactMatch;
  @XmlElement(name = "inScheme", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> inScheme;
  @XmlElement(name = "narrower", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> narrower;
  @XmlElement(name = "narrowMatch", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> narrowMatch;
  @XmlElement(name = "related", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> related;
  @XmlElement(name = "relatedMatch", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> relatedMatch;
  /** Supported for serving in dereference API, not in the wider aggregation workflow. **/
  @XmlElement(name = "scopeNote", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Label> scopeNotes;

  /**
   * Default constructor.
   */
  public Concept() {
    super();
    // Required for XML binding.
  }

  // Used for creating XML entity from EM model class
  public Concept(eu.europeana.entitymanagement.definitions.model.Concept concept) {
    super(concept);
    init(concept);
  }

  public List<Label> getHiddenLabel() {
    return unmodifiableListAcceptingNull(hiddenLabel);
  }

  public void setHiddenLabel(List<Label> hiddenLabel) {
    this.hiddenLabel = cloneListAcceptingNull(hiddenLabel);
  }

  public List<Label> getNotation() {
    return unmodifiableListAcceptingNull(notation);
  }

  public void setNotation(List<Label> notation) {
    this.notation = cloneListAcceptingNull(notation);
  }

  public List<Resource> getBroader() {
    return unmodifiableListAcceptingNull(broader);
  }

  public void setBroader(List<Resource> broader) {
    this.broader = cloneListAcceptingNull(broader);
  }

  public List<Resource> getBroadMatch() {
    return unmodifiableListAcceptingNull(broadMatch);
  }

  public void setBroadMatch(List<Resource> broader) {
    this.broadMatch = cloneListAcceptingNull(broader);
  }

  public List<Resource> getCloseMatch() {
    return unmodifiableListAcceptingNull(closeMatch);
  }

  public void setCloseMatch(List<Resource> closeMatch) {
    this.closeMatch = cloneListAcceptingNull(closeMatch);
  }

  public List<Label> getDefinitions() {
    return unmodifiableListAcceptingNull(definitions);
  }

  public void setDefinitions(List<Label> definitions) {
    this.definitions = cloneListAcceptingNull(definitions);
  }

  public List<Resource> getExactMatch() {
    return unmodifiableListAcceptingNull(exactMatch);
  }

  public void setExactMatch(List<Resource> exactMatch) {
    this.exactMatch = cloneListAcceptingNull(exactMatch);
  }

  public List<Resource> getInScheme() {
    return unmodifiableListAcceptingNull(inScheme);
  }

  public void setInScheme(List<Resource> inScheme) {
    this.inScheme = cloneListAcceptingNull(inScheme);
  }

  public List<Resource> getNarrower() {
    return unmodifiableListAcceptingNull(narrower);
  }

  public void setNarrower(List<Resource> narrower) {
    this.narrower = cloneListAcceptingNull(narrower);
  }

  public List<Resource> getNarrowMatch() {
    return unmodifiableListAcceptingNull(narrowMatch);
  }

  public void setNarrowMatch(List<Resource> narrowMatch) {
    this.narrowMatch = cloneListAcceptingNull(narrowMatch);
  }

  public List<Resource> getRelated() {
    return unmodifiableListAcceptingNull(related);
  }

  public void setRelated(List<Resource> related) {
    this.related = cloneListAcceptingNull(related);
  }

  public List<Resource> getRelatedMatch() {
    return unmodifiableListAcceptingNull(relatedMatch);
  }

  public void setRelatedMatch(List<Resource> relatedMatch) {
    this.relatedMatch = cloneListAcceptingNull(relatedMatch);
  }

  public List<Label> getScopeNotes() {
    return unmodifiableListAcceptingNull(scopeNotes);
  }

  public void setScopeNotes(List<Label> scopeNotes) {
    this.scopeNotes = cloneListAcceptingNull(scopeNotes);
  }

  private void init(eu.europeana.entitymanagement.definitions.model.Concept concept) {
    this.hiddenLabel = convertListToLabel(concept.getHiddenLabel());
    this.notation = convertMultilingualMapToLabel(concept.getNotation());
    this.broader = convertListToResource(concept.getBroader());
    this.broadMatch = convertListToResource(concept.getBroadMatch());
    this.closeMatch = convertListToResource(concept.getCloseMatch());
    this.exactMatch = convertListToResource(concept.getSameReferenceLinks());
    this.inScheme = convertListToResource(concept.getInScheme());
    this.narrower = convertListToResource(concept.getNarrower());
    this.narrowMatch = convertListToResource(concept.getNarrowMatch());
    this.related = convertListToResource(concept.getRelated());
    this.relatedMatch = convertListToResource(concept.getRelatedMatch());
  }
}
