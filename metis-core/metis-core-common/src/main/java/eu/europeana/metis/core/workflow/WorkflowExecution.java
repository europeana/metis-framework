package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.Indexes;
import eu.europeana.metis.utils.CommonStringValues;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.json.ObjectIdSerializer;
import eu.europeana.metis.mongo.HasMongoObjectId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.bson.types.ObjectId;

/**
 * Is the structure where the combined plugins of harvesting and the other plugins will be stored.
 * <p>This is the object where the execution of the workflow takes place and will host all
 * information, regarding its execution.</p>
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
@Entity
@Indexes({
    @Index(fields = {@Field("datasetId")}),
    @Index(fields = {@Field("workflowStatus")}),
    @Index(fields = {@Field("ecloudDatasetId")}),
    @Index(fields = {@Field("cancelledBy")}),
    @Index(fields = {@Field("startedBy")}),
    @Index(fields = {@Field("createdDate")}),
    @Index(fields = {@Field("startedDate")}),
    @Index(fields = {@Field("updatedDate")}),
    @Index(fields = {@Field("finishedDate")}),
    //Embedded indexes definitions should be referenced on the parent entity
    @Index(fields = {@Field("metisPlugins.id")}),
    @Index(fields = {@Field("metisPlugins.startedDate")}),
    @Index(fields = {@Field("metisPlugins.updatedDate")}),
    @Index(fields = {@Field("metisPlugins.finishedDate")})})
public class WorkflowExecution implements HasMongoObjectId {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  private String datasetId;
  private WorkflowStatus workflowStatus;
  private String ecloudDatasetId;
  private String cancelledBy;
  private String startedBy;
  private int workflowPriority;
  private boolean cancelling;

  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date createdDate;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date startedDate;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date updatedDate;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date finishedDate;

  private List<AbstractMetisPlugin> metisPlugins = new ArrayList<>();

  public WorkflowExecution() {
    //Required for json serialization
  }

  /**
   * Constructor with all required parameters and initializes it's internal structure.
   *
   * @param dataset the {@link Dataset} related to the execution
   * @param metisPlugins the list of {@link AbstractMetisPlugin} including harvest plugin for
   * execution
   * @param workflowPriority the positive number of the priority of the execution
   */
  public WorkflowExecution(Dataset dataset, List<? extends AbstractMetisPlugin> metisPlugins,
      int workflowPriority) {
    this.datasetId = dataset.getDatasetId();
    this.ecloudDatasetId = dataset.getEcloudDatasetId();
    this.workflowPriority = workflowPriority;
    this.metisPlugins = new ArrayList<>(metisPlugins);
  }

  /**
   * Sets all plugins inside the execution, that have status {@link PluginStatus#INQUEUE} or {@link
   * PluginStatus#RUNNING} or {@link PluginStatus#CLEANING} or {@link PluginStatus#PENDING}, to
   * {@link PluginStatus#CANCELLED}
   */
  public void setWorkflowAndAllQualifiedPluginsToCancelled() {
    this.setWorkflowStatus(WorkflowStatus.CANCELLED);
    setAllQualifiedPluginsToCancelled();
    this.setCancelling(false);
  }

  /**
   * Checks if one of the plugins has {@link PluginStatus#FAILED} and if yes sets all other plugins
   * that have status {@link PluginStatus#INQUEUE} or {@link PluginStatus#RUNNING} or {@link
   * PluginStatus#CLEANING} or {@link PluginStatus#PENDING}, to {@link PluginStatus#CANCELLED}
   */
  public void checkAndSetAllRunningAndInqueuePluginsToCancelledIfOnePluginHasFailed() {
    boolean hasAPluginFailed = false;
    for (AbstractMetisPlugin metisPlugin : this.getMetisPlugins()) {
      if (metisPlugin.getPluginStatus() == PluginStatus.FAILED) {
        hasAPluginFailed = true;
        break;
      }
    }
    if (hasAPluginFailed) {
      this.setWorkflowStatus(WorkflowStatus.FAILED);
      setAllQualifiedPluginsToCancelled();
    }
  }

  private void setAllQualifiedPluginsToCancelled() {
    for (AbstractMetisPlugin metisPlugin : this.getMetisPlugins()) {
      if (metisPlugin.getPluginStatus() == PluginStatus.INQUEUE
          || metisPlugin.getPluginStatus() == PluginStatus.RUNNING
          || metisPlugin.getPluginStatus() == PluginStatus.CLEANING
          || metisPlugin.getPluginStatus() == PluginStatus.PENDING) {
        metisPlugin.setPluginStatusAndResetFailMessage(PluginStatus.CANCELLED);
      }
    }
  }

  /**
   * Returns an {@link Optional} for the plugin with the given plugin type.
   *
   * @param pluginType The type of the plugin we are looking for.
   * @return The plugin.
   */
  public Optional<AbstractMetisPlugin> getMetisPluginWithType(PluginType pluginType) {
    return getMetisPlugins().stream().filter(plugin -> plugin.getPluginType() == pluginType)
        .findFirst();
  }

  @Override
  public ObjectId getId() {
    return id;
  }

  @Override
  public void setId(ObjectId id) {
    this.id = id;
  }

  public boolean isCancelling() {
    return cancelling;
  }

  public void setCancelling(boolean cancelling) {
    this.cancelling = cancelling;
  }

  public WorkflowStatus getWorkflowStatus() {
    return workflowStatus;
  }

  public void setWorkflowStatus(WorkflowStatus workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
  }

  public String getCancelledBy() {
    return cancelledBy;
  }

  public void setCancelledBy(String cancelledBy) {
    this.cancelledBy = cancelledBy;
  }

  public String getStartedBy() {
    return startedBy;
  }

  public void setStartedBy(String startedBy) {
    this.startedBy = startedBy;
  }

  public String getEcloudDatasetId() {
    return ecloudDatasetId;
  }

  public void setEcloudDatasetId(String ecloudDatasetId) {
    this.ecloudDatasetId = ecloudDatasetId;
  }

  public int getWorkflowPriority() {
    return workflowPriority;
  }

  public void setWorkflowPriority(int workflowPriority) {
    this.workflowPriority = workflowPriority;
  }

  public Date getCreatedDate() {
    return createdDate == null ? null : new Date(createdDate.getTime());
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate == null ? null : new Date(createdDate.getTime());
  }

  public Date getStartedDate() {
    return startedDate == null ? null : new Date(startedDate.getTime());
  }

  public void setStartedDate(Date startedDate) {
    this.startedDate = startedDate == null ? null : new Date(startedDate.getTime());
  }

  public Date getFinishedDate() {
    return finishedDate == null ? null : new Date(finishedDate.getTime());
  }

  public void setFinishedDate(Date finishedDate) {
    this.finishedDate = finishedDate == null ? null : new Date(finishedDate.getTime());
  }

  public Date getUpdatedDate() {
    return updatedDate == null ? null : new Date(updatedDate.getTime());
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate == null ? null : new Date(updatedDate.getTime());
  }

  public List<AbstractMetisPlugin> getMetisPlugins() {
    return metisPlugins;
  }

  public void setMetisPlugins(List<AbstractMetisPlugin> metisPlugins) {
    this.metisPlugins = metisPlugins;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, datasetId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    WorkflowExecution that = (WorkflowExecution) obj;
    return Objects.equals(id, that.getId()) && Objects.equals(datasetId, that.datasetId);
  }
}
