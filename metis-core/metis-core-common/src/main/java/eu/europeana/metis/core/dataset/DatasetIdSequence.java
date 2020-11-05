package eu.europeana.metis.core.dataset;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import eu.europeana.metis.mongo.ObjectIdSerializer;
import org.bson.types.ObjectId;

/**
 * The database structure to hold the dataset identifiers sequence.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-12-27
 */
@Entity
public class DatasetIdSequence {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;

  private int sequence;

  public DatasetIdSequence() {
    //Required for json serialization
  }

  /**
   * Initialize sequence with provided argument.
   *
   * @param sequence the number to start the sequence from
   */
  public DatasetIdSequence(int sequence) {
    this.sequence = sequence;
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int sequence) {
    this.sequence = sequence;
  }
}
