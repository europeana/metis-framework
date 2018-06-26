package eu.europeana.indexing.mongo.property;

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
    propertyUpdater.updateObject("ccDeprecatedOn", LicenseImpl::getCcDeprecatedOn);
    propertyUpdater.updateObject("odrlInheritFrom", LicenseImpl::getOdrlInheritFrom);
  }
}
