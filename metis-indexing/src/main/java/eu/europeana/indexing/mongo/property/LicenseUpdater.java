package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.solr.entity.LicenseImpl;
import java.util.Date;
import java.util.function.UnaryOperator;

/**
 * Field updater for instances of {@link LicenseImpl}.
 */
public class LicenseUpdater extends AbstractIsolatedEdmEntityUpdater<LicenseImpl> {

  /**
   * This pre-processing function prevents dates from being saved to the database in anything other
   * than the {@link Date} class (so no subclasses, and particularly not java.sql.{@link
   * java.sql.Date}). Please note that originally this pre-processing also added the timezone
   * offset, but since this actually creates the wrong dates, we don't do that anymore.
   */
  static final UnaryOperator<Date> DATE_PREPROCESSING = date -> new Date(date.getTime());

  @Override
  protected Class<LicenseImpl> getObjectClass() {
    return LicenseImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<LicenseImpl> propertyUpdater) {
    propertyUpdater
        .updateObject("ccDeprecatedOn", LicenseImpl::getCcDeprecatedOn, DATE_PREPROCESSING);
    propertyUpdater.updateObject("odrlInheritFrom", LicenseImpl::getOdrlInheritFrom);
  }  
}
