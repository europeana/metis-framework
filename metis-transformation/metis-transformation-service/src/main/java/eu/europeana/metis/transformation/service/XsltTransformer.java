package eu.europeana.metis.transformation.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.time.Duration;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.metis.transformation.service.CacheValueSupplier.CacheValueSupplierException;
import net.sf.saxon.TransformerFactoryImpl;

/**
 * This class performs XSL transforms (XSLT). Instances of this class are <b>not thread-safe</b>.
 * For each thread a new instance needs to be created, but, due to the caching mechanism of the XSLT
 * compilation, this operation is not very expensive.
 */
public class XsltTransformer {

  private static final Logger LOGGER = LoggerFactory.getLogger(XsltTransformer.class);

  private static final CacheWithExpirationTime<String, Templates> TEMPLATES_CACHE =
      new CacheWithExpirationTime<>();

  private final Transformer transformer;

  /**
   * Constructor in the case that no value of the datasetId field needs to be set.
   *
   * @param xsltUrl The URL of the XSLT file.
   * @throws TransformationException In case there was a problem setting up the transformation.
   */
  public XsltTransformer(String xsltUrl) throws TransformationException {
    this(xsltUrl, null, null, null);
  }

  /**
   * Constructor.
   *
   * @param xsltUrl The URL of the XSLT file.
   * @param datasetName the dataset name related to the dataset
   * @param edmCountry the Country related to the dataset
   * @param edmLanguage the language related to the dataset
   * @throws TransformationException In case there was a problem with setting up the transformation.
   */
  public XsltTransformer(String xsltUrl, String datasetName, String edmCountry, String edmLanguage)
      throws TransformationException {
    try {
      this.transformer = getTemplates(xsltUrl).newTransformer();
    } catch (TransformerConfigurationException | CacheValueSupplierException e) {
      LOGGER.error("Exception during transformation setup", e);
      throw new TransformationException(e);
    }
    if (datasetName != null && !datasetName.trim().isEmpty()) {
      transformer.setParameter("datasetName", datasetName);
    }
    if (edmLanguage != null && !edmLanguage.trim().isEmpty()) {
      transformer.setParameter("edmLanguage", edmLanguage);
    }
    if (edmCountry != null && !edmCountry.trim().isEmpty()) {
      transformer.setParameter("edmCountry", edmCountry);
    }
  }

  /**
   * Transforms a file using this instance's XSL transformation.
   *
   * @param fileContent The file to be transformed.
   * @param europeanaGeneratedIdsMap all the identifiers related to europeana RDF elements
   * @return The transformed file.
   * @throws TransformationException In case there was a problem with the transformation.
   */
  public StringWriter transform(byte[] fileContent,
      EuropeanaGeneratedIdsMap europeanaGeneratedIdsMap) throws TransformationException {
    if (europeanaGeneratedIdsMap != null) {
      transformer.setParameter("providedCHOAboutId",
          europeanaGeneratedIdsMap.getEuropeanaGeneratedId());
      transformer.setParameter("aggregationAboutId",
          europeanaGeneratedIdsMap.getAggregationAboutPrefixed());
      transformer.setParameter("europeanaAggregationAboutId",
          europeanaGeneratedIdsMap.getEuropeanaAggregationAboutPrefixed());
      transformer.setParameter("proxyAboutId", europeanaGeneratedIdsMap.getProxyAboutPrefixed());
      transformer.setParameter("europeanaProxyAboutId",
          europeanaGeneratedIdsMap.getEuropeanaProxyAboutPrefixed());
      transformer.setParameter("dcIdentifier",
          europeanaGeneratedIdsMap.getSourceProvidedChoAbout());
    }
    try (final InputStream contentStream = new ByteArrayInputStream(fileContent)) {
      final StringWriter result = new StringWriter();
      transformer.transform(new StreamSource(contentStream), new StreamResult(result));
      return result;
    } catch (TransformerException | IOException e) {
      LOGGER.error("Exception during transformation", e);
      throw new TransformationException(e);
    }
  }

  private static Templates getTemplates(String xsltUrl) throws CacheValueSupplierException {
    return TEMPLATES_CACHE.getFromCache(xsltUrl, () -> createTemplatesFromUrl(xsltUrl));
  }

  private static Templates createTemplatesFromUrl(String xsltUrl)
      throws CacheValueSupplierException {
    final TransformerFactory transformerFactory = new TransformerFactoryImpl();
    try (final InputStream xsltStream = new URL(xsltUrl).openStream()) {
      return transformerFactory.newTemplates(new StreamSource(xsltStream));
    } catch (IOException | TransformerConfigurationException e) {
      throw new CacheValueSupplierException(e);
    }
  }

  /**
   * Set a new expiration time for the internal XSLT cache by calling
   * {@link CacheWithExpirationTime#setExpirationTime(Duration)}.
   * 
   * @param expirationTime The new expiration time.
   */
  public static void setExpirationTime(Duration expirationTime) {
    TEMPLATES_CACHE.setExpirationTime(expirationTime);
  }

  /**
   * Set a new leniency mode for the internal XSLT cache by calling
   * {@link CacheWithExpirationTime#setLenientWithReloads(boolean)}.
   * 
   * @param lenientWithReloads The new leniency mode.
   */
  public static void setLenientWithReloads(boolean lenientWithReloads) {
    TEMPLATES_CACHE.setLenientWithReloads(lenientWithReloads);
  }

  /**
   * Clean up the internal XSLT cache by calling
   * {@link CacheWithExpirationTime#removeItemsNotAccessedSince(Duration)}.
   * 
   * @param since The interval length of the period we want to check (which ends now). A negative
   *        duration cleans everything.
   */
  public static void removeItemsNotAccessedSince(Duration since) {
    TEMPLATES_CACHE.removeItemsNotAccessedSince(since);
  }
}
