package eu.europeana.enrichment.internal.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import eu.europeana.enrichment.api.external.model.LabelInfo;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.json.ObjectIdSerializer;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * Enrichment Term containing the relative entity object.
 *
 * @author Simon Tzanakis
 * @since 2020-08-04
 */
@Entity
@Indexes({
    @Index(fields = {@Field("created")}),
    @Index(fields = {@Field("updated")}),
    @Index(fields = {@Field("entityType")}),
    @Index(fields = {@Field("enrichmentEntity.about"), @Field("entityType")}),
    @Index(fields = {@Field("enrichmentEntity.owlSameAs"), @Field("entityType")}),
    @Index(fields = {@Field("labelInfos.lowerCaseLabel"), @Field("labelInfos.lang"),
        @Field("entityType")}),
    @Index(fields = {@Field("created"), @Field("entityType")}),
    @Index(fields = {@Field("updated"), @Field("entityType")}),
    @Index(fields = {@Field("enrichmentEntity.about")}, options = @IndexOptions(unique = true)),
    @Index(fields = {@Field("enrichmentEntity.owlSameAs")})
})
public class EnrichmentTerm {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;

  private String parent;
  private Date created;
  private Date updated;
  private EntityType entityType;
  private AbstractEnrichmentEntity enrichmentEntity;
  private List<LabelInfo> labelInfos;

  public EnrichmentTerm() {
    // Required for json serialization
  }

  public EntityType getEntityType() {
    return entityType;
  }

  public void setEntityType(EntityType entityType) {
    this.entityType = entityType;
  }

  public AbstractEnrichmentEntity getEnrichmentEntity() {
    return enrichmentEntity;
  }

  public void setEnrichmentEntity(AbstractEnrichmentEntity enrichmentEntity) {
    this.enrichmentEntity = enrichmentEntity;
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

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
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
