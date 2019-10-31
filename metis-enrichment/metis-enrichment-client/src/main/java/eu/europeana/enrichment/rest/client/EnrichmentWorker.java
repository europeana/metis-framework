package eu.europeana.enrichment.rest.client;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.utils.DereferenceUtils;
import eu.europeana.enrichment.utils.EnrichmentUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.InputValue;
import eu.europeana.enrichment.utils.RdfConversionUtils;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ObjIntConsumer;
import org.apache.commons.collections.CollectionUtils;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.HttpServerErrorException;

/**
 * This class performs the task of dereferencing and enrichment for a given RDF document.
 *
 * @author jochen
 */
public class EnrichmentWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentWorker.class);
  private static final int EXTERNAL_CALL_MAX_RETRIES = 30;
  private static final int EXTERNAL_CALL_PERIOD_BETWEEN_RETRIES_IN_MILLIS = 1000;
  private static final Map<Class<?>, String> mapWithRetrieableExceptions;

  static {
    final Map<Class<?>, String> retriableExceptionMap = new HashMap<>();
    retriableExceptionMap.put(UnknownHostException.class, "");
    retriableExceptionMap.put(HttpServerErrorException.class, "");
    mapWithRetrieableExceptions = Collections.unmodifiableMap(retriableExceptionMap);
  }

  private final EnrichmentClient enrichmentClient;
  private final DereferenceClient dereferenceClient;
  private final EntityMergeEngine entityMergeEngine;

  /**
   * Contains the Modes that are allowed for enrichment.
   */
  public enum Mode {
    ENRICHMENT_ONLY, DEREFERENCE_ONLY, DEREFERENCE_AND_ENRICHMENT
  }

  /**
   * Constructor.
   *
   * @param dereferenceUrl The URL of the dereference service.
   * @param enrichmentUrl The URL of the enrichment service.
   */
  public EnrichmentWorker(String dereferenceUrl, String enrichmentUrl) {
    this(new DereferenceClient(dereferenceUrl), new EnrichmentClient(enrichmentUrl),
        new EntityMergeEngine());
  }

  /**
   * Constructor.
   *
   * @param dereferenceClient The dereference client.
   * @param enrichmentClient The enrichment client.
   * @param entityMergeEngine The engine to be used for merging entities into the RDF.
   */
  EnrichmentWorker(DereferenceClient dereferenceClient, EnrichmentClient enrichmentClient,
      EntityMergeEngine entityMergeEngine) {
    this.dereferenceClient = dereferenceClient;
    this.enrichmentClient = enrichmentClient;
    this.entityMergeEngine = entityMergeEngine;
  }

  /**
   * Performs dereference and enrichment on an input String to produce a target String. This is a
   * wrapper for {@link #process(RDF)}.
   *
   * @param inputString The RDF to be processed as a String.
   * @return The processed RDF as a String.
   * @throws JiBXException In case there is a problem converting to or from RDF format.
   * @throws UnsupportedEncodingException In case something goes wrong with converting the result
   * RDF back to a String.
   * @throws DereferenceOrEnrichException In case something goes wrong with processing the RDF.
   */
  public String process(final String inputString)
      throws DereferenceOrEnrichException, JiBXException, UnsupportedEncodingException {

    if (inputString == null) {
      throw new IllegalArgumentException("Input RDF string cannot be null.");
    }
    final RDF inputRdf = convertStringToRdf(inputString);
    final RDF resultRdf = process(inputRdf, Mode.DEREFERENCE_AND_ENRICHMENT);
    return convertRdfToString(resultRdf);
  }

  /**
   * Performs dereference and enrichment on an input RDF to produce a target RDF. This is a wrapper
   * for {@link #process(RDF, Mode)} where the mode is {@link Mode#DEREFERENCE_AND_ENRICHMENT}.
   *
   * @param inputRdf The RDF to be processed.
   * @return The processed RDF. Note: this may be the same object as the input object.
   * @throws DereferenceOrEnrichException In case something goes wrong.
   */
  public RDF process(final RDF inputRdf) throws DereferenceOrEnrichException {
    return process(inputRdf, Mode.DEREFERENCE_AND_ENRICHMENT);
  }

  /**
   * Performs dereference and enrichment on an input RDF to produce a target RDF.
   *
   * @param rdf The RDF to be processed.
   * @param mode The processing mode to be applied.
   * @return The processed RDF. Note: this will be the same object as the input object.
   * @throws DereferenceOrEnrichException In case something goes wrong.
   */
  public RDF process(final RDF rdf, Mode mode) throws DereferenceOrEnrichException {

    // Sanity checks
    if (rdf == null) {
      throw new IllegalArgumentException("Input RDF cannot be null.");
    }
    if (mode == null) {
      throw new IllegalArgumentException("Mode cannot be null.");
    }

    // Preparation
    LOGGER.info("Received RDF for enrichment/dereferencing. Mode: {}", mode);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Processing RDF:\n{}", convertRdfToStringForLogging(rdf));
    }

    // Dereferencing
    if (Mode.DEREFERENCE_AND_ENRICHMENT == mode || Mode.DEREFERENCE_ONLY == mode) {
      LOGGER.debug("Performing dereferencing...");
      performDereferencing(rdf);
      LOGGER.debug("Dereferencing completed.");
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("RDF after dereferencing:\n{}", convertRdfToStringForLogging(rdf));
      }
    }

    // Enrichment
    if (Mode.DEREFERENCE_AND_ENRICHMENT == mode || Mode.ENRICHMENT_ONLY == mode) {
      LOGGER.debug("Performing enrichment...");
      performEnrichment(rdf);
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
    } catch (UnsupportedEncodingException | JiBXException e) {
      LOGGER.warn("Exception occurred while rendering an RDF document as a String.", e);
      return "[COULD NOT RENDER RDF]";
    }
  }

  private void performEnrichment(final RDF rdf) throws DereferenceOrEnrichException {

    // [1] Extract fields from the RDF for enrichment
    LOGGER.debug("Extracting fields from RDF for enrichment...");
    final List<InputValue> fieldsForEnrichment = extractFieldsForEnrichment(rdf);
    if (LOGGER.isDebugEnabled()) {
      logExtractionResult(fieldsForEnrichment, EnrichmentWorker::logInputValueWithCounter);
    }

    // [2] Get the information with which to enrich the RDF using the extracted fields
    LOGGER.debug("Using extracted fields to gather enrichment information...");
    EnrichmentResultList enrichmentInformation = enrichFields(fieldsForEnrichment);
    if (LOGGER.isDebugEnabled()) {
      logDereferencingOrEnrichmentResults(Collections.singletonList(enrichmentInformation));
    }

    // [3] Merge the acquired information into the RDF
    LOGGER.debug("Merging Enrichment Information...");
    if (enrichmentInformation != null && CollectionUtils
        .isNotEmpty(enrichmentInformation.getEnrichmentBaseWrapperList())) {
      entityMergeEngine.mergeEntities(rdf, enrichmentInformation.getEnrichmentBaseWrapperList());
    }

    // [4] Setting additional field values and set them in the RDF.
    LOGGER.debug("Setting additional data in the RDF...");
    EnrichmentUtils.setAdditionalData(rdf);
    LOGGER.debug("Enrichment completed.");
  }

  private EnrichmentResultList enrichFields(List<InputValue> fieldsForEnrichment)
      throws DereferenceOrEnrichException {
    try {
      return CollectionUtils.isEmpty(fieldsForEnrichment) ? null :
          ExternalRequestUtil
              .retryableExternalRequest(() -> enrichmentClient.enrich(fieldsForEnrichment),
                  mapWithRetrieableExceptions, EXTERNAL_CALL_MAX_RETRIES,
                  EXTERNAL_CALL_PERIOD_BETWEEN_RETRIES_IN_MILLIS);
    } catch (Exception e) {
      throw new DereferenceOrEnrichException(
          "Exception occurred while trying to perform enrichment.", e);
    }
  }

  private void performDereferencing(final RDF rdf) throws DereferenceOrEnrichException {

    // [1] Extract fields from the RDF for dereferencing
    LOGGER.debug(" Extracting fields from RDF for dereferencing...");
    Set<String> resourceIds = extractValuesForDereferencing(rdf);
    if (LOGGER.isDebugEnabled()) {
      logExtractionResult(resourceIds, EnrichmentWorker::logStringWithCounter);
    }

    // [2] Get the information with which to enrich (via dereferencing) the RDF using the extracted
    // fields
    LOGGER.debug("Using extracted fields to gather enrichment-via-dereferencing information...");
    List<EnrichmentResultList> dereferenceInformation = dereferenceFields(resourceIds);
    if (LOGGER.isDebugEnabled()) {
      logDereferencingOrEnrichmentResults(dereferenceInformation);
    }

    // [3] Merge the acquired information into the RDF
    LOGGER.debug("Merging Dereference Information...");
    for (EnrichmentResultList dereferenceResultList : dereferenceInformation) {
      if (dereferenceResultList != null
          && dereferenceResultList.getEnrichmentBaseWrapperList() != null
          && !dereferenceResultList.getEnrichmentBaseWrapperList().isEmpty()) {
        entityMergeEngine.mergeEntities(rdf, dereferenceResultList.getEnrichmentBaseWrapperList());
      }
    }
    LOGGER.debug("Dereference completed.");
  }

  private List<EnrichmentResultList> dereferenceFields(Set<String> resourceIds)
      throws DereferenceOrEnrichException {
    List<EnrichmentResultList> dereferenceInformation = new ArrayList<>();
    for (String resourceId : resourceIds) {
      try {
        if (resourceId == null) {
          continue;
        }
        LOGGER.debug("== Processing {}", resourceId);
        EnrichmentResultList result =
            ExternalRequestUtil
                .retryableExternalRequest(() -> dereferenceClient.dereference(resourceId),
                    mapWithRetrieableExceptions, EXTERNAL_CALL_MAX_RETRIES,
                    EXTERNAL_CALL_PERIOD_BETWEEN_RETRIES_IN_MILLIS);
        if (result == null || result.getEnrichmentBaseWrapperList() == null || result
            .getEnrichmentBaseWrapperList().isEmpty()) {
          LOGGER.debug("==== Null or empty value received for reference {}", resourceId);
        } else {
          dereferenceInformation.add(result);
        }
      } catch (BadRequest e) {
        //We are forgiving for these errors
        LOGGER.warn("ResourceId {}, failed", resourceId, e);
      } catch (Exception e) {
        throw new DereferenceOrEnrichException(
            "Exception occurred while trying to perform dereferencing.", e);
      }
    }
    return dereferenceInformation;
  }

  private static void logStringWithCounter(String field, int count) {
    LOGGER.debug("== {}: {}", count, field);
  }

  private static void logInputValueWithCounter(InputValue inputValue, int count) {
    LOGGER.debug("== {}: {} {} {} {}.", count, inputValue.getLanguage(),
        inputValue.getOriginalField(), inputValue.getValue(), inputValue.getVocabularies());
  }

  private <T> void logExtractionResult(Collection<T> resultList, ObjIntConsumer<T> logger) {
    int count = 0;
    if (resultList != null) {
      LOGGER.debug("Extracted following fields:");
      for (T result : resultList) {
        if (result == null) {
          continue;
        }
        count++;
        logger.accept(result, count);
      }
    }
    if (count == 0) {
      LOGGER.debug("No fields to process.");
    }
  }

  private void logDereferencingOrEnrichmentResults(List<EnrichmentResultList> resultList) {
    int count = 0;
    if (resultList != null) {
      LOGGER.debug("Following information found:");
      for (EnrichmentResultList result : resultList) {
        count += logDereferencingOrEnrichmentResult(result);
      }
    }
    if (count == 0) {
      LOGGER.debug("No information found. Nothing to merge.");
    }
  }

  private int logDereferencingOrEnrichmentResult(EnrichmentResultList result) {
    if (result == null) {
      return 0;
    }
    int count = 0;
    for (EnrichmentBaseWrapper enrichmentBaseWrapper : result.getEnrichmentBaseWrapperList()) {
      if (enrichmentBaseWrapper == null) {
        continue;
      }
      count++;
      LOGGER.debug("== {}: About: {} AltLabelList: {} Notes: {} PrefLabelListL {}", count,
          enrichmentBaseWrapper.getEnrichmentBase().getAbout(),
          enrichmentBaseWrapper.getEnrichmentBase().getAltLabelList(),
          enrichmentBaseWrapper.getEnrichmentBase().getNotes(),
          enrichmentBaseWrapper.getEnrichmentBase().getPrefLabelList());
    }
    return count;
  }

  List<InputValue> extractFieldsForEnrichment(RDF rdf) {
    return EnrichmentUtils.extractFieldsForEnrichmentFromRDF(rdf);
  }

  Set<String> extractValuesForDereferencing(RDF rdf) {
    return DereferenceUtils.extractValuesForDereferencing(rdf);
  }

  String convertRdfToString(RDF rdf) throws UnsupportedEncodingException, JiBXException {
    return RdfConversionUtils.convertRdfToString(rdf);
  }

  RDF convertStringToRdf(String xml) throws JiBXException {
    return RdfConversionUtils.convertStringToRdf(xml);
  }
}
