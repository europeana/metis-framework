package eu.europeana.metis.dereference;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.metis.mongo.utils.ObjectIdSerializer;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * A processed (mapped) Entity Created by ymamakis on 2/11/16.
 */
@XmlRootElement
@Entity
@Indexes({
    @Index(fields = {@Field(ProcessedEntity.RESOURCE_ID_FIELD)}, options = @IndexOptions(unique = true))
})
@Setter
@Getter
public class ProcessedEntity {

  public static final String MONGO_ID_FIELD = "_id";
  public static final String RESOURCE_ID_FIELD = "resourceId";
  public static final String VOCABULARY_ID_FIELD = "vocabularyId";
  public static final String XML_FIELD = "xml";
  public static final String RESULT_STATUS_FIELD = "resultStatus";

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;

  /**
   * The resourceId (URI) of the resource.
   **/
  private String resourceId;

  /**
   * A xml representation of the contextual resource (transformed from the original entity).
   **/
  private String xml;

  /**
   * The ID of the vocabulary of which the resource is part.
   **/
  private String vocabularyId;

  /**
   * The status of the dereference operation.
   */
  private DereferenceResultStatus resultStatus;
}
