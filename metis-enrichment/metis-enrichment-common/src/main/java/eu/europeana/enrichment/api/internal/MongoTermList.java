package eu.europeana.enrichment.api.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Id;
import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;
import java.util.Date;
import org.bson.types.ObjectId;

/**
 * Basic Class linking a number of MongoTerms. This class enables searching by CodeUri for fetching
 * all the relevant MongoTerms while it includes the parent term (skos:broader, dcterms:isPartOf),
 * the className of the entityType for deserialization and a JSON representation of the contextual
 * class
 *
 * @param <T> AgentImpl, PlaceImpl, ConceptImpl, TimespanImpl, OrganizationImpl
 * @author Yorgos.Mamakis@ europeana.eu
 */
public abstract class MongoTermList<T extends AbstractEdmEntityImpl> implements DatedObject {

  private String parent;
  private String codeUri;
  private String[] owlSameAs;

  private Date created;
  private Date modified;

  @Id
  @JsonProperty("_id")
  private ObjectId id;

  protected T representation;
  private String entityType;

  public MongoTermList() {
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getCodeUri() {
    return codeUri;
  }

  public void setCodeUri(String codeUri) {
    this.codeUri = codeUri;
  }

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public abstract T getRepresentation();

  public abstract void setRepresentation(T representation);

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public void setOwlSameAs(String[] owlSameAs) {
    this.owlSameAs = owlSameAs;
  }

  public String[] getOwlSameAs() {
    return owlSameAs;
  }

  @SuppressWarnings("unchecked")
  public static <T extends AbstractEdmEntityImpl, S extends T> MongoTermList<T> cast(
      MongoTermList<S> source) {
    return (MongoTermList<T>) source;
  }

  @Override
  public Date getModified() {
    return modified;
  }

  @Override
  public void setModified(Date modified) {
    this.modified = modified;
  }

  @Override
  public Date getCreated() {
    return created;
  }

  @Override
  public void setCreated(Date created) {
    this.created = created;
  }
}
