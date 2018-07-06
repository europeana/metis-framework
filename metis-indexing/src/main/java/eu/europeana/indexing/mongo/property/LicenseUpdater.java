package eu.europeana.indexing.mongo.property;

import java.util.Date;

import eu.europeana.corelib.solr.entity.LicenseImpl;

/**
 * Field updater for instances of {@link LicenseImpl}.
 */
public class LicenseUpdater extends AbstractEdmEntityUpdater<LicenseImpl> {

  @Override
  protected Class<LicenseImpl> getObjectClass() {
    return LicenseImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<LicenseImpl> propertyUpdater) {
	// Note: forcing this date to be saved as java.util.Date because the mongo 
	// connection cannot deal with subclasses (like java.sql.Date).
    propertyUpdater.updateObject("ccDeprecatedOn", license -> new Date(license.getCcDeprecatedOn().getTime()));
    propertyUpdater.updateObject("odrlInheritFrom", LicenseImpl::getOdrlInheritFrom);
  }  
}
