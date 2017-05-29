package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.organization.ObjectIdSerializer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Indexed;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public class VoidDereferencePlugin implements AbstractMetisPlugin {
  @Indexed
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  private PluginStatus pluginStatus;
  private final PluginType pluginType = PluginType.DEREFERENCE;
  private Map<String, List<String>> parameters = new HashMap<>();

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

  public VoidDereferencePlugin() {
  }

  public VoidDereferencePlugin(VoidDereferencePluginInfo voidDereferencePluginInfo)
  {
    if (voidDereferencePluginInfo != null)
      this.parameters = voidDereferencePluginInfo.getParameters();
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  @Override
  public PluginStatus getPluginStatus() {
    return pluginStatus;
  }

  @Override
  public void setPluginStatus(PluginStatus pluginStatus) {
    this.pluginStatus = pluginStatus;
  }

  public PluginType getPluginType() {
    return pluginType;
  }

  @Override
  public Date getStartedDate() {
    return startedDate;
  }

  @Override
  public void setStartedDate(Date startedDate) {
    this.startedDate = startedDate;
  }

  @Override
  public Date getFinishedDate() {
    return finishedDate;
  }

  @Override
  public void setFinishedDate(Date finishedDate) {
    this.finishedDate = finishedDate;
  }

  @Override
  public Date getUpdatedDate() {
    return updatedDate;
  }

  @Override
  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

  @Override
  public long getRecordsProcessed() {
    return recordsProcessed;
  }

  public void setRecordsProcessed(long recordsProcessed) {
    this.recordsProcessed = recordsProcessed;
  }

  @Override
  public long getRecordsFailed() {
    return recordsFailed;
  }

  public void setRecordsFailed(long recordsFailed) {
    this.recordsFailed = recordsFailed;
  }

  @Override
  public long getRecordsCreated() {
    return recordsCreated;
  }

  public void setRecordsCreated(long recordsCreated) {
    this.recordsCreated = recordsCreated;
  }

  @Override
  public long getRecordsUpdated() {
    return recordsUpdated;
  }

  public void setRecordsUpdated(long recordsUpdated) {
    this.recordsUpdated = recordsUpdated;
  }

  @Override
  public long getRecordsDeleted() {
    return recordsDeleted;
  }

  public void setRecordsDeleted(long recordsDeleted) {
    this.recordsDeleted = recordsDeleted;
  }

  @Override
  public void setParameters(Map<String, List<String>> parameters) {
    this.parameters = parameters;
  }

  @Override
  public Map<String, List<String>> getParameters() {
    return parameters;
  }

  @Override
  public void execute() {
  }

  @Override
  public CloudStatistics monitor(String datasetId) {
    return null;
  }

}