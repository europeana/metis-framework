package eu.europeana.enrichment.service.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import javax.xml.bind.JAXBException;
import org.apache.maven.shared.utils.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.service.EntityConverterUtils;
import eu.europeana.enrichment.service.exception.WikidataAccessException;


/**
 * This class provides supporting methods for management
 * of Wikidata communication
 * 
 * @author GrafR
 *
 */
public class WikidataAccessService {

  String WIKIDATA_BASE_URL = "http://www.wikidata.org/entity/Q";
  
  WikidataAccessDao wikidataAccessDao;
  
  EntityConverterUtils entityConverterUtils = new EntityConverterUtils();
  
  public WikidataAccessService(WikidataAccessDao wikidataAccessDao) {
    this.wikidataAccessDao = wikidataAccessDao;
  }
  
  
  public EntityConverterUtils getEntityConverterUtils() {
    return entityConverterUtils;
  }

  public WikidataAccessDao getWikidataAccessDao() {
    return wikidataAccessDao;
  }
  
  /**
   * This method builds organization URI for passed ID.
   * @param organizationId
   * @return organization URI
   */
  public URI buildOrganizationUriById(String organizationId) {

    URI uri = null;
    String contactsSearchUrl = String.format("%s%s", WIKIDATA_BASE_URL, organizationId);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl);
    uri = builder.build().encode().toUri();
    return uri;
  }
  
  /**
   * This method parses wikidata organization content stored in XSLT/XML file
   * into EdmOrganization object
   * @param inputFile The file containing the wikidata
   * @return WikidataOrganization object
   * @throws IOException 
   * @throws JAXBException 
   */
  public WikidataOrganization parseWikidataFromXsltXmlFile(File inputFile)
      throws IOException, JAXBException {

    return wikidataAccessDao.parseWikidataFromXsltXmlFile(inputFile);
  }

  /**
   * This method converts Wikidata organization in OrganizationImpl
   * @param wikidataOrganization
   * @return OrganizationImpl object
   */
  public Organization toOrganizationImpl(WikidataOrganization wikidataOrganization)
      throws WikidataAccessException {

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
   * This method loads XML content from given file.
   * @param path The path to the file
   * @return XML content in string format
   * @throws IOException
   */
  public String readXmlFile(File contentFile) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader(contentFile))) {
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
        sb.append("\n");
      }
    }
    return sb.toString();
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
    try {
      FileWriter fileWriter = new FileWriter(contentFile);
      fileWriter.write(xml);
      fileWriter.flush();
      fileWriter.close();
      res = true;
    } catch (IOException e) {
      throw new WikidataAccessException(WikidataAccessException.XML_COULD_NOT_BE_WRITTEN_TO_FILE_ERROR, e);
    }
    return res;
  }
}
