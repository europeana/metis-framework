package eu.europeana.enrichment.api.external.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import eu.europeana.corelib.solr.entity.ContextualClassImpl;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.json.ObjectIdSerializer;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * @author Simon Tzanakis
 * @since 2020-08-04
 */
@Entity
@Indexes({
    @Index(fields = {@Field("codeUri")}, options = @IndexOptions(unique = true)),
    @Index(fields = {@Field("owlSameAs")}),
    @Index(fields = {@Field("created")}),
    @Index(fields = {@Field("updated")}),
    @Index(fields = {@Field("entityType")}),
    @Index(fields = {@Field("codeUri"), @Field("entityType")}),
    @Index(fields = {@Field("owlSameAs"), @Field("entityType")}),
    @Index(fields = {@Field("labelInfos.lowerCaseLabel"), @Field("labelInfos.lang"),
        @Field("entityType")}),
    @Index(fields = {@Field("created"), @Field("entityType")}),
    @Index(fields = {@Field("updated"), @Field("entityType")})
})
public class EnrichmentTerm {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;

  private String parent;
  private String codeUri;
  private List<String> owlSameAs;
  private Date created;
  private Date updated;
  private EntityType entityType;
  @Embedded
  private ContextualClassImpl contextualEntity;
  @Embedded
  private List<LabelInfo> labelInfos;

  public EnrichmentTerm() {
  }

  public EntityType getEntityType() {
    return entityType;
  }

  public void setEntityType(EntityType entityType) {
    this.entityType = entityType;
  }

  public ContextualClassImpl getContextualEntity() {
    return contextualEntity;
  }

  public void setContextualEntity(ContextualClassImpl contextualEntity) {
    this.contextualEntity = contextualEntity;
  }

  public List<LabelInfo> getLabelInfos() {
    return labelInfos;
  }

  public void setLabelInfos(List<LabelInfo> labelInfos) {
    this.labelInfos = labelInfos;
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

  public void setOwlSameAs(List<String> owlSameAs) {
    this.owlSameAs = owlSameAs;
  }

  public List<String> getOwlSameAs() {
    return owlSameAs;
  }

  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

}
