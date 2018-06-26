package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Updater of EDM entities (properties of {@link FullBean}).
 * 
 * @param <T> The type of the entity to update.
 */
public abstract class AbstractEdmEntityUpdater<T extends AbstractEdmEntity>
    extends AbstractMongoObjectUpdater<T> {

  @Override
  protected final MongoPropertyUpdater<T> createPropertyUpdater(T newEntity,
      MongoServer mongoServer) {
    return MongoPropertyUpdater.createForEdmEntity(newEntity, mongoServer, getObjectClass());
  }

  /**
   * @return The class object reflecting this updater's contextual class.
   */
  protected abstract Class<T> getObjectClass();
}
