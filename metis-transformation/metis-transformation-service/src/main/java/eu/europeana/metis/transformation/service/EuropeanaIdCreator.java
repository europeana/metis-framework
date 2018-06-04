package eu.europeana.metis.transformation.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import eu.europeana.corelib.definitions.jibx.ProvidedCHOType;
import eu.europeana.corelib.definitions.jibx.RDF;

/**
 * An instance of this class can be used to create Europeana IDs for RDF records. This class is
 * <b>not thread-safe</b>: this means that instances should not be shared between different threads.
 * 
 * @author jochen
 *
 */
public final class EuropeanaIdCreator {

  private static final Logger LOGGER = LoggerFactory.getLogger(EuropeanaIdCreator.class);

  private static final String RDF_NAMESPACE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  private static final String RDF_NAMESPACE_PREFIX = "rdf";

  private static final String EDM_NAMESPACE_URI = "http://www.europeana.eu/schemas/edm/";
  private static final String EDM_NAMESPACE_PREFIX = "edm";

  private static final String RDF_ABOUT_EXPRESSION = "/" + RDF_NAMESPACE_PREFIX + ":RDF/"
      + EDM_NAMESPACE_PREFIX + ":ProvidedCHO[1]/@" + RDF_NAMESPACE_PREFIX + ":about";

  private static final Pattern LEGACY_COLLECTION_ID_PATTERN = Pattern.compile("[a-zA-Z]");
  private static final Pattern LEGACY_RDF_ABOUT_REPLACE_PATTERN = Pattern.compile("[^a-zA-Z0-9_]");
  private static final String REPLACEMENT_CHARACTER = "_";

  private static final Supplier<EuropeanaIdException> ID_NOT_FOUND_EXCEPTION_SUPPLIER =
      () -> new EuropeanaIdException("Could not find provider ID in source.");

  /**
   * The modes that are supported for Europeana ID creation.
   */
  public enum Mode {

    /** This is the current, recommended mode. **/
    REGULAR,

    /** This is the legacy mode: records may exist with this type of Europeana ID. **/
    LEGACY
  }

  private final XPathExpression rdfAboutExtractor;

  /**
   * Constructor.
   * 
   * @throws EuropeanaIdException In case of problems setting the RDF about extractor.
   */
  public EuropeanaIdCreator() throws EuropeanaIdException {
    final XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(new RdfNamespaceResolver());
    try {
      rdfAboutExtractor = xpath.compile(RDF_ABOUT_EXPRESSION);
    } catch (XPathExpressionException e) {
      LOGGER.warn("Something went wrong while setting up the xpath.", e);
      throw new EuropeanaIdException("Something went wrong while setting up the xpath.", e);
    }
  }

  /**
   * This method constructs a Europeana ID for an RDF.
   * 
   * @param rdf The RDF. Is not null.
   * @param datasetId The ID of the dataset to which this RDF belongs. Is not null.
   * @param mode The mode for Europeana ID creation. Is not null.
   * @return The Europeana ID of this RDF. Is not null.
   * @throws EuropeanaIdException In case no rdf:about could be found.
   */
  public String constructEuropeanaId(RDF rdf, String datasetId, Mode mode)
      throws EuropeanaIdException {
    final String rdfAbout = extractRdfAboutFromRdf(rdf);
    return constructEuropeanaIdFromRdfAbout(rdfAbout, datasetId, mode);
  }

  /**
   * This method constructs a Europeana ID for an RDF represented as a string.
   * 
   * @param rdfString The RDF as a string. Is not null.
   * @param datasetId The ID of the dataset to which this RDF belongs. Is not null.
   * @param mode The mode for Europeana ID creation. Is not null.
   * @return The Europeana ID of this RDF. Is not null.
   * @throws EuropeanaIdException In case no rdf:about could be found.
   */
  public String constructEuropeanaId(String rdfString, String datasetId, Mode mode)
      throws EuropeanaIdException {
    final String rdfAbout = extractRdfAboutFromRdfString(rdfString);
    return constructEuropeanaIdFromRdfAbout(rdfAbout, datasetId, mode);
  }

  private String constructEuropeanaIdFromRdfAbout(String rdfAbout, String datasetId, Mode mode) {
    // TODO create regular sanitizing.
    final String sanitizedDatasetId = mode == Mode.LEGACY ? sanitizeDatasetIdLegacy(datasetId)
        : sanitizeDatasetIdLegacy(datasetId);
    // TODO create regular sanitizing.
    final String sanitizedRdfAbout =
        mode == Mode.LEGACY ? sanitizeRdfAboutLegacy(rdfAbout) : sanitizeRdfAboutLegacy(rdfAbout);
    return "/" + sanitizedDatasetId + "/" + sanitizedRdfAbout;
  }

  private static String sanitizeRdfAboutLegacy(final String rdfAbout) {
    final String recordId = rdfAbout.startsWith("http://")
        ? substringAfterLegacy(substringAfterLegacy(rdfAbout, "http://"), "/")
        : rdfAbout;
    final Matcher matcher = LEGACY_RDF_ABOUT_REPLACE_PATTERN.matcher(recordId);
    return matcher.replaceAll(REPLACEMENT_CHARACTER);
  }

  private static String substringAfterLegacy(String str, String separator) {
    final int pos = str.indexOf(separator);
    if (pos < 0) {
      return "";
    }
    return str.substring(pos + separator.length());
  }

  private static String sanitizeDatasetIdLegacy(String collectionId) {
    final Matcher matcher = LEGACY_COLLECTION_ID_PATTERN
        .matcher(collectionId.substring(collectionId.length() - 1, collectionId.length()));
    return matcher.find() ? collectionId.substring(0, collectionId.length() - 1) : collectionId;
  }

  private String extractRdfAboutFromRdf(RDF rdf) throws EuropeanaIdException {
    final String result = rdf.getProvidedCHOList().stream().filter(Objects::nonNull).findFirst()
        .map(ProvidedCHOType::getAbout).orElse(null);
    if (result == null || result.trim().isEmpty()) {
      throw ID_NOT_FOUND_EXCEPTION_SUPPLIER.get();
    }
    return result;
  }

  private String extractRdfAboutFromRdfString(String rdfString) throws EuropeanaIdException {

    // Obtain the rdf about
    final String result;
    try {
      final InputStream inputStream =
          new ByteArrayInputStream(rdfString.getBytes(StandardCharsets.UTF_8));
      result =
          (String) rdfAboutExtractor.evaluate(new InputSource(inputStream), XPathConstants.STRING);
    } catch (XPathExpressionException e) {
      throw new EuropeanaIdException(
          "Something went wrong while extracting the provider ID from the source.", e);
    }

    // Check it for presence before returning it.
    if (result == null || result.trim().isEmpty()) {
      throw ID_NOT_FOUND_EXCEPTION_SUPPLIER.get();
    }
    return result;
  }

  private static final class RdfNamespaceResolver implements NamespaceContext {

    @Override
    public String getNamespaceURI(String prefix) {
      if (prefix == null) {
        throw new IllegalArgumentException();
      }
      final String result;
      switch (prefix) {
        case RDF_NAMESPACE_PREFIX:
          result = RDF_NAMESPACE_URI;
          break;
        case EDM_NAMESPACE_PREFIX:
          result = EDM_NAMESPACE_URI;
          break;
        default:
          result = XMLConstants.NULL_NS_URI;
          break;
      }
      return result;
    }

    @Override
    public String getPrefix(String uri) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<String> getPrefixes(String uri) {
      throw new UnsupportedOperationException();
    }
  }
}
