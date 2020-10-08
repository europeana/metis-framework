package eu.europeana.enrichment.rest.client;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.rest.client.dereference.DereferenceClient;
import eu.europeana.enrichment.rest.client.dereference.Dereferencer;
import eu.europeana.enrichment.rest.client.dereference.DereferencerImpl;
import eu.europeana.enrichment.rest.client.enrichment.Enricher;
import eu.europeana.enrichment.rest.client.enrichment.EnricherImpl;
import eu.europeana.enrichment.rest.client.enrichment.EnrichmentClient;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.rest.client.exceptions.SerializationException;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.RdfConversionUtils;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs the task of dereferencing and enrichment for a given RDF document.
 */
public class EnrichmentWorkerImpl implements EnrichmentWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentWorkerImpl.class);

  private final EnrichmentClient enrichmentClient;
  private final DereferenceClient dereferenceClient;
  private final EntityMergeEngine entityMergeEngine;
  private final Set<Mode> supportedModes;

  /**
   * Constructor.
   *
   * @param dereferenceClient The dereference client.
   * @param enrichmentClient The enrichment client.
   * @param entityMergeEngine The engine to be used for merging entities into the RDF.
   */
  public EnrichmentWorkerImpl(DereferenceClient dereferenceClient,
      EnrichmentClient enrichmentClient,
      EntityMergeEngine entityMergeEngine) {
    this.dereferenceClient = dereferenceClient;
    this.enrichmentClient = enrichmentClient;
    this.entityMergeEngine = entityMergeEngine;
    supportedModes = EnumSet.noneOf(Mode.class);
    if (dereferenceClient != null) {
      supportedModes.add(Mode.DEREFERENCE_ONLY);
    }
    if (enrichmentClient != null) {
      supportedModes.add(Mode.ENRICHMENT_ONLY);
    }
    if (enrichmentClient != null && dereferenceClient != null) {
      supportedModes.add(Mode.DEREFERENCE_AND_ENRICHMENT);
    }
  }

  @Override
  public Set<Mode> getSupportedModes() {
    return Collections.unmodifiableSet(supportedModes);
  }

  @Override
  public byte[] process(InputStream inputStream)
      throws EnrichmentException, DereferenceException, SerializationException {
    return process(inputStream, Mode.DEREFERENCE_AND_ENRICHMENT);
  }

  @Override
  public byte[] process(final InputStream inputStream, Mode mode)
      throws SerializationException, EnrichmentException, DereferenceException {
    if (inputStream == null) {
      throw new IllegalArgumentException("The input stream cannot be null.");
    }
    try {
      final RDF inputRdf = convertInputStreamToRdf(inputStream);
      final RDF resultRdf = process(inputRdf, mode);
      return convertRdfToBytes(resultRdf);
    } catch (JiBXException e) {
      throw new SerializationException(
          "Something went wrong with converting to or from the RDF format.", e);
    } catch (EnrichmentException e){
      throw new EnrichmentException(
          "Something went wrong with the enrichment from the RDF file.", e);
    } catch (DereferenceException e){
      throw new DereferenceException(
          "Something went wrong with the dereference from the RDF file.", e);
    }
  }

  @Override
  public String process(String inputString)
      throws EnrichmentException, DereferenceException, SerializationException {
    return process(inputString, Mode.DEREFERENCE_AND_ENRICHMENT);
  }

  @Override
  public String process(final String inputString, Mode mode)
      throws SerializationException, EnrichmentException, DereferenceException {
    if (inputString == null) {
      throw new IllegalArgumentException("Input RDF string cannot be null.");
    }
    try {
      final RDF inputRdf = convertStringToRdf(inputString);
      final RDF resultRdf = process(inputRdf, mode);
      return convertRdfToString(resultRdf);
    } catch (JiBXException e) {
      throw new SerializationException(
          "Something went wrong with converting to or from the RDF format.", e);
    } catch (EnrichmentException e){
      throw new EnrichmentException(
          "Something went wrong with the enrichment from the RDF file.", e);
    } catch (DereferenceException e){
      throw new DereferenceException(
          "Something went wrong with the dereference from the RDF file.", e);
    }
  }

  @Override
  public RDF process(final RDF inputRdf)
      throws  EnrichmentException, DereferenceException {
    return process(inputRdf, Mode.DEREFERENCE_AND_ENRICHMENT);
  }

  @Override
  public RDF process(final RDF rdf, Mode mode)
      throws EnrichmentException, DereferenceException {

    // Sanity checks
    if (rdf == null) {
      throw new IllegalArgumentException("Input RDF cannot be null.");
    }
    if (mode == null) {
      throw new IllegalArgumentException("Mode cannot be null.");
    }
    if (!getSupportedModes().contains(mode)) {
      throw new IllegalArgumentException(
          "The requested mode '" + mode.name() + "' is not supported by this instance.");
    }

    // Preparation
    LOGGER.info("Received RDF for enrichment/dereferencing. Mode: {}", mode);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Processing RDF:\n{}", convertRdfToStringForLogging(rdf));
    }

    Enricher enricher = new EnricherImpl(entityMergeEngine, enrichmentClient);
    Dereferencer dereferencer = new DereferencerImpl(entityMergeEngine, enrichmentClient, dereferenceClient);

    // Dereferencing first: this is because we may enrich based on its results.
    if (Mode.DEREFERENCE_AND_ENRICHMENT == mode || Mode.DEREFERENCE_ONLY == mode) {
      LOGGER.debug("Performing dereferencing...");
      dereferencer.dereference(rdf);
      LOGGER.debug("Dereferencing completed.");
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("RDF after dereferencing:\n{}", convertRdfToStringForLogging(rdf));
      }
    }

    // Enrichment second: we use the result of dereferencing as well.
    if (Mode.DEREFERENCE_AND_ENRICHMENT == mode || Mode.ENRICHMENT_ONLY == mode) {
      LOGGER.debug("Performing enrichment...");
      enricher.enrichment(rdf);
      LOGGER.debug("Enrichment completed.");
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("RDF after enrichment:\n{}", convertRdfToStringForLogging(rdf));
      }
    }

    // Done
    LOGGER.debug("Processing complete.");
    return rdf;
  }

  private String convertRdfToStringForLogging(final RDF rdf) {
    try {
      return convertRdfToString(rdf);
    } catch (JiBXException e) {
      LOGGER.warn("Exception occurred while rendering an RDF document as a String.", e);
      return "[COULD NOT RENDER RDF]";
    }
  }


  String convertRdfToString(RDF rdf) throws JiBXException {
    return RdfConversionUtils.convertRdfToString(rdf);
  }

  byte[] convertRdfToBytes(RDF rdf) throws JiBXException {
    return RdfConversionUtils.convertRdfToBytes(rdf);
  }

  RDF convertStringToRdf(String xml) throws JiBXException {
    return RdfConversionUtils.convertStringToRdf(xml);
  }

  RDF convertInputStreamToRdf(InputStream xml) throws JiBXException {
    return RdfConversionUtils.convertInputStreamToRdf(xml);
  }
}
