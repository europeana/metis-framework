package eu.europeana.metis.transformation.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sf.saxon.TransformerFactoryImpl;

/**
 * This class performs XSL transforms (XSLT). Instances of this class are <b>not thread-safe</b>.
 * For each thread a new instance needs to be created, but, due to the caching mechanism of the XSLT
 * compilation, this operation is not very expensive.
 */
public class XsltTransformer {

  private static final Logger LOGGER = LoggerFactory.getLogger(XsltTransformer.class);

  private static final int CACHE_SIZE = 50;
  private static final LRUCache<String, Templates> TEMPLATES_CACHE = new LRUCache<>(CACHE_SIZE);

  private final Transformer transformer;

  /**
   * Constructor in the case that no value of the datasetId field needs to be set.
   *
   * @param xsltUrl The URL of the XSLT file.
   * @throws TransformationException In case there was a problem setting up the transformation.
   */
  public XsltTransformer(String xsltUrl) throws TransformationException {
    this(xsltUrl, null, null, null, null);
  }

  /**
   * Constructor.
   *
   * @param xsltUrl The URL of the XSLT file.
   * @param europeanaGeneratedIdsMap all the identifiers related to europeana RDF elements
   * @param datasetName the dataset name related to the dataset
   * @param edmCountry the Country related to the dataset
   * @param edmLanguage the language related to the dataset
   * @throws TransformationException In case there was a problem with setting up the transformation.
   */
  public XsltTransformer(String xsltUrl, EuropeanaGeneratedIdsMap europeanaGeneratedIdsMap,
      String datasetName, String edmCountry, String edmLanguage)
      throws TransformationException {
    try {
      this.transformer = getTemplates(xsltUrl).newTransformer();
    } catch (TransformerConfigurationException | IOException e) {
      LOGGER.error("Exception during transformation setup", e);
      throw new TransformationException(e);
    }
    if (europeanaGeneratedIdsMap != null) {
      transformer
          .setParameter("providedCHOAboutId", europeanaGeneratedIdsMap.getEuropeanaGeneratedId());
      transformer
          .setParameter("aggregationAboutId",
              europeanaGeneratedIdsMap.getAggregationAboutPrefixed());
      transformer.setParameter("europeanaAggregationAboutId",
          europeanaGeneratedIdsMap.getEuropeanaAggregationAboutPrefixed());
      transformer.setParameter("proxyAboutId", europeanaGeneratedIdsMap.getProxyAboutPrefixed());
      transformer.setParameter("europeanaProxyAboutId",
          europeanaGeneratedIdsMap.getEuropeanaProxyAboutPrefixed());
      transformer.setParameter("dcIdentifier", europeanaGeneratedIdsMap.getSourceProvidedChoAbout());
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
   * @return The transformed file.
   * @throws TransformationException In case there was a problem with the transformation.
   */
  public StringWriter transform(byte[] fileContent) throws TransformationException {
    try (final InputStream contentStream = new ByteArrayInputStream(fileContent)) {
      final StringWriter result = new StringWriter();
      transformer.transform(new StreamSource(contentStream), new StreamResult(result));
      return result;
    } catch (TransformerException | IOException e) {
      LOGGER.error("Exception during transformation", e);
      throw new TransformationException(e);
    }
  }

  private static Templates getTemplates(String xsltUrl)
      throws TransformerConfigurationException, IOException {

    // Check if the cache already contains this URL.
    synchronized (TEMPLATES_CACHE) {
      if (TEMPLATES_CACHE.containsKey(xsltUrl)) {
        return TEMPLATES_CACHE.get(xsltUrl);
      }
    }

    // If it doesn't, resolve the URL and create the compiled transformation.
    final TransformerFactory transformerFactory = new TransformerFactoryImpl();
    final Templates templates;
    try (final InputStream xsltStream = new URL(xsltUrl).openStream()) {
      templates = transformerFactory.newTemplates(new StreamSource(xsltStream));
    }

    // Save it in the cache.
    synchronized (TEMPLATES_CACHE) {
      TEMPLATES_CACHE.put(xsltUrl, templates);
    }
    return templates;
  }
}
