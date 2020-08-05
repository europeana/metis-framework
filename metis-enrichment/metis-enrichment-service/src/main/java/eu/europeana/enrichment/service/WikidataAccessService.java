package eu.europeana.enrichment.service;

import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.Address;
import eu.europeana.corelib.solr.entity.AddressImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.external.model.EdmOrganization;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.VcardAddress;
import eu.europeana.enrichment.api.external.model.WikidataOrganization;
import eu.europeana.enrichment.service.dao.WikidataAccessDao;
import eu.europeana.enrichment.service.exception.WikidataAccessException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;


/**
 * This class provides supporting methods for management of Wikidata communication
 *
 * @author GrafR
 */
public class WikidataAccessService {

  public static final String WIKIDATA_BASE_URL = "http://www.wikidata.org/entity/Q";
  private static final Logger LOGGER = LoggerFactory.getLogger(WikidataAccessService.class);

  private final WikidataAccessDao wikidataAccessDao;
  private final EntityConverterUtils entityConverterUtils = new EntityConverterUtils();

  /**
   * Constructor with required parameters to function
   *
   * @param wikidataAccessDao the Dao object to access wikidata
   */
  public WikidataAccessService(WikidataAccessDao wikidataAccessDao) {
    this.wikidataAccessDao = wikidataAccessDao;
  }

  public EntityConverterUtils getEntityConverterUtils() {
    return entityConverterUtils;
  }

  protected WikidataAccessDao getWikidataAccessDao() {
    return wikidataAccessDao;
  }

