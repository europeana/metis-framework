package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;

/**
 * Updater of isolated EDM entities (properties of {@link FullBean} that are not accompanied by
 * ancestor information).
 * 
 * @param <R> The type of the record to update.
 */
public abstract class AbstractIsolatedEdmEntityUpdater<R extends AbstractEdmEntity>
    extends AbstractEdmEntityUpdater<R, Void> {


  @Override
  protected final void update(MongoPropertyUpdater<R> propertyUpdater, Void ancestorInformation) {
    update(propertyUpdater);
  }

  /**
   * This method performs the actual updates on the property updater.
   * 
   * @param propertyUpdater The updater to update.
   */
  protected abstract void update(MongoPropertyUpdater<R> propertyUpdater);

}
