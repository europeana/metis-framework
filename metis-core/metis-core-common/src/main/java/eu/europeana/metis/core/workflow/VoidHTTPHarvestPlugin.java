package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.organization.ObjectIdSerializer;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Indexed;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public class VoidHTTPHarvestPlugin implements AbstractMetisPlugin {
  @Indexed
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  private PluginStatus pluginStatus;
  private final PluginType pluginType = PluginType.HTTP_HARVEST;

  @Indexed
  private Date startedDate;
  @Indexed
  private Date finishedDate;
  @Indexed
  private Date updatedDate;

  private long recordsProcessed;
  private long recordsFailed;
  private long recordsCreated;
  private long recordsUpdated;
  private long recordsDeleted;

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public PluginType getPluginType() {
    return pluginType;
  }

  @Override
  public PluginStatus getPluginStatus() {
    return pluginStatus;
  }

  @Override
  public void setPluginStatus(PluginStatus pluginStatus) {
    this.pluginStatus = pluginStatus;
  }

  public Date getStartedDate() {
    return startedDate;
  }

  public void setStartedDate(Date startedDate) {
    this.startedDate = startedDate;
  }

  public Date getFinishedDate() {
    return finishedDate;
  }

  public void setFinishedDate(Date finishedDate) {
    this.finishedDate = finishedDate;
  }

  public Date getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

  @Override
  public long getRecordsProcessed() {
    return recordsProcessed;
  }

  @Override
  public void setRecordsProcessed(long recordsProcessed) {
    this.recordsProcessed = recordsProcessed;
  }

  @Override
  public long getRecordsFailed() {
    return recordsFailed;
  }

  @Override
  public void setRecordsFailed(long recordsFailed) {
    this.recordsFailed = recordsFailed;
  }

  @Override
  public long getRecordsCreated() {
    return recordsCreated;
  }

  @Override
  public void setRecordsCreated(long recordsCreated) {
    this.recordsCreated = recordsCreated;
  }

  @Override
  public long getRecordsUpdated() {
    return recordsUpdated;
  }

  @Override
  public void setRecordsUpdated(long recordsUpdated) {
    this.recordsUpdated = recordsUpdated;
  }

  @Override
  public long getRecordsDeleted() {
    return recordsDeleted;
  }

  @Override
  public void setRecordsDeleted(long recordsDeleted) {
    this.recordsDeleted = recordsDeleted;
  }

  @Override
  public void setParameters(Map<String, List<String>> parameters) {

  }

  @Override
  public Map<String, List<String>> getParameters() {
    return null;
  }

  @Override
  public void execute() {
  }

  @Override
  public CloudStatistics monitor(String dataseId) {
    return null;
  }
}
