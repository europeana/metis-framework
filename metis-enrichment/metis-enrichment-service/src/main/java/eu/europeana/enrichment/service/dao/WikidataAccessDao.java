package eu.europeana.enrichment.service.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CharSequenceReader;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.api.external.model.WikidataOrganization;
import eu.europeana.enrichment.service.EntityConverterUtils;
import eu.europeana.enrichment.service.exception.WikidataAccessException;


/**
 * @author GrafR
 * @since 03 April 2018
 */
public class WikidataAccessDao {

  private static final String SPARQL = "https://query.wikidata.org/sparql";
  private static final int SIZE = 1024 * 1024;

  private static final Logger LOGGER = LoggerFactory.getLogger(WikidataAccessDao.class);

  private Transformer transformer;

  private File templateFile;

  EntityConverterUtils entityConverterUtils = new EntityConverterUtils();

  public WikidataAccessDao(File templateFile) throws WikidataAccessException {
    this.templateFile = templateFile;
    init();
  }

  public EntityConverterUtils getEntityConverterUtils() {
    return entityConverterUtils;
  }

  /**
   * This method initializes classes needed for Wikidata related activities
   * 
   * @param file The template file
   * @throws WikidataAccessException
   */
  public final void init() throws WikidataAccessException {
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
    String xml = FileUtils.readFileToString(xmlFile, "UTF-8");
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
      LOGGER.error("Interrupted while querying Wikidata from the WikidataAccessDao", e);
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

    try (StringBuilderWriter sbw = new StringBuilderWriter(sb)) {
      RDFWriter writer = m.getWriter("RDF/XML");
      writer.setProperty("tab", "0");
      writer.setProperty("allowBadURIs", "true");
      writer.setProperty("relativeURIs", "");
      writer.write(m, sbw, "RDF/XML");
      t.transform(new StreamSource(new CharSequenceReader(sb)), res);
      sb.setLength(0);
    } catch (TransformerException e) {
      throw new WikidataAccessException(WikidataAccessException.TRANSFORM_WIKIDATA_TO_RDF_XML_ERROR,
          e);
    }
  }

  /**
   * This method parses wikidata organization content stored in XSLT/XML file object
   * 
   * @param inputFile The file containing the wikidata
   * @return WikidataOrganization object
   * @throws IOException
   * @throws JAXBException
   */
  public WikidataOrganization parseWikidataOrganization(File inputFile)
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
