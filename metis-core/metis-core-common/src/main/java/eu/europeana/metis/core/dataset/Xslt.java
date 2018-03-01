package eu.europeana.metis.core.dataset;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.json.ObjectIdSerializer;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-27
 */
@Entity(value = "Xslts")
public class Xslt {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;

  private int datasetId;
  private String xslt;

  public Xslt() {
    //Required for json serialization
  }

  public Xslt(int datasetId, String xslt) {
    this.datasetId = datasetId;
    this.xslt = xslt;
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public int getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(int datasetId) {
    this.datasetId = datasetId;
  }

  public String getXslt() {
    return xslt;
  }

  public void setXslt(String xslt) {
    this.xslt = xslt;
  }
}
