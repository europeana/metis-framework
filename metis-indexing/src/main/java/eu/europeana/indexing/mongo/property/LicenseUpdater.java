package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.solr.entity.LicenseImpl;
import java.util.Date;
import java.util.TimeZone;

/**
 * Field updater for instances of {@link LicenseImpl}.
 */
public class LicenseUpdater extends AbstractIsolatedEdmEntityUpdater<LicenseImpl> {

  @Override
  protected Class<LicenseImpl> getObjectClass() {
    return LicenseImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<LicenseImpl> propertyUpdater) {
    // Note: forcing this date to be saved as java.util.Date because the mongo
    // connection cannot deal with subclasses (like java.sql.Date).
    // Also on the date adding the timezone difference is required so
    // that when stored it will become the correct UTC value
    propertyUpdater.updateObject("ccDeprecatedOn", license -> {
      Date ccDeprecatedOnDate = new Date(license.getCcDeprecatedOn().getTime());
      return new Date(ccDeprecatedOnDate.getTime() + TimeZone.getDefault()
          .getOffset(ccDeprecatedOnDate.getTime()));
    });
    propertyUpdater.updateObject("odrlInheritFrom", LicenseImpl::getOdrlInheritFrom);
  }  
}
