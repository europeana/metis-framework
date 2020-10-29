package eu.europeana.indexing.fullbean;

import java.util.function.Function;
import eu.europeana.metis.schema.jibx.License;
import eu.europeana.corelib.solr.entity.LicenseImpl;

/**
 * Converts a {@link License} from an {@link eu.europeana.metis.schema.jibx.RDF} to a
 * {@link LicenseImpl} for a {@link eu.europeana.metis.schema.edm.beans.FullBean}.
 */
class LicenseFieldInput implements Function<License, LicenseImpl> {

  @Override
  public LicenseImpl apply(License jibxLicense) {
    LicenseImpl mongoLicense = new LicenseImpl();
    mongoLicense.setAbout(jibxLicense.getAbout());
    if (jibxLicense.getDeprecatedOn() != null) {
      mongoLicense.setCcDeprecatedOn(jibxLicense.getDeprecatedOn().getDate());
    }
    mongoLicense.setOdrlInheritFrom(jibxLicense.getInheritFrom().getResource());
    return mongoLicense;
  }
}
