package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.definitions.jibx.License;
import eu.europeana.corelib.solr.entity.LicenseImpl;

/**
 * Converts a {@link License} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to a
 * {@link LicenseImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
class LicenseFieldInput {

  LicenseImpl createLicenseMongoFields(License jibxLicense) {
    LicenseImpl mongoLicense = new LicenseImpl();
    mongoLicense.setAbout(jibxLicense.getAbout());
    mongoLicense.setCcDeprecatedOn(jibxLicense.getDeprecatedOn().getDate());
    mongoLicense.setOdrlInheritFrom(jibxLicense.getInheritFrom().getResource());
    return mongoLicense;
  }
}
