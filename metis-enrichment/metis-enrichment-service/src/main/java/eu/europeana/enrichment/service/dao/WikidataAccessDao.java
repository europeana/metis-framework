package eu.europeana.enrichment.service.dao;

import eu.europeana.enrichment.api.external.model.WikidataOrganization;
import eu.europeana.enrichment.service.exception.WikidataAccessException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.xml.XMLConstants;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;


/**
 * Wikidata Access Object for remote wikidata objects.
 * <p>It is not a common DAO per se, in the sense
 * that it does not access directly a database but rather talks to wikidata urls, or a wikidata
 * sparql endpoint. It also contains several utility methods that parse a file, input stream
 * etc.</p>
 *
 * @author GrafR
 * @since 03 April 2018
 */
public class WikidataAccessDao {

  public static final String WIKIDATA_ORGANIZATION_XSL_FILE = "/wkd2org.xsl";
  private static final String SPARQL = "https://query.wikidata.org/sparql";
  private static final int SIZE = 1024 * 1024;
  private Transformer transformer;

  private WikidataAccessDao(InputStreamCreator inputStreamSupplier) throws WikidataAccessException {
    try (InputStream inputStream = inputStreamSupplier.create()) {
      init(inputStream);
    } catch (IOException e) {
      throw new WikidataAccessException(
          "Unexpected exception while reading the wikidata XSLT file.", e);
    }
  }

  @FunctionalInterface
  private interface InputStreamCreator {

    InputStream create() throws IOException;
  }

  /**
   * Constructor with file argument of the required xslt.
   *
   * @param templateFile the xslt file
   * @throws WikidataAccessException if an exception occurred during initialization
   */
  public WikidataAccessDao(File templateFile) throws WikidataAccessException {
    this(() -> Files.newInputStream(templateFile.toPath()));
  }

  /**
   * Constructor with input stream argument of the required xslt.
   *
   * @param xslTemplate the xslt contents through an input stream
   * @throws WikidataAccessException if an exception occurred during initialization
   */
  public WikidataAccessDao(InputStream xslTemplate) throws WikidataAccessException {
    this(() -> xslTemplate);
  }

  /**
   * Default constructor using code default xslt path
   *
   * @throws WikidataAccessException if an exception occurred during initialization
   */
  public WikidataAccessDao() throws WikidataAccessException {
    this(() -> WikidataAccessDao.class.getResourceAsStream(WIKIDATA_ORGANIZATION_XSL_FILE));
  }

