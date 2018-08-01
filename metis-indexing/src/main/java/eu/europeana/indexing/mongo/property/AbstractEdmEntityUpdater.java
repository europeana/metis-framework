package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Updater of EDM entities (properties of {@link FullBean}).
 * 
 * @param <R> The type of the record to update.
 * @param <A> The type of the ancestor information (information from parents).
 */
public abstract class AbstractEdmEntityUpdater<R extends AbstractEdmEntity, A>
    extends AbstractMongoObjectUpdater<R, A> {

  @Override
  protected final MongoPropertyUpdater<R> createPropertyUpdater(R newEntity, A ancestorInformation,
      MongoServer mongoServer) {
    return MongoPropertyUpdater.createForObjectWithAbout(newEntity, mongoServer, getObjectClass(),
        AbstractEdmEntity::getAbout, null);
  }

  /**
   * @return The class object reflecting this updater's contextual class.
   */
  protected abstract Class<R> getObjectClass();
}
