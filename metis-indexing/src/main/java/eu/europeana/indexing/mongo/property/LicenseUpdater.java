package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.solr.entity.LicenseImpl;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.UnaryOperator;

/**
 * Field updater for instances of {@link LicenseImpl}.
 */
public class LicenseUpdater extends AbstractIsolatedEdmEntityUpdater<LicenseImpl> {

  /**
   * <p>This pre-processing function prevents dates from being saved to the database in anything
   * other than the {@link Date} class (so no subclasses, and particularly not java.sql.{@link
   * java.sql.Date}). </p>
   * <p>This pre-processing function also adds the time zone difference. This is a hack to fix a
   * problem with the parsing of the RDF. This parsing mechanism assumes that the deprecation date
   * (which does not come with a time zone indication) is in this machine's timezone, whereas we
   * want this to be UTC (making it independent of machine, timezone and whether or not daylight
   * savings is in effect).</p>
   */
  protected static final UnaryOperator<Date> DEPRECATED_ON_PREPROCESSING = date -> new Date(
      date.getTime() + TimeZone.getDefault().getOffset(date.getTime()));

  @Override
  protected Class<LicenseImpl> getObjectClass() {
    return LicenseImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<LicenseImpl> propertyUpdater) {
    propertyUpdater.updateObject("ccDeprecatedOn", LicenseImpl::getCcDeprecatedOn,
        DEPRECATED_ON_PREPROCESSING);
    propertyUpdater.updateObject("odrlInheritFrom", LicenseImpl::getOdrlInheritFrom);
  }
}
