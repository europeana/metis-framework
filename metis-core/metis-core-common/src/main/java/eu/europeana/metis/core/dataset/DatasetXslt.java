package eu.europeana.metis.core.dataset;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.json.ObjectIdSerializer;
import java.util.Date;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-27
 */
@Entity
public class DatasetXslt {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;

  private String datasetId;
  private String xslt;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date createdDate;

  public DatasetXslt() {
    //Required for json serialization
  }

  /**
   * Constructor with required parameters.
   * When created it assigns the current date to it.
   * @param datasetId the datasetId that this class is related to
   * @param xslt the raw xslt
   */
  public DatasetXslt(String datasetId, String xslt) {
    this.datasetId = datasetId;
    this.xslt = xslt;
    this.createdDate = new Date();
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
  }

  public String getXslt() {
    return xslt;
  }

  public void setXslt(String xslt) {
    this.xslt = xslt;
  }

  public Date getCreatedDate() {
    return createdDate == null ? null : new Date(createdDate.getTime());
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate == null ? null : new Date(createdDate.getTime());
  }
}
