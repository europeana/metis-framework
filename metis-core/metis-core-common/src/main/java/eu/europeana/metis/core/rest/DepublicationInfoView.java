package eu.europeana.metis.core.rest;

public class DepublicationInfoView {

  private final ResponseListWrapper<DepublishRecordIdView> depublicationRecordIds;
  private final boolean depublicationTriggerable;

  public DepublicationInfoView(
          ResponseListWrapper<DepublishRecordIdView> depublicationRecordIds,
          boolean depublicationTriggerable) {
    this.depublicationRecordIds = depublicationRecordIds;
    this.depublicationTriggerable = depublicationTriggerable;
  }

  public ResponseListWrapper<DepublishRecordIdView> getDepublicationRecordIds() {
    return depublicationRecordIds;
  }

  public boolean isDepublicationTriggerable() {
    return depublicationTriggerable;
  }
}
