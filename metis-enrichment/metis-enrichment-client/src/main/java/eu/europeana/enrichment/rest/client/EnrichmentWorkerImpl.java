package eu.europeana.enrichment.rest.client;

import static eu.europeana.metis.utils.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.utils.DereferenceUtils;
import eu.europeana.enrichment.utils.EnrichmentFields;
import eu.europeana.enrichment.utils.EnrichmentUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.InputValue;
import eu.europeana.enrichment.utils.RdfConversionUtils;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

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
  EnrichmentWorkerImpl(DereferenceClient dereferenceClient, EnrichmentClient enrichmentClient,
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
  public byte[] process(InputStream inputStream) throws DereferenceOrEnrichException {
    return process(inputStream, Mode.DEREFERENCE_AND_ENRICHMENT);
  }

  @Override
  public byte[] process(final InputStream inputStream, Mode mode)
      throws DereferenceOrEnrichException {
    if (inputStream == null) {
      throw new IllegalArgumentException("The input stream cannot be null.");
    }
    try {
      final RDF inputRdf = convertInputStreamToRdf(inputStream);
      final RDF resultRdf = process(inputRdf, mode);
      return convertRdfToBytes(resultRdf);
    } catch (JiBXException e) {
      throw new DereferenceOrEnrichException(
          "Something went wrong with converting to or from the RDF format.", e);
    }
  }

  @Override
  public String process(String inputString) throws DereferenceOrEnrichException {
    return process(inputString, Mode.DEREFERENCE_AND_ENRICHMENT);
  }

  @Override
  public String process(final String inputString, Mode mode) throws DereferenceOrEnrichException {
    if (inputString == null) {
      throw new IllegalArgumentException("Input RDF string cannot be null.");
    }
    try {
      final RDF inputRdf = convertStringToRdf(inputString);
      final RDF resultRdf = process(inputRdf, mode);
      return convertRdfToString(resultRdf);
    } catch (JiBXException e) {
      throw new DereferenceOrEnrichException(
          "Something went wrong with converting to or from the RDF format.", e);
    }
  }

  @Override
  public RDF process(final RDF inputRdf) throws DereferenceOrEnrichException {
    return process(inputRdf, Mode.DEREFERENCE_AND_ENRICHMENT);
  }

  @Override
  public RDF process(final RDF rdf, Mode mode) throws DereferenceOrEnrichException {

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

    // Dereferencing first: this is because we may enrich based on its results.
    if (Mode.DEREFERENCE_AND_ENRICHMENT == mode || Mode.DEREFERENCE_ONLY == mode) {
      LOGGER.debug("Performing dereferencing...");
      performDereferencing(rdf);
      LOGGER.debug("Dereferencing completed.");
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("RDF after dereferencing:\n{}", convertRdfToStringForLogging(rdf));
      }
    }

    // Enrichment second: we use the result of dereferencing as well.
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
    } catch (JiBXException e) {
      LOGGER.warn("Exception occurred while rendering an RDF document as a String.", e);
      return "[COULD NOT RENDER RDF]";
    }
  }

  private void performEnrichment(final RDF rdf) throws DereferenceOrEnrichException {

    // Extract values and references from the RDF for enrichment
    LOGGER.debug("Extracting values and references from RDF for enrichment...");
    final List<InputValue> valuesForEnrichment = extractValuesForEnrichment(rdf);
    final Map<String, Set<EnrichmentFields>> referencesForEnrichment = extractReferencesForEnrichment(
        rdf);

    // Get the information with which to enrich the RDF using the extracted values and references
    LOGGER.debug("Using extracted values and references to gather enrichment information...");
    final EnrichmentResultList valueEnrichmentInformation = enrichValues(valuesForEnrichment);
    final List<EnrichmentBaseWrapper> referenceEnrichmentInformation = enrichReferences(
        referencesForEnrichment.keySet());

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Enrichment Information...");
    if (valueEnrichmentInformation != null && CollectionUtils
        .isNotEmpty(valueEnrichmentInformation.getEnrichmentBaseWrapperList())) {
      entityMergeEngine
          .mergeEntities(rdf, valueEnrichmentInformation.getEnrichmentBaseWrapperList());
    }
    if (!referenceEnrichmentInformation.isEmpty()) {
      final List<EnrichmentBase> entities = referenceEnrichmentInformation.stream()
          .map(EnrichmentBaseWrapper::getEnrichmentBase).collect(Collectors.toList());
      entityMergeEngine.mergeEntities(rdf, entities, referencesForEnrichment);
    }

    // Setting additional field values and set them in the RDF.
    LOGGER.debug("Setting additional data in the RDF...");
    EnrichmentUtils.setAdditionalData(rdf);

    // Done.
    LOGGER.debug("Enrichment completed.");
  }

  private EnrichmentResultList enrichValues(List<InputValue> valuesForEnrichment)
      throws DereferenceOrEnrichException {
    try {
      return CollectionUtils.isEmpty(valuesForEnrichment) ? null
          : retryableExternalRequestForNetworkExceptions(
              () -> enrichmentClient.enrich(valuesForEnrichment));
    } catch (Exception e) {
      throw new DereferenceOrEnrichException(
          "Exception occurred while trying to perform enrichment.", e);
    }
  }

  private List<EnrichmentBaseWrapper> enrichReferences(Set<String> referencesForEnrichment)
      throws DereferenceOrEnrichException {
    try {
      return CollectionUtils.isEmpty(referencesForEnrichment) ? Collections.emptyList()
          : retryableExternalRequestForNetworkExceptions(
              () -> enrichmentClient.getByUri(referencesForEnrichment));
    } catch (Exception e) {
      throw new DereferenceOrEnrichException(
          "Exception occurred while trying to perform enrichment.", e);
    }
  }

  private void performDereferencing(final RDF rdf) throws DereferenceOrEnrichException {

    // Extract fields from the RDF for dereferencing
    LOGGER.debug(" Extracting fields from RDF for dereferencing...");
    Set<String> resourceIds = extractReferencesForDereferencing(rdf);

    // Get the dereferenced information to add to the RDF using the extracted fields
    LOGGER.debug("Using extracted fields to gather enrichment-via-dereferencing information...");
    final List<EnrichmentBaseWrapper> dereferenceInformation = dereferenceFields(resourceIds);

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Dereference Information...");
    entityMergeEngine.mergeEntities(rdf, dereferenceInformation);

    // Done.
    LOGGER.debug("Dereference completed.");
  }

  private List<EnrichmentBaseWrapper> dereferenceFields(Set<String> resourceIds)
      throws DereferenceOrEnrichException {

    // Sanity check.
    if (resourceIds.isEmpty()) {
      return Collections.emptyList();
    }

    // First try to get them from our own entity collection database.
    final List<EnrichmentBaseWrapper> result = new ArrayList<>(dereferenceOwnEntities(resourceIds));
    final Set<String> foundOwnEntityIds = result.stream()
        .map(EnrichmentBaseWrapper::getEnrichmentBase).map(EnrichmentBase::getAbout)
        .collect(Collectors.toSet());

    // For the remaining ones, get them from the dereference service.
    for (String resourceId : resourceIds) {
      if (!foundOwnEntityIds.contains(resourceId)) {
        result.addAll(dereferenceExternalEntity(resourceId));
      }
    }

    // Done.
    return result;
  }

  private List<EnrichmentBaseWrapper> dereferenceOwnEntities(Set<String> resourceIds)
      throws DereferenceOrEnrichException {
    try {
      return retryableExternalRequestForNetworkExceptions(
          () -> enrichmentClient.getById(resourceIds));
    } catch (Exception e) {
      throw new DereferenceOrEnrichException(
          "Exception occurred while trying to perform dereferencing.", e);
    }
  }

  private List<EnrichmentBaseWrapper> dereferenceExternalEntity(String resourceId)
      throws DereferenceOrEnrichException {

    // Perform the dereferencing.
    EnrichmentResultList result;
    try {
      LOGGER.debug("== Processing {}", resourceId);
      result = retryableExternalRequestForNetworkExceptions(
          () -> dereferenceClient.dereference(resourceId));
    } catch (BadRequest e) {
      // We are forgiving for these errors
      LOGGER.warn("ResourceId {}, failed", resourceId, e);
      result = null;
    } catch (Exception e) {
      throw new DereferenceOrEnrichException(
          "Exception occurred while trying to perform dereferencing.", e);
    }

    // Return the result.
    return Optional.ofNullable(result).map(EnrichmentResultList::getEnrichmentBaseWrapperList)
        .orElseGet(Collections::emptyList);
  }

  List<InputValue> extractValuesForEnrichment(RDF rdf) {
    return EnrichmentUtils.extractValuesForEnrichmentFromRDF(rdf);
  }

  Map<String, Set<EnrichmentFields>> extractReferencesForEnrichment(RDF rdf) {
    return EnrichmentUtils.extractReferencesForEnrichmentFromRDF(rdf);
  }

  Set<String> extractReferencesForDereferencing(RDF rdf) {
    return DereferenceUtils.extractReferencesForDereferencing(rdf);
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
