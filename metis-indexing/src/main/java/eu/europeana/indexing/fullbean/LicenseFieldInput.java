package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.solr.entity.LicenseImpl;

class LicenseFieldInput {

  LicenseImpl createLicenseMongoFields(eu.europeana.corelib.definitions.jibx.License jibxLicense) {
    LicenseImpl mongoLicense = new LicenseImpl();
    mongoLicense.setAbout(jibxLicense.getAbout());
    mongoLicense.setCcDeprecatedOn(jibxLicense.getDeprecatedOn().getDate());
    mongoLicense.setOdrlInheritFrom(jibxLicense.getInheritFrom().getResource());
    return mongoLicense;
  }
}
