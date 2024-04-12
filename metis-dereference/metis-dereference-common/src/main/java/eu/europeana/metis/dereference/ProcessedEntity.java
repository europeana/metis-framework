package eu.europeana.metis.dereference;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import eu.europeana.metis.mongo.utils.ObjectIdSerializer;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.bson.types.ObjectId;

/**
 * A processed (mapped) Entity Created by ymamakis on 2/11/16.
 */
@XmlRootElement
@Entity
@Indexes({
    @Index(fields = {@Field("resourceId")}, options = @IndexOptions(unique = true))
})
public class ProcessedEntity {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;

  /**
   * The resourceId (URI) of the resource
   **/
  private String resourceId;

  /**
   * A xml representation of the mapped resource in one of the contextual resources
   **/
  private String xml;

  /**
   * The ID of the vocabulary of which the resource is part.
   **/
  private String vocabularyId;

  @XmlElement
  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  @XmlElement
  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  @XmlElement
  public String getXml() {
    return xml;
  }

  public void setXml(String xml) {
    this.xml = xml;
  }

  @XmlElement
  public String getVocabularyId() {
    return vocabularyId;
  }

  public void setVocabularyId(String vocabularyId) {
    this.vocabularyId = vocabularyId;
  }
}
