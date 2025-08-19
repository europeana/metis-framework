package eu.europeana.indexing.record.v2.entity;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;
import eu.europeana.indexing.record.v2.property.AbstractMongoObjectUpdater;
import eu.europeana.indexing.record.v2.property.MongoPropertyUpdater;
import eu.europeana.indexing.record.v2.property.MongoPropertyUpdaterFactory;
import eu.europeana.metis.mongo.dao.RecordDao;
import java.util.Date;

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
      Date recordDate, Date recordCreationDate, RecordDao mongoServer) {
    return MongoPropertyUpdaterFactory.createForObjectWithAbout(newEntity, mongoServer,
        getObjectClass(), AbstractEdmEntity::getAbout, null);
  }

  /**
   * @return The class object reflecting this updater's contextual class.
   */
  protected abstract Class<R> getObjectClass();
}
