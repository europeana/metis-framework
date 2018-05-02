package eu.europeana.enrichment.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.apache.maven.shared.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.external.model.EdmOrganization;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.WikidataOrganization;
import eu.europeana.enrichment.service.dao.WikidataAccessDao;
import eu.europeana.enrichment.service.exception.WikidataAccessException;


/**
 * This class provides supporting methods for management
 * of Wikidata communication
 * 
 * @author GrafR
 *
 */
public class WikidataAccessService {

  public static final String WIKIDATA_BASE_URL = "http://www.wikidata.org/entity/Q";
  public static final String WIKIDATA_ORGANIZATION_XSL_FILE = "/wkd2org.xsl";
  private static final Logger LOGGER = LoggerFactory.getLogger(WikidataAccessService.class);

  private WikidataAccessDao wikidataAccessDao;
  
  public WikidataAccessService(WikidataAccessDao wikidataAccessDao) {
    this.wikidataAccessDao = wikidataAccessDao;
  }
  
  private EntityConverterUtils entityConverterUtils = new EntityConverterUtils();
    
  public EntityConverterUtils getEntityConverterUtils() {
    return entityConverterUtils;
  }

  protected WikidataAccessDao getWikidataAccessDao() {
    return wikidataAccessDao;
  }
  
  /**
   * This method builds organization URI for passed ID.
   * @param organizationId
   * @return organization URI
   */
  public URI buildOrganizationUri(String organizationId) {

    URI uri = null;
    String contactsSearchUrl = String.format("%s%s", WIKIDATA_BASE_URL, organizationId);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl);
    uri = builder.build().encode().toUri();
    return uri;
  }
  
  public Organization dereference(String wikidataUri) throws WikidataAccessException {
    
    StringBuilder wikidataXml = null;
    WikidataOrganization wikidataOrganization = null;
    
    try {
      wikidataXml = getWikidataAccessDao().getEntity(wikidataUri);
      wikidataOrganization = getWikidataAccessDao().parse(wikidataXml.toString());
    } catch (IOException e) {
      LOGGER.warn("Cannot fetch wikidata entity with uri: {}", wikidataUri, e);
    } catch (JAXBException e) {
      LOGGER.debug("Cannot parse wikidata response: {}", wikidataXml);
      throw new WikidataAccessException("Cannot parse wikidata xml response for uri: " + wikidataUri, e);
    }
    
    //convert to OrganizationImpl
    if(wikidataOrganization == null){
      return null;
    }else{
      return toOrganizationImpl(wikidataOrganization);
    }
  }
  
  /**
   * This method parses wikidata organization content stored in XSLT/XML file
   * into EdmOrganization object
   * @param inputFile The file containing the wikidata
   * @return WikidataOrganization object
   * @throws IOException 
   * @throws JAXBException 
   */
  public WikidataOrganization parseWikidataOrganization(File inputFile)
      throws IOException, JAXBException {

    return wikidataAccessDao.parseWikidataOrganization(inputFile);
  }

  /**
   * This method converts Wikidata organization in OrganizationImpl
   * @param wikidataOrganization
   */
  public Organization toOrganizationImpl(WikidataOrganization wikidataOrganization){

    OrganizationImpl org = new OrganizationImpl();

    EdmOrganization edmOrganization = wikidataOrganization.getOrganization();

    if (edmOrganization.getAbout() != null) {
      String about = edmOrganization.getAbout();
      if (StringUtils.isNotEmpty(about))
        org.setAbout(about);
    }
    
    if (edmOrganization.getCountry() != null) {
      String country = edmOrganization.getCountry();
      org.setEdmCountry(getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(),country));
    }
    
    if (edmOrganization.getHomepage() != null) {
      String homepage = edmOrganization.getHomepage().getResource();
      org.setFoafHomepage(homepage);
    }     

    List<Label> acronymLabel = edmOrganization.getAcronyms();
    org.setEdmAcronym(getEntityConverterUtils().createLanguageMapFromTextPropertyList(acronymLabel));
    
    List<Resource> sameAs = edmOrganization.getSameAs();
    org.setOwlSameAs(getEntityConverterUtils().createStringArrayFromPartList(sameAs));
    
    List<Label> descriptions = edmOrganization.getDescriptions();
    org.setDcDescription(getEntityConverterUtils().createStringStringMapFromTextPropertyList(descriptions));
    
    List<Label> prefLabel = edmOrganization.getPrefLabelList();
    org.setPrefLabel(getEntityConverterUtils().createLanguageMapFromTextPropertyList(prefLabel));
    
    List<Label> altLabel = edmOrganization.getAltLabelList();
    org.setAltLabel(getEntityConverterUtils().createLanguageMapFromTextPropertyList(altLabel));
    
    return org;
  }  
  
  /**
   * This method saves XML content to a passed file.
   * @param xml The XML content
   * @param contentFile The output file
   * @return true if successfully written
   * @throws WikidataAccessException
   */
  public boolean saveXmlToFile(String xml, File contentFile) throws WikidataAccessException {
    boolean res = false;
    try (FileWriter fileWriter = new FileWriter(contentFile)) {
      fileWriter.write(xml);
      fileWriter.flush();
      fileWriter.close();
      res = true;
    } catch (IOException e) {
      throw new WikidataAccessException(WikidataAccessException.XML_COULD_NOT_BE_WRITTEN_TO_FILE_ERROR, e);
    }
    return res;
  }
  
  /**
   * This method performs merging of Wikidata properties into the Zoho organizations according to predefined rules
   * specified in EA-1045.
   * 
   * @param zohoOrganization the organization object to which the Wikidata values will be added 
   * @param wikidataOrganization
   */
  public void mergePropsFromWikidata(Organization zohoOrganization, Organization wikidataOrganization) {
    //see EA-1045 for individual specs
    // prefLabel (if a different pref label exists for the given language, add the label to alt
    // label list for the same language, if it is also not a duplicate)
    Map<String, List<String>> addToAltLabelMap = new HashMap<String, List<String>>();
    //results are set directly in prefLabel and addToAltLabelMap maps
    getEntityConverterUtils()
        .mergePrefLabel(zohoOrganization.getPrefLabel(),
            wikidataOrganization.getPrefLabel(), addToAltLabelMap);
    
    // merge all alternative labels from wikidata
    Map<String, List<String>> allWikidataAltLabels = getEntityConverterUtils()
        .mergeLanguageMap(wikidataOrganization.getAltLabel(), addToAltLabelMap);

    //merge all wikidata alternative labels to zoho alternative labels
    Map<String, List<String>> mergedAltLabelMap = getEntityConverterUtils()
        .mergeLanguageMap(allWikidataAltLabels, zohoOrganization.getAltLabel());
   zohoOrganization.setAltLabel(mergedAltLabelMap);

   // edm:acronym (if not available in Zoho for each language)
   Map<String, List<String>> acronyms = getEntityConverterUtils()
       .mergeLanguageMap(zohoOrganization.getEdmAcronym(), wikidataOrganization.getEdmAcronym());   
   zohoOrganization.setEdmAcronym(acronyms);

     // logo (if not available in zoho)
    if (StringUtils.isEmpty(zohoOrganization.getFoafLogo())){
      zohoOrganization.setFoafLogo(wikidataOrganization.getFoafLogo());
    }

     // homepage (if not available in zoho)
    if (StringUtils.isEmpty(zohoOrganization.getFoafHomepage())){
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

    // sameAs (add non duplicate labels)
    String[] sameAs = getEntityConverterUtils().mergeStringArrays(
        zohoOrganization.getOwlSameAs(), wikidataOrganization.getOwlSameAs());
    zohoOrganization.setOwlSameAs(sameAs);

    // description (always as not present in Zoho)
    zohoOrganization.setDcDescription(wikidataOrganization.getDcDescription());
    
    //address
    getEntityConverterUtils().mergeAddress(zohoOrganization, wikidataOrganization);
    
  }

}
