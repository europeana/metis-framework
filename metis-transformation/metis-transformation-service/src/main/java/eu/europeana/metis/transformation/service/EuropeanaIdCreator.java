package eu.europeana.metis.transformation.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
 */
public final class EuropeanaIdCreator {

  private static final Logger LOGGER = LoggerFactory.getLogger(EuropeanaIdCreator.class);
  
  private static final int EVALUATE_XPATH_ATTEMPT_COUNT = 20;
  private static final int EVALUATE_XPATH_ATTEMPT_INTERVAL_IN_MS = 50;

  private static final String RDF_NAMESPACE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  private static final String RDF_NAMESPACE_PREFIX = "rdf";

  private static final String EDM_NAMESPACE_URI = "http://www.europeana.eu/schemas/edm/";
  private static final String EDM_NAMESPACE_PREFIX = "edm";

  private static final String RDF_ABOUT_EXPRESSION =
      String.format("/%s:RDF/%s:ProvidedCHO[1]/@%s:about", RDF_NAMESPACE_PREFIX,
          EDM_NAMESPACE_PREFIX, RDF_NAMESPACE_PREFIX);

  private static final Pattern LEGACY_COLLECTION_ID_PATTERN = Pattern.compile("[a-zA-Z]");
  private static final Pattern LEGACY_RDF_ABOUT_REPLACE_PATTERN = Pattern.compile("[^a-zA-Z0-9_]");
  private static final String REPLACEMENT_CHARACTER = "_";

  private static final Supplier<EuropeanaIdException> ID_NOT_FOUND_EXCEPTION_SUPPLIER =
      () -> new EuropeanaIdException("Could not find provider ID in source.");

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
   * This method constructs a Europeana ID for an RDF and provides a map for the Provider ID and
   * Europeana ID.
   *
   * @param rdf The RDF. Is not null.
   * @param datasetId The ID of the dataset to which this RDF belongs. Is not null.
   * @return The Europeana ID and Provider ID in a class structure, of this RDF. Is not null.
   * @throws EuropeanaIdException In case no rdf:about could be found.
   */
  public EuropeanaGeneratedIdsMap constructEuropeanaId(RDF rdf, String datasetId)
      throws EuropeanaIdException {
    final String rdfAbout = extractRdfAboutFromRdf(rdf);
    String europeanaIdFromRdfAbout = constructEuropeanaIdFromRdfAbout(rdfAbout, datasetId);
    return new EuropeanaGeneratedIdsMap(rdfAbout, europeanaIdFromRdfAbout);
  }

  /**
   * This method constructs a Europeana ID for an RDF represented as a string and provides a map for
   * the ProvidedCHO rdf:about and Europeana ID. If the rdfAbout is already a europeana identifier
   * then there will not be a generation of the Europeana ID but a copy of the ProvidedCHO
   * rdf:about.
   *
   * @param rdfString The RDF as a string. Is not null.
   * @param datasetId The ID of the dataset to which this RDF belongs. Is not null.
   * @return The Europeana ID and Provider ID in a class structure, of this RDF. Is not null.
   * @throws EuropeanaIdException In case no rdf:about could be found.
   */
  public EuropeanaGeneratedIdsMap constructEuropeanaId(String rdfString, String datasetId)
      throws EuropeanaIdException {
    final String rdfAbout = extractRdfAboutFromRdfString(rdfString);
    String europeanaIdFromRdfAbout = constructEuropeanaIdFromRdfAbout(rdfAbout, datasetId);
    return new EuropeanaGeneratedIdsMap(rdfAbout, europeanaIdFromRdfAbout);
  }

  private String constructEuropeanaIdFromRdfAbout(String rdfAbout, String datasetId) {
    return "/" + sanitizeDatasetIdLegacy(datasetId) + "/" + sanitizeRdfAboutLegacy(rdfAbout);
  }

  /**
   * <p>
   * This method sanitizes the RDF about for inclusion in the Europeana record ID. This is the
   * legacy method (the UIM way). This sanitization consists of two steps (in the order given here):
   * <ol>
   * <li><b>Shortening</b> the string, removing some structure in the case it is a URI.</li>
   * <li><b>Normalizing</b> the characters, replacing unsupported ones by a default
   * placeholder.</li>
   * </ol>
   * </p>
   * <p>
   * The rules for <b>shortening</b> are as follows:
   * <ul>
   * <li>If the input represents a link starting with <code>http://</code> then everything after the
   * first <code>/</code> that follows that prefix will be kept, the rest will be discarded. For
   * instance, the URI <code> http://www.europeana.eu/somepath/someid </code> would become
   * <code>somepath/someid</code>.</li>
   * <li>As a special case of the previous rule, if there is no <code>/</code> after the prefix (for
   * instance for the URI <code>http://someid</code>), the empty string will be returned.</li>
   * <li>In all other cases shortening has no effect: the entire string is kept.</li>
   * </ul>
   * </p>
   * <p>
   * The rules for <b>normalizing</b> are as follows. Every character that is not a digit or a Roman
   * letter without diacritics (lower case or upper case) will be replaced by an underscore ('_').
   * </p>
   *
   * @param rdfAbout The string to be sanitized.
   * @return The sanitized string.
   */
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

