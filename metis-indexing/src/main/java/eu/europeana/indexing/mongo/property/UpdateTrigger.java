package eu.europeana.indexing.mongo.property;

/**
 * Object that can be passed around and that can be used to signal that an update is required. When
 * created, an update is not assumed to be required (i.e. {@link #isUpdateTriggered()} returns
 * false). But code can signal the requirement for an update by calling the {@link #triggerUpdate()}
 * method.
 * 
 * @author jochen
 *
 */
final class UpdateTrigger {

  private boolean updateTriggered = false;

  /**
   * Can be used to signal that an update is needed. After this method is called,
   * {@link #isUpdateTriggered()} will return true.
   */
  public void triggerUpdate() {
    updateTriggered = true;
  }

  /**
   * @return Whether an update has been triggered.
   */
  public boolean isUpdateTriggered() {
    return updateTriggered;
  }
}
