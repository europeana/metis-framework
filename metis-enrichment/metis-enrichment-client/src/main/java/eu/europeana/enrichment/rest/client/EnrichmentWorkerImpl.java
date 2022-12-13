package eu.europeana.enrichment.rest.client;

import eu.europeana.enrichment.rest.client.dereference.Dereferencer;
import eu.europeana.enrichment.rest.client.enrichment.Enricher;
import eu.europeana.enrichment.rest.client.report.ProcessedResult;
import eu.europeana.enrichment.rest.client.report.ReportMessage;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs the task of dereferencing and enrichment for a given RDF document.
 */
public class EnrichmentWorkerImpl implements EnrichmentWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentWorkerImpl.class);
  private static final RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();

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
  public ProcessedResult<byte[]> process(InputStream inputStream) {
    return process(inputStream, EnumSet.allOf(Mode.class));
  }

  @Override
  public ProcessedResult<byte[]> process(final InputStream inputStream, Set<Mode> modes) {
    ProcessedResult<byte[]> result;
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    if (inputStream == null) {
      IllegalArgumentException e = new IllegalArgumentException("The input stream cannot be null.");
      reportMessages.add(ReportMessage
          .buildEnrichmentError()
          .withException(e)
          .build());
      result = new ProcessedResult<>(null, reportMessages);
    } else {
      try {
        final RDF inputRdf = convertInputStreamToRdf(inputStream);
        result = new ProcessedResult<>(convertRdfToBytes(process(inputRdf, modes)));
        return result;
      } catch (SerializationException e) {
        reportMessages.add(ReportMessage
            .buildEnrichmentError()
            .withException(e)
            .build());
        result = new ProcessedResult<>(null, reportMessages);
      }
    }
    return result;
  }

  @Override
  public ProcessedResult<String> process(String inputString) {
    return process(inputString, EnumSet.allOf(Mode.class));
  }

  @Override
  public ProcessedResult<String> process(final String inputString, Set<Mode> modes) {
    ProcessedResult<String> result;
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    if (inputString == null) {
      IllegalArgumentException e = new IllegalArgumentException("Input RDF string cannot be null.");
      reportMessages.add(ReportMessage
          .buildEnrichmentError()
          .withValue(inputString)
          .withException(e)
          .build());
      result = new ProcessedResult<>(null, reportMessages);
    } else {
      try {
        final RDF inputRdf = convertStringToRdf(inputString);
        result = new ProcessedResult<>(convertRdfToString(process(inputRdf, modes)));
      } catch (SerializationException e) {
        reportMessages.add(ReportMessage
            .buildEnrichmentError()
            .withValue(inputString)
            .withException(e)
            .build());
        result = new ProcessedResult<>(null, reportMessages);
      }
    }
    return result;
  }

  @Override
  public ProcessedResult<RDF> process(final RDF inputRdf) {
    return process(inputRdf, EnumSet.allOf(Mode.class));
  }

  @Override
  public ProcessedResult<RDF> process(final RDF rdf, Set<Mode> modes) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    IllegalArgumentException e;
    // Sanity checks
    if (rdf == null) {
      e = new IllegalArgumentException("Input RDF cannot be null.");
      reportMessages.add(ReportMessage
          .buildEnrichmentError()
          .withException(e)
          .build());
      return new ProcessedResult<>(null, reportMessages);
    }
    if (modes == null) {
      e = new IllegalArgumentException("Set of Modes cannot be null.");
      reportMessages.add(ReportMessage
          .buildEnrichmentError()
          .withException(e)
          .build());
      return new ProcessedResult<>(null, reportMessages);
    }
    if (!getSupportedModes().containsAll(modes)) {
      e = new IllegalArgumentException("The requested mode(s) is not supported by this instance.");
      reportMessages.add(ReportMessage
          .buildEnrichmentError()
          .withException(e)
          .build());
      return new ProcessedResult<>(null, reportMessages);
    }

    // Preparation
    LOGGER.info("Received RDF for enrichment/dereferencing. Mode: {}", modes);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Processing RDF:\n{}", convertRdfToStringForLogging(rdf));
    }

    // Dereferencing first: this is because we may enrich based on its results.
    if (modes.contains(Mode.DEREFERENCE)) {
      LOGGER.debug("Performing dereferencing...");
      reportMessages.addAll(dereferencer.dereference(rdf));
      LOGGER.debug("Dereferencing completed.");
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("RDF after dereferencing:\n{}", convertRdfToStringForLogging(rdf));
      }
    }

    // Enrichment second: we use the result of dereferencing as well.
    if (modes.contains(Mode.ENRICHMENT)) {
      LOGGER.debug("Performing enrichment...");
      reportMessages.addAll(enricher.enrichment(rdf));
      LOGGER.debug("Enrichment completed.");
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("RDF after enrichment:\n{}", convertRdfToStringForLogging(rdf));
      }
    }

    // Done
    LOGGER.debug("Processing complete.");
    return new ProcessedResult<>(rdf, reportMessages);
  }

  @Override
  public void cleanupPreviousEnrichmentEntities(RDF rdf) {
    enricher.cleanupPreviousEnrichmentEntities(rdf);
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
    return rdfConversionUtils.convertRdfToString(rdf);
  }

  ProcessedResult<String> convertRdfToString(ProcessedResult<RDF> rdfProcessedResult) throws SerializationException {
    return new ProcessedResult<>(
        this.convertRdfToString(rdfProcessedResult.getProcessedRecord()),
        rdfProcessedResult.getReport());
  }

  ProcessedResult<byte[]> convertRdfToBytes(ProcessedResult<RDF> rdfProcessedResult) throws SerializationException {
    return new ProcessedResult<>(
        rdfConversionUtils.convertRdfToBytes(rdfProcessedResult.getProcessedRecord()),
        rdfProcessedResult.getReport());
  }

  RDF convertStringToRdf(String xml) throws SerializationException {
    return rdfConversionUtils.convertStringToRdf(xml);
  }

  RDF convertInputStreamToRdf(InputStream xml) throws SerializationException {
    return rdfConversionUtils.convertInputStreamToRdf(xml);
  }
}
