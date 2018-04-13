package eu.europeana.enrichment.service.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.input.CharSequenceReader;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import eu.europeana.corelib.definitions.edm.entity.ContextualClass;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.service.EntityConverterUtils;
import eu.europeana.enrichment.service.exception.WikidataAccessException;


/**
 * @author GrafR
 * @since 03 April 2018
 */
public class WikidataAccessDao {

  private static String SPARQL = "https://query.wikidata.org/sparql";
  private static int SIZE = 1024 * 1024;

  private Transformer transformer;

  private File templateFile;
  
  EntityConverterUtils entityConverterUtils = new EntityConverterUtils();
   
  public EntityConverterUtils getEntityConverterUtils() {
    return entityConverterUtils;
  }
  
  public WikidataAccessDao(File templateFile) throws WikidataAccessException {
    this.templateFile = templateFile;
    init();
  }
  
  /**
   * This method initializes classes needed for Wikidata related activities
   * 
   * @param file The template file
   * @throws WikidataAccessException
   */
  public void init() throws WikidataAccessException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    try {     
      Source xslt = new StreamSource(templateFile);
      transformer = transformerFactory.newTransformer(xslt);
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");             
    } catch (TransformerConfigurationException e) {
      throw new WikidataAccessException(WikidataAccessException.TRANSFORMER_CONFIGURATION_ERROR, e);
    }
  }

  /**
   * This method retrieves organization RDF/XML data from Wikidata using SPARQL query and stores it
   * in XSLT/XML format in a given file applying XSLT template.
   * 
   * @param uri The Wikidata URI in string format
   * @param outputFile To store Wikidata response in RDF format
   * @return String The Result of Wikidata query in XML format
   * @throws WikidataAccessException
   * @throws IOException
   */
  public StringBuilder dereference(String uri) throws WikidataAccessException, IOException {

    StringBuilder res = new StringBuilder();
    StreamResult wikidataRes = new StreamResult(new StringBuilderWriter(res));
    translate(uri, wikidataRes);
    return res;
  }

  /**
   * This method converts XML string to Wikidata organization object.
   * 
   * @param xmlFile The Wikidata organization object in XML format
   * @return Wikidata organization object
   * @throws JAXBException
   * @throws IOException
   */
  public WikidataOrganization parse(File xmlFile) throws JAXBException, IOException {
    String xml = getEntityConverterUtils().readFile(xmlFile);
    return parse(xml);
  }

  /**
   * This method converts XML string to Wikidata organization object.
   * 
   * @param xml The Wikidata organization object in string XML format
   * @return Wikidata organization object
   * @throws JAXBException
   * @throws IOException
   */
  public WikidataOrganization parse(String xml) throws JAXBException, IOException {
    JAXBContext jc = JAXBContext.newInstance(WikidataOrganization.class);

    Unmarshaller unmarshaller = jc.createUnmarshaller();
    InputStream stream = new ByteArrayInputStream(xml.getBytes());
    WikidataOrganization result = (WikidataOrganization) unmarshaller.unmarshal(stream);

    return result;
  }

  /**
   * This method creates SPARQL query by passed URI to Wikidata.
   * 
   * @param uri The Wikidata URI in string format
   * @return RDF model
   */
  private Model getModelFromSPARQL(String uri) {
    String sDescribe = "DESCRIBE <" + uri + ">";

    Model m = ModelFactory.createDefaultModel();
    QueryEngineHTTP endpoint = new QueryEngineHTTP(SPARQL, sDescribe);
    try {
      return endpoint.execDescribe(m);
    } catch (RiotException e) {
      System.out.println("Error: " + e.getMessage());
    } finally {
      endpoint.close();
    }

    return m;
  }

  /**
   * This method transforms StreamResult to XML format
   * 
   * @param m The RDF model
   * @param t The transformer e.g. based on XSLT template
   * @param res The StreamResult of Wikidata query
   * @throws WikidataAccessException
   */
  private synchronized void transform(Model m, Transformer t, StreamResult res)
      throws WikidataAccessException {

    StringBuilder sb = new StringBuilder(SIZE);
    StringBuilderWriter sbw = new StringBuilderWriter(sb);
    try {
      RDFWriter writer = m.getWriter("RDF/XML");
      writer.setProperty("tab", "0");
      writer.setProperty("allowBadURIs", "true");
      writer.setProperty("relativeURIs", "");
      writer.write(m, sbw, "RDF/XML");
      t.transform(new StreamSource(new CharSequenceReader(sb)), res);
    } catch (Exception e) {
      throw new WikidataAccessException(WikidataAccessException.TRANSFORM_WIKIDATA_TO_RDF_XML_ERROR,
          e);
    } finally {
      sb.setLength(0);
    }
  }

  /**
   * This method parses wikidata organization content stored in XSLT/XML file into EdmOrganization
   * object
   * 
   * @param inputFile The file containing the wikidata
   * @return WikidataOrganization object
   * @throws IOException
   * @throws JAXBException
   */
  public WikidataOrganization parseWikidataFromXsltXmlFile(File inputFile)
      throws IOException, JAXBException {

    JAXBContext jc = JAXBContext.newInstance(WikidataOrganization.class);

    Unmarshaller unmarshaller = jc.createUnmarshaller();
    WikidataOrganization result = (WikidataOrganization) unmarshaller.unmarshal(inputFile);

    return result;
  }

  /**
   * This method loads and transforms StreamResult to XML format
   * 
   * @param uri The Wikidata URI in string format
   * @param res The StreamResult of Wikidata query
   * @throws WikidataAccessException
   */
  public void translate(String uri, StreamResult res) throws WikidataAccessException {
    transformer.setParameter("rdf_about", uri);
    transform(getModelFromSPARQL(uri), transformer, res);
  }
  
  /**
   * This method performs merging between Zoho and Wikidata inputs according to predefined rules
   * listed below.
   * @param zohoOrganization
   * @param wikidataOrganization
   * @return merged organization object based on Zoho organization
   */
  public Organization merge(Organization zohoOrganization, Organization wikidataOrganization) {
    
    /** 
     * prefLabel (if a different pref label exists for the given language, 
     * add the label to alt label list for the same language, if it is also not a duplicate)
     */
    if (!getEntityConverterUtils().isEqualLanguageListMap(
        zohoOrganization.getPrefLabel(), wikidataOrganization.getPrefLabel())) {      
      Map<String, List<String>> diffLanguageListMap = getEntityConverterUtils().extractValuesNotIncludedInBaseMap(
          zohoOrganization.getPrefLabel(), wikidataOrganization.getPrefLabel());
      Map<String, List<String>> mergedLanguageListMap = getEntityConverterUtils().mergeLanguageListMap(
          ((ContextualClass) zohoOrganization).getAltLabel(), diffLanguageListMap);
      ((ContextualClass) zohoOrganization).setAltLabel(mergedLanguageListMap);
    }
    
    /**
     * altLabel (merge labels for each language if they are not duplicates)
     */
    Map<String, List<String>> mergedLanguageListMap = getEntityConverterUtils().mergeLanguageListMap(
        ((ContextualClass) zohoOrganization).getAltLabel(), ((ContextualClass) wikidataOrganization).getAltLabel());
    ((ContextualClass) zohoOrganization).setAltLabel(mergedLanguageListMap);
      
    /**
     * edm:acronym (if not available in Zoho)
     */
    if (zohoOrganization.getEdmAcronym().size() == 0 && wikidataOrganization.getEdmAcronym().size() > 0) 
      zohoOrganization.setEdmAcronym(wikidataOrganization.getEdmAcronym());    
    
    /**
     * logo (if not available in zoho)
     */
    if (StringUtils.isEmpty(zohoOrganization.getFoafLogo()) && !StringUtils.isEmpty(wikidataOrganization.getFoafLogo())) 
      zohoOrganization.setFoafLogo(wikidataOrganization.getFoafLogo());    
        
    /**
     * homepage (if not available in zoho)
     */
    if (StringUtils.isEmpty(zohoOrganization.getFoafHomepage()) 
        && !StringUtils.isEmpty(wikidataOrganization.getFoafHomepage())) 
      zohoOrganization.setFoafLogo(wikidataOrganization.getFoafLogo());    
        
    /**
     * phone (if not duplicate)
     */
    List<String> diffList = getEntityConverterUtils().mergeStringLists(
        zohoOrganization.getFoafPhone(), wikidataOrganization.getFoafPhone());
    if (diffList != null && diffList.size() > 0) 
      zohoOrganization.setFoafPhone(diffList);   
        
    /**
     * mbox (if not duplicate)
     */
    List<String> diffMboxList = getEntityConverterUtils().mergeStringLists(
        zohoOrganization.getFoafMbox(), wikidataOrganization.getFoafMbox());
    if (diffMboxList != null && diffMboxList.size() > 0) 
      zohoOrganization.setFoafMbox(diffMboxList);   
        
    /**
     * country (ignore as it is mandatory in Zoho)
     */
//    Map<String, String> mergedLanguageStringListMap = getEntityConverterUtils().mergeLanguageListMap(
//        zohoOrganization.getEdmCountry(), wikidataOrganization.getEdmCountry());
//    zohoOrganization.setEdmCountry(mergedLanguageStringListMap);
        
    /**
     * sameAs (add non duplicate labels)
     */
//    String[] both = (String[]) ArrayUtils.addAll(zohoOrganization.getOwlSameAs(), wikidataOrganization.getOwlSameAs());
    String[] mergedStringArray = getEntityConverterUtils().concatenateStringArrays(
        zohoOrganization.getOwlSameAs(), wikidataOrganization.getOwlSameAs());
    zohoOrganization.setOwlSameAs(mergedStringArray);
        
    /**                                          
     * description (always as not present in Zoho)      
     */
    zohoOrganization.setDcDescription(wikidataOrganization.getDcDescription());
    
    return (Organization) zohoOrganization;
  }
}