  /**
   * This method builds organization URI for passed ID.
   *
   * @param organizationId the organization identifier
   * @return organization URI
   */
  public URI buildOrganizationUri(String organizationId) {

    URI uri;
    String contactsSearchUrl = String.format("%s%s", WIKIDATA_BASE_URL, organizationId);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl);
    uri = builder.build().encode().toUri();
    return uri;
  }

  /**
   * Dereferences a wikidate uri and converts the response to an {@link Organization}
   *
   * @param wikidataUri the wikidata uri
   * @return the dereferenced/converted organization
   * @throws WikidataAccessException if an exception occurred during dereferencing of the wikidata
   * uri
   */
  public OrganizationImpl dereference(String wikidataUri) throws WikidataAccessException {

    StringBuilder wikidataXml = null;
    WikidataOrganization wikidataOrganization;

    try {
      wikidataXml = getWikidataAccessDao().getEntity(wikidataUri);
      wikidataOrganization = getWikidataAccessDao().parse(wikidataXml.toString());
    } catch (JAXBException e) {
      LOGGER.debug("Cannot parse wikidata response: {}", wikidataXml);
      throw new WikidataAccessException(
          "Cannot parse wikidata xml response for uri: " + wikidataUri, e);
    }

    //convert to OrganizationImpl
    if (wikidataOrganization == null) {
      return null;
    } else {
      return toOrganizationImpl(wikidataOrganization);
    }
  }

  /**
   * This method parses wikidata organization content stored in XSLT/XML file into EdmOrganization
   * object
   *
   * @param inputFile The file containing the wikidata
   * @return WikidataOrganization object
   * @throws JAXBException if an exception occurred during jaxb binding
   */
  public WikidataOrganization parseWikidataOrganization(File inputFile) throws JAXBException {

    return wikidataAccessDao.parseWikidataOrganization(inputFile);
  }

  /**
   * This method converts Wikidata organization in OrganizationImpl.
   *
   * @param wikidataOrganization the wikidata organization object to extract values from
   * @return the converted organization
   */
  public OrganizationImpl toOrganizationImpl(WikidataOrganization wikidataOrganization) {

    OrganizationImpl org = new OrganizationImpl();

    EdmOrganization edmOrganization = wikidataOrganization.getOrganization();

    if (edmOrganization.getAbout() != null) {
      String about = edmOrganization.getAbout();
      if (StringUtils.isNotEmpty(about)) {
        org.setAbout(about);
      }
    }

    if (edmOrganization.getCountry() != null) {
      String country = edmOrganization.getCountry();
      org.setEdmCountry(getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(), country));
    }

    if (edmOrganization.getHomepage() != null) {
      String homepage = edmOrganization.getHomepage().getResource();
      org.setFoafHomepage(homepage);
    }

    if (edmOrganization.getLogo() != null) {
      String logo = edmOrganization.getLogo().getResource();
      org.setFoafLogo(logo);
    }

    if (edmOrganization.getDepiction() != null) {
      String depiction = edmOrganization.getDepiction().getResource();
      org.setFoafDepiction(depiction);
    }
    
    if (edmOrganization.getMbox() != null) {
      String mbox = edmOrganization.getMbox();
      org.setFoafMbox(getEntityConverterUtils().createList(mbox));
    }

    if (edmOrganization.getPhone() != null) {
      String phone = edmOrganization.getPhone();
      org.setFoafPhone(getEntityConverterUtils().createList(phone));
    }

    if (edmOrganization.getLogo() != null) {
      String logo = edmOrganization.getLogo().getResource();
      org.setFoafLogo(logo);
    }

    List<Label> acronymLabel = edmOrganization.getAcronyms();
    org.setEdmAcronym(
        getEntityConverterUtils().createMapWithListsFromTextPropertyListMerging(acronymLabel));

    List<Resource> sameAs = edmOrganization.getSameAs();
    org.setOwlSameAs(getEntityConverterUtils().createStringArrayFromPartList(sameAs));

    List<Label> descriptions = edmOrganization.getDescriptions();
    org.setDcDescription(getEntityConverterUtils().createMapFromTextPropertyList(descriptions));

    List<Label> prefLabel = edmOrganization.getPrefLabelList();
    org.setPrefLabel(
        getEntityConverterUtils().createMapWithListsFromTextPropertyListNonMerging(prefLabel));

    List<Label> altLabel = edmOrganization.getAltLabelList();
    org.setAltLabel(
        getEntityConverterUtils().createMapWithListsFromTextPropertyListMerging(altLabel));

    if (edmOrganization.getHasAddress() != null
        && edmOrganization.getHasAddress().getVcardAddressesList() != null) {
      VcardAddress vcardAddress = edmOrganization.getHasAddress().getVcardAddressesList().get(0);
      AddressImpl addressImpl = new AddressImpl();
      addressImpl.setAbout(org.getAbout() + "#address");
      addressImpl.setVcardCountryName(vcardAddress.getCountryName());
      if(vcardAddress.getHasGeo() != null)
        addressImpl.setVcardHasGeo(vcardAddress.getHasGeo().getResource());
// TODO: enable support for other address fields and locality when the issues related to
//  the dereferencing localities, and support for multiple addresses are available
//      address.setVcardStreetAddress(vcardAddress.getStreetAddress());
//      address.setVcardLocality(vcardAddress.getLocality());
//      address.setVcardPostalCode(vcardAddress.getPostalCode());
//      address.setVcardPostOfficeBox(vcardAddress.getPostOfficeBox());
      org.setAddress(new Address(addressImpl));
    }

    return org;
  }

  /**
   * This method saves XML content to a passed file.
   *
   * @param xml The XML content
   * @param contentFile The output file
   * @throws WikidataAccessException if any I/O exception occurred
   */
  public void saveXmlToFile(String xml, File contentFile) throws WikidataAccessException {
    try {
      //create content file if needed
      final boolean wasFileCreated = contentFile.createNewFile();
      if (!wasFileCreated) {
        LOGGER.warn("Content file existed, it will be overwritten: {}", contentFile.getAbsolutePath());
      }
      FileUtils.write(contentFile, xml, StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      throw new WikidataAccessException(
          WikidataAccessException.XML_COULD_NOT_BE_WRITTEN_TO_FILE_ERROR, e);
    }
  }

  /**
   * This method performs merging of Wikidata properties into the Zoho organizations according to
   * predefined rules specified in EA-1045.
   *
   * @param zohoOrganization the organization object to which the Wikidata values will be added
   * @param wikidataOrganization the wikidata object from which the wikidata values will be
   * extracted
   */
  public void mergePropsFromWikidata(OrganizationImpl zohoOrganization,
      OrganizationImpl wikidataOrganization) {

    // Merge the pref label maps. There may be some values that could not be merged, they will later
    // be added as alternative label.
    final Map<String, List<String>> addToAltLabelMap = new HashMap<>();
    final Map<String, List<String>> newPrefLabelMap =
        getEntityConverterUtils().mergeMapsWithSingletonLists(zohoOrganization.getPrefLabel(),
            wikidataOrganization.getPrefLabel(), addToAltLabelMap);
    zohoOrganization.setPrefLabel(newPrefLabelMap);

    // merge all alternative labels (zoho, wikidata and result of previous operation).
    Map<String, List<String>> allWikidataAltLabels = getEntityConverterUtils()
        .mergeMapsWithLists(wikidataOrganization.getAltLabel(), addToAltLabelMap);
    Map<String, List<String>> mergedAltLabelMap = getEntityConverterUtils()
        .mergeMapsWithLists(allWikidataAltLabels, zohoOrganization.getAltLabel());
    zohoOrganization.setAltLabel(mergedAltLabelMap);

    // edm:acronym (if not available in Zoho for each language)
    Map<String, List<String>> acronyms = getEntityConverterUtils()
        .mergeMapsWithLists(zohoOrganization.getEdmAcronym(), wikidataOrganization.getEdmAcronym());
    zohoOrganization.setEdmAcronym(acronyms);

    // logo (if not available in zoho)
    if (StringUtils.isEmpty(zohoOrganization.getFoafLogo())) {
      zohoOrganization.setFoafLogo(wikidataOrganization.getFoafLogo());
    }

    // depiction (if not available in zoho)
    if (StringUtils.isEmpty(zohoOrganization.getFoafDepiction())) {
      zohoOrganization.setFoafDepiction(wikidataOrganization.getFoafDepiction());
    }
    
    // homepage (if not available in zoho)
    if (StringUtils.isEmpty(zohoOrganization.getFoafHomepage())) {
      zohoOrganization.setFoafLogo(wikidataOrganization.getFoafLogo());
    }

    // phone (if not duplicate)
    List<String> phoneList = getEntityConverterUtils()
        .mergeStringLists(zohoOrganization.getFoafPhone(), wikidataOrganization.getFoafPhone());
    zohoOrganization.setFoafPhone(phoneList);

    // mbox (if not duplicate)
    List<String> mbox = getEntityConverterUtils()
        .mergeStringLists(zohoOrganization.getFoafMbox(), wikidataOrganization.getFoafMbox());
    zohoOrganization.setFoafMbox(mbox);

    // sameAs (add non duplicate URIs)
    String[] sameAs = buildSameAs(zohoOrganization, wikidataOrganization);
    zohoOrganization.setOwlSameAs(sameAs);

    // description (always as not present in Zoho)
    zohoOrganization.setDcDescription(wikidataOrganization.getDcDescription());

    //address
    getEntityConverterUtils().mergeAddress(zohoOrganization, wikidataOrganization);

  }

  /**
   * This methods builds a string array by merging all sameAs statements for the given resources.
   * In case of wikidata redirection, the resource URI of the provided Wikidata organization is ensured to be present in the returned array  
   * 
   * @param zohoOrganization
   * @param wikidataOrganization
   * @return
   */
  private String[] buildSameAs(OrganizationImpl zohoOrganization, OrganizationImpl wikidataOrganization) {
    
    String[] sameAs = getEntityConverterUtils().mergeStringArrays(
        zohoOrganization.getOwlSameAs(), wikidataOrganization.getOwlSameAs());
    
    //#EA-1418 if dupplicated/redirected, wikidata resource has different URI
    String wikidataResourceUri = wikidataOrganization.getAbout();
    if(!ArrayUtils.contains(sameAs, wikidataResourceUri)){
      sameAs = ArrayUtils.add(sameAs, wikidataResourceUri);
    }
    
    return sameAs;
  }
}
