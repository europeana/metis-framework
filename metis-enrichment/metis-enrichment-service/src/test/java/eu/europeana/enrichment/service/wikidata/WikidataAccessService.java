package eu.europeana.enrichment.service.wikidata;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.springframework.web.util.UriComponentsBuilder;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.service.exception.WikidataAccessException;
import java.io.FileOutputStream;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;


/**
 * This class provides supporting methods for management
 * of Wikidata communication
 * 
 * @author GrafR
 *
 */
public class WikidataAccessService {

  String WIKIDATA_BASE_URL = "http://www.wikidata.org/entity/Q";
  final String WIKIDATA_ORGANIZATION_XSLT_TEMPLATE = "wkd2org.xsl";
  
  WkdOrgDereferencer wkdOrgDereferencer;
  
  /**
   * This method initializes classes needed for Wikidata related activities
   * 
   * @throws WikidataAccessException
   */
  public void initializeWikidataInfrastucture() throws WikidataAccessException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer;
    try {     
      Source xslt = new StreamSource(new File(WIKIDATA_ORGANIZATION_XSLT_TEMPLATE));
      transformer = transformerFactory.newTransformer(xslt);
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");            
      wkdOrgDereferencer = new WkdOrgDereferencer(transformer);
    } catch (TransformerConfigurationException e) {
      throw new WikidataAccessException(WikidataAccessException.TRANSFORMER_CONFIGURATION_ERROR, e);
    }
  }
  
  /**
   * This method retrieves organization RDF/XML data from Wikidata using SPARQL query and stores it
   * in XSLT/XML format in a given file applying XSLT template.
   * 
   * @param uri
   * @param outputFile
   * @throws WikidataAccessException
   */
  public void retrieveWikidataToXsltXmlFile(String uri, String outputFile)
      throws WikidataAccessException {
    FileOutputStream fos;
    try {
      fos = new FileOutputStream(outputFile);
      StreamResult wikidataRes = new StreamResult(fos);
      wkdOrgDereferencer.translate(uri, wikidataRes);
    } catch (FileNotFoundException e) {
      throw new WikidataAccessException(
          WikidataAccessException.OUTPUT_WIKIDATA_FILE_NOT_FOUND_ERROR, e);
    }
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
    public WikidataOrganization parseWikidataFromXsltXmlFile(String inputFile) 
        throws IOException, JAXBException {

    JAXBContext jc = JAXBContext.newInstance(WikidataOrganization.class);
    
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    File xml = new File(inputFile);
    WikidataOrganization result = (WikidataOrganization) unmarshaller.unmarshal(xml);

    return result;    
  }
    
  /**
   * @param zohoOrganization
   * @param wikidataOrganization
   * @return
   */
  public Organization mergeZohoAndWikidataOrganizationObjects(Organization zohoOrganization,
      WikidataOrganization wikidataOrganization) {

    Map<String, String> mapStringString = new HashMap<String, String>();
    mapStringString.put("EN", wikidataOrganization.getOrganization().getCountry());
    zohoOrganization.setEdmCountry(mapStringString);
    //...
    return zohoOrganization;
  }
    
  
}