  /**
   * This method initializes classes needed for performing the required XML transformations for
   * Wikidata organizations.
   * <p>The provided {@code xslTemplate} argument is assumed to be safe content. If not make sure
   * it is for security<p/>
   *
   * @param xslTemplate the InputStream connected to the xls transformation template. This parameter
   * has to be controlled and the content should be safe for xslt transformation.
   */
  public final void init(InputStream xslTemplate) throws WikidataAccessException {
    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
      transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      Source xslt = new StreamSource(xslTemplate);
      transformer = transformerFactory.newTransformer(xslt);
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
      transformer.setParameter("deref", Boolean.TRUE);
      transformer.setParameter("address", Boolean.TRUE);

    } catch (TransformerConfigurationException e) {
      throw new WikidataAccessException(WikidataAccessException.TRANSFORMER_CONFIGURATION_ERROR, e);
    }
  }

  /**
   * This method retrieves organization RDF/XML data from Wikidata using SPARQL query and stores it
   * in XSLT/XML format in a given file applying XSLT template.
   *
   * @param uri The Wikidata URI in string format
   * @return String The Result of Wikidata query in XML format
   */
  public StringBuilder getEntity(String uri) throws WikidataAccessException {

    StringBuilder res = new StringBuilder();
    StreamResult wikidataRes = new StreamResult(new StringBuilderWriter(res));
    translate(uri, wikidataRes);
    return res;
  }

  /**
   * This method converts the XML representation from to given file into a WikidataOrganization
   * object.
   *
   * @param xmlFile The file containing the representation of Wikidata organization in XML format
   * @return Wikidata organization object
   */
  public WikidataOrganization parse(File xmlFile) throws JAXBException, IOException {
    String xml = FileUtils.readFileToString(xmlFile, StandardCharsets.UTF_8);
    return parse(xml);
  }

  /**
   * This method converts XML representation accessible through the InputStream into a Wikidata
   * organization object.
   *
   * @param xmlStream The stream accessing the XML representation of Wikidata Organization
   * @return Wikidata organization object
   */
  public WikidataOrganization parse(InputStream xmlStream) throws JAXBException, IOException {
    StringWriter writer = new StringWriter();
    IOUtils.copy(xmlStream, writer, StandardCharsets.UTF_8);
    String wikidataXml = writer.toString();

    return parse(wikidataXml);
  }


  /**
   * This method converts XML string into a Wikidata organization object.
   *
   * @param xml The Wikidata organization object in string XML format
   * @return Wikidata organization object
   */
  public WikidataOrganization parse(String xml) throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance(WikidataOrganization.class);

    Unmarshaller unmarshaller = jc.createUnmarshaller();
    InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    return (WikidataOrganization) unmarshaller.unmarshal(stream);
  }

  /**
   * This method creates SPARQL query by passed URI to Wikidata.
   *
   * @param uri The Wikidata URI in string format
   * @return RDF model
   */
  private Resource getModelFromSPARQL(String uri) throws WikidataAccessException {
    Resource resource = fetchFromSPARQL(uri);
    if (!isDuplicate(resource)) {
      return resource;
    }

    //if duplication fetch main resource
    StmtIterator iter = resource.listProperties(OWL.sameAs);
    try {
      while (iter.hasNext()) {
        String sameAs = iter.next().getResource().getURI();
        Resource r2 = fetchFromSPARQL(sameAs);
        //check if main resource
        if (!isDuplicate(r2)) {
          resource = r2;
          break;
        }
      }
    } finally {
      iter.close();
    }

    return resource;
  }

  /**
   * @return true if the retrieved resource is duplicated
   */
  private boolean isDuplicate(Resource resource) {
    return (resource != null && resource.hasProperty(OWL.sameAs) && !resource
        .hasProperty(RDFS.label));
  }

  private Resource fetchFromSPARQL(String uri) throws WikidataAccessException {
    String sDescribe = "DESCRIBE <" + uri + ">";

    Model m = ModelFactory.createDefaultModel();
    try (QueryEngineHTTP endpoint = new QueryEngineHTTP(SPARQL, sDescribe)) {
      return endpoint.execDescribe(m).getResource(uri);
    } catch (RuntimeException e) {
      throw new WikidataAccessException(
          WikidataAccessException.CANNOT_ACCESS_WIKIDATA_RESOURCE_ERROR + uri, e);
    }
  }


  /**
   * This method transforms StreamResult to XML format
   *
   * @param resource The RDF resource
   * @param res The StreamResult of Wikidata query
   */
  private void transform(Resource resource, StreamResult res)
      throws WikidataAccessException {
    synchronized (this) {

      // set rdf_about
      transformer.setParameter("rdf_about", resource.getURI());
      StringBuilder sb = new StringBuilder(SIZE);

      try (StringBuilderWriter sbw = new StringBuilderWriter(sb)) {
        Model model = resource.getModel();
        RDFWriter writer = model.getWriter("RDF/XML");
        writer.setProperty("tab", "0");
        writer.setProperty("allowBadURIs", "true");
        writer.setProperty("relativeURIs", "");
        writer.write(model, sbw, "RDF/XML");
        transformer.transform(new StreamSource(new CharSequenceReader(sb)), res);
      } catch (TransformerException e) {
        throw new WikidataAccessException(
            WikidataAccessException.TRANSFORM_WIKIDATA_TO_RDF_XML_ERROR,
            e);
      } finally {
        sb.setLength(0);
      }
    }
  }

  /**
   * This method parses wikidata organization content stored in XSLT/XML file object
   *
   * @param inputFile The file containing the wikidata
   * @return WikidataOrganization object
   */
  public WikidataOrganization parseWikidataOrganization(File inputFile)
      throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance(WikidataOrganization.class);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (WikidataOrganization) unmarshaller.unmarshal(inputFile);
  }

  /**
   * This method loads and transforms StreamResult to XML format
   *
   * @param uri The Wikidata URI in string format
   * @param res The StreamResult of Wikidata query
   */
  public void translate(String uri, StreamResult res) throws WikidataAccessException {
    Resource wikidataResource = getModelFromSPARQL(uri);
    if (wikidataResource == null || wikidataResource.getURI() == null) {
      throw new WikidataAccessException(
          WikidataAccessException.CANNOT_ACCESS_WIKIDATA_RESOURCE_ERROR + uri, null);
    }
    transform(wikidataResource, res);
  }

}
