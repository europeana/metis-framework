package eu.europeana.enrichment.api.external.model.zoho;

import java.util.Date;
import java.util.List;

/**
 * This API defines methods for Zoho organization object.
 * 
 * @author GrafR
 *
 */
public interface ZohoOrganization {

  String getZohoId();

  String getOrganizationName();

  List<String> getAlternativeOrganizationName();

  String getOrganizationOwner();

  String getAcronym();

  String getDomain();

  String getOrganizationCountry();

  String getSector();

  String getLogo();

  String getWebsite();

  String getLanguageForOrganizationName();

  List<String> getAlternativeLanguage();

  String getRole();

  String getScope();

  String getGeographicLevel();

  String getModifiedBy();

  Date getModified();

  Date getCreated();

  List<String> getSameAs();

  String getPostBox();

  String getStreet();

  String getCity();

  String getZipCode();

  String getCountry();

  String getDescription();

  String getLangAcronym();

}