  /**
   * This method sanitizes the collection ID for inclusion in the Europeana record ID. This is the
   * legacy method (the UIM way). This sanitization consists of merely checking the last character
   * of the ID.
   * <ul>
   * <li>If it is a Roman letter without diacritics it will be removed from the collection ID and
   * the rest will be returned.</li>
   * <li>If it is any other character, the collection ID will be returned without any changes.</li>
   * </ul>
   *
   * @param collectionId The collection ID to be sanitized.
   * @return The sanitized collection ID.
   */
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

    // Obtain the RDF about
    final String result;
    try (final InputStream inputStream =
        new ByteArrayInputStream(rdfString.getBytes(StandardCharsets.UTF_8))) {
      result = extractRdfAboutFromInputStream(inputStream);
    } catch (InterruptedException e) {
      LOGGER.warn("Thread interrupted.");
      Thread.currentThread().interrupt();
      return null;
    } catch (IOException e) {
      throw new EuropeanaIdException(
          "Something went wrong while extracting the provider ID from the source.", e);
    }

    // Check it for presence before returning it.
    if (result == null || result.trim().isEmpty()) {
      throw ID_NOT_FOUND_EXCEPTION_SUPPLIER.get();
    }
    return result;
  }

  /*
   * TODO Note: the synchronized block around the actual call to the RDF about extractor is
   * necessary to solve issue MET-1258. It is currently expected that somewhere in the apache xerces
   * implementation there is a threading issue that causes exceptions even if individual threads are
   * calling privately owned instances of this class. Eventually an upgrade to a later version of
   * the apache libraries or a move towards another parser would be needed to solve this issue
   * permanently.
   * TODO This is also why the loop with the attempts was added.
   */
  private String extractRdfAboutFromInputStream(InputStream inputStream)
      throws EuropeanaIdException, InterruptedException {
    
    // Keep track of the latest exception received.
    XPathExpressionException xpathException = null;
    
    // Try a number of times.
    for (int i = 0; i < EVALUATE_XPATH_ATTEMPT_COUNT; i++) {
      
      // Sleep first to give a race condition time to resolve itself (except during the first run).
      if (i > 0) {
        Thread.sleep(EVALUATE_XPATH_ATTEMPT_INTERVAL_IN_MS);
      }
      
      // Make sure that no other thread in this JVM goes here at the same time.
      synchronized (EuropeanaIdCreator.class) {
        try {
          
          // Attempt evaluation of XPath.
          return (String) rdfAboutExtractor.evaluate(new InputSource(inputStream),
              XPathConstants.STRING);
        } catch (XPathExpressionException e) {
          if (isRaceCondition(e)) {
            
            // Handle exception that is caused by a race condition: remember it and try again.
            LOGGER.warn("Race condition error occurred during attempt {} of {}. Trying again...", i,
                EVALUATE_XPATH_ATTEMPT_COUNT, e);
            xpathException = e;
          } else {
            
            // Handle unexpected exception that is not caused by a race condition: re-throw.
            throw new EuropeanaIdException(
                "Something went wrong while extracting the provider ID from the source.", e);
          }
        }
      }
    }
    
    // If we are here, it is because all attempts have failed. Re-throw the latest exception.
    throw new EuropeanaIdException("Last XPath evaluation attempt generated exception.",
        xpathException);
  }
  
  private static boolean isRaceCondition(Exception exception) {
    
    // Check for the thread collision exception
    if (exception.getMessage() != null
        && exception.getMessage().contains("FWK005 parse may not be called while parsing")) {
      return true;
    }

    // Check for the specific null pointer exception
    if (exception.getCause() instanceof NullPointerException) {
      final StackTraceElement[] stackTrace = exception.getCause().getStackTrace();
      final StackTraceElement firstMethod = stackTrace.length > 0 ? stackTrace[0] : null;
      if (firstMethod != null
          && "org.apache.xerces.parsers.AbstractDOMParser".equals(firstMethod.getClassName())
          && "characters".equals(firstMethod.getMethodName())) {
        return true;
      }
    }

    // So it is not
    return false;
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
