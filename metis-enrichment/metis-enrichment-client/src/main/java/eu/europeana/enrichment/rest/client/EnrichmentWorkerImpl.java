package eu.europeana.enrichment.rest.client;

import eu.europeana.enrichment.rest.client.dereference.Dereferencer;
import eu.europeana.enrichment.rest.client.enrichment.Enricher;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs the task of dereferencing and enrichment for a given RDF document.
 */
public class EnrichmentWorkerImpl implements EnrichmentWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentWorkerImpl.class);

  private final Enricher enricher;
  private final Dereferencer dereferencer;
  private final Set<Mode> supportedModes;

  /**
   * Constructor.
   *
   * @param dereferencer The dereference service.
   * @param enricher The enrichment service.
   */
  public EnrichmentWorkerImpl(Dereferencer dereferencer, Enricher enricher) {

    this.enricher = enricher;
    this.dereferencer = dereferencer;
    supportedModes = EnumSet.noneOf(Mode.class);

    if (dereferencer != null) {
      supportedModes.add(Mode.DEREFERENCE);
    }
    if (enricher != null) {
      supportedModes.add(Mode.ENRICHMENT);
    }

  }

  @Override
  public Set<Mode> getSupportedModes() {
    return Collections.unmodifiableSet(supportedModes);
  }

  @Override
  public byte[] process(InputStream inputStream)
      throws EnrichmentException, DereferenceException, SerializationException {
    return process(inputStream, EnumSet.allOf(Mode.class));
  }

  @Override
  public byte[] process(final InputStream inputStream, Set<Mode> modes)
      throws SerializationException, EnrichmentException, DereferenceException {
    if (inputStream == null) {
      throw new IllegalArgumentException("The input stream cannot be null.");
    }
    try {
      final RDF inputRdf = convertInputStreamToRdf(inputStream);
      final RDF resultRdf = process(inputRdf, modes);
      return convertRdfToBytes(resultRdf);
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
    return process(inputString, EnumSet.allOf(Mode.class));
  }

  @Override
  public String process(final String inputString, Set<Mode> modes)
      throws SerializationException, EnrichmentException, DereferenceException {
    if (inputString == null) {
      throw new IllegalArgumentException("Input RDF string cannot be null.");
    }
    try {
      final RDF inputRdf = convertStringToRdf(inputString);
      final RDF resultRdf = process(inputRdf, modes);
      return convertRdfToString(resultRdf);
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
    return process(inputRdf, EnumSet.allOf(Mode.class));
  }

  @Override
  public RDF process(final RDF rdf, Set<Mode> modes)
      throws EnrichmentException, DereferenceException {

    // Sanity checks
    if (rdf == null) {
      throw new IllegalArgumentException("Input RDF cannot be null.");
    }
    if (modes == null) {
      throw new IllegalArgumentException("Set of Modes cannot be null.");
    }
    if (!getSupportedModes().containsAll(modes)) {
      throw new IllegalArgumentException(
          "The requested mode(s) is not supported by this instance.");
    }

    // Preparation
    LOGGER.info("Received RDF for enrichment/dereferencing. Mode: {}", modes);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Processing RDF:\n{}", convertRdfToStringForLogging(rdf));
    }

    // Dereferencing first: this is because we may enrich based on its results.
    if (modes.contains(Mode.DEREFERENCE)) {
      LOGGER.debug("Performing dereferencing...");
      dereferencer.dereference(rdf);
      LOGGER.debug("Dereferencing completed.");
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("RDF after dereferencing:\n{}", convertRdfToStringForLogging(rdf));
      }
    }

    // Enrichment second: we use the result of dereferencing as well.
    if (modes.contains(Mode.ENRICHMENT)) {
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
    } catch (SerializationException e) {
      LOGGER.warn("Exception occurred while rendering an RDF document as a String.", e);
      return "[COULD NOT RENDER RDF]";
    }
  }


  String convertRdfToString(RDF rdf) throws SerializationException {
    return RdfConversionUtils.convertRdfToString(rdf);
  }

  byte[] convertRdfToBytes(RDF rdf) throws SerializationException {
    return RdfConversionUtils.convertRdfToBytes(rdf);
  }

  RDF convertStringToRdf(String xml) throws SerializationException {
    return RdfConversionUtils.convertStringToRdf(xml);
  }

  RDF convertInputStreamToRdf(InputStream xml) throws SerializationException {
    return RdfConversionUtils.convertInputStreamToRdf(xml);
  }
}
