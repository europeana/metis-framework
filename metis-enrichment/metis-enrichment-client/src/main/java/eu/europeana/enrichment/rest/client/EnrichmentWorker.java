package eu.europeana.enrichment.rest.client;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.utils.EnrichmentUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.InputValue;
import eu.europeana.enrichment.utils.RdfConversionUtils;
import eu.europeana.metis.dereference.DereferenceUtils;

/**
 * This class performs the task of dereferencing and enrichment for a given RDF document.
 * 
 * @author jochen
 *
 */
public class EnrichmentWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentWorker.class);

  private final EnrichmentClient enrichmentClient;
  private final DereferenceClient dereferenceClient;
  private final EntityMergeEngine entityMergeEngine = new EntityMergeEngine();

  public enum Mode {
    ENRICHMENT_ONLY, DEREFERENCE_ONLY, DEREFERENCE_AND_ENRICHMENT;
  }

  /**
   * Constructor.
   * 
   * @param dereferenceUrl The URL of the dereference service.
   * @param enrichmentUrl The URL of the enrichment service.
   */
  public EnrichmentWorker(String dereferenceUrl, String enrichmentUrl) {
    this(new DereferenceClient(dereferenceUrl), new EnrichmentClient(enrichmentUrl));
  }

  /**
   * Constructor.
   * 
   * @param dereferenceClient The dereference client.
   * @param enrichmentClient The enrichment client.
   */
  EnrichmentWorker(DereferenceClient dereferenceClient, EnrichmentClient enrichmentClient) {
    this.dereferenceClient = dereferenceClient;
    this.enrichmentClient = enrichmentClient;
  }

  /**
   * Performs dereference and enrichment on an input String to produce a target String. This is a
   * wrapper for {@link #process(RDF)}.
   * 
   * @param inputString The RDF to be processed as a String.
   * @return The processed RDF as a String.
   * @throws JiBXException In case there is a problem converting to or from RDF format.
   * @throws UnsupportedEncodingException In case something goes wrong with converting the result
   *         RDF back to a String.
   * @throws DereferenceOrEnrichException In case something goes wrong with processing the RDF.
   */
  public String process(final String inputString)
      throws DereferenceOrEnrichException, JiBXException, UnsupportedEncodingException {
    final RDF inputRdf = RdfConversionUtils.convertStringToRdf(inputString);
    final RDF resultRdf = process(inputRdf, Mode.DEREFERENCE_AND_ENRICHMENT);
    return RdfConversionUtils.convertRdftoString(resultRdf);
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
    LOGGER.info("Received RDF for enrichment/dereferencing.");
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Processing RDF:\n{}", convertRdfToStringForLogging(rdf));
    }

    // Dereferencing
    if (Mode.DEREFERENCE_AND_ENRICHMENT.equals(mode) || Mode.DEREFERENCE_ONLY.equals(mode)) {
      LOGGER.info("Performing dereferencing...");
      performDereferencing(rdf);
      LOGGER.info("Dereferencing completed.");
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("RDF after dereferencing:\n{}", convertRdfToStringForLogging(rdf));
      }
    }

    // Enrichment
    if (Mode.DEREFERENCE_AND_ENRICHMENT.equals(mode) || Mode.ENRICHMENT_ONLY.equals(mode)) {
      LOGGER.info("Performing enrichment...");
      performEnrichment(rdf);
      LOGGER.info("Enrichment completed.");
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("RDF after enrichment:\n{}", convertRdfToStringForLogging(rdf));
      }
    }

    // Done
    LOGGER.debug("Processing complete.");
    return rdf;
  }

  private static String convertRdfToStringForLogging(final RDF rdf) {
    try {
      return RdfConversionUtils.convertRdftoString(rdf);
    } catch (UnsupportedEncodingException | JiBXException e) {
      LOGGER.warn("Exception occurred while rendering an RDF document as a String.", e);
      return "[COULD NOT RENDER RDF]";
    }
  }

  private void performEnrichment(final RDF rdf) throws DereferenceOrEnrichException {

    // [1] Extract fields from the RDF for enrichment
    LOGGER.debug("Extracting fields from RDF for enrichment...");
    final List<InputValue> fieldsForEnrichment =
        EnrichmentUtils.extractFieldsForEnrichmentFromRDF(rdf);

    if (fieldsForEnrichment == null || fieldsForEnrichment.isEmpty()) {
      LOGGER.debug("No fields to enrich.");
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Extracted following fields:");
      int count = 0;
      for (InputValue i : fieldsForEnrichment) {
        count++;
        LOGGER.debug("== {}: {} {} {} {}.", count, i.getLanguage(), i.getOriginalField(),
            i.getValue(), i.getVocabularies());
      }
    }

    // [2] Get the information with which to enrich the RDF using the extracted fields
    LOGGER.debug("Using extracted fields to gather enrichment information...");
    EnrichmentResultList enrichmentInformation = enrichFields(fieldsForEnrichment);
    if (enrichmentInformation == null || enrichmentInformation.getResult() == null
        || enrichmentInformation.getResult().isEmpty()) {
      LOGGER.debug("No information found. Nothing to merge.");
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Following information found:");
      int count = 1;
      for (EnrichmentBase enrichmentBase : enrichmentInformation.getResult()) {
        LOGGER.debug("== {}: About: {} AltLabelList: {} Notes: {} PrefLabelListL {}", count,
            enrichmentBase.getAbout(), enrichmentBase.getAltLabelList(), enrichmentBase.getNotes(),
            enrichmentBase.getPrefLabelList());
        count++;
      }
    }

    // [3] Merge the acquired information into the RDF
    LOGGER.debug("Merging Enrichment Information...");
    entityMergeEngine.mergeEntity(rdf, enrichmentInformation.getResult());
    LOGGER.debug("Merging completed.");
  }

  private EnrichmentResultList enrichFields(List<InputValue> fieldsForEnrichment)
      throws DereferenceOrEnrichException {
    EnrichmentResultList enrichmentInformation = null;
    try {
      enrichmentInformation = enrichmentClient.enrich(fieldsForEnrichment);
    } catch (RuntimeException e) {
      throw new DereferenceOrEnrichException(
          "Exception occurred while trying to perform enrichment.", e);
    }
    return enrichmentInformation;
  }

  private void performDereferencing(final RDF rdf) throws DereferenceOrEnrichException {

    // [1] Extract fields from the RDF for dereferencing
    LOGGER.debug(" Extracting fields from RDF for dereferencing...");
    Set<String> fieldsForDereferencing = DereferenceUtils.extractValuesForDereferencing(rdf);

    if (fieldsForDereferencing == null || fieldsForDereferencing.isEmpty()) {
      LOGGER.debug("No fields to dereference.");
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Extracted following fields:");
      int count = 0;
      for (String field : fieldsForDereferencing) {
        count++;
        LOGGER.debug("== {}: {}", count, field);
      }
    }

    // [2] Get the information with which to enrich (via dereferencing) the RDF using the extracted
    // fields
    LOGGER.debug("Using extracted fields to gather enrichment-via-dereferencing information...");
    List<EnrichmentResultList> dereferenceInformation = dereferenceFields(fieldsForDereferencing);
    if (dereferenceInformation.isEmpty()) {
      LOGGER.debug("No information found. Nothing to merge.");
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Following information found:");
      int count = 1;
      for (EnrichmentResultList dereferenceResultList : dereferenceInformation) {
        for (EnrichmentBase enrichmentBase : dereferenceResultList.getResult()) {
          LOGGER.debug("== {}: About: {} AltLabelList: {} Notes: {} PrefLabelListL {}", count,
              enrichmentBase.getAbout(), enrichmentBase.getAltLabelList(),
              enrichmentBase.getNotes(), enrichmentBase.getPrefLabelList());
          count++;
        }
      }
    }

    // [3] Merge the acquired information into the RDF
    LOGGER.debug("Merging Dereference Information...");
    for (EnrichmentResultList dereferenceResultList : dereferenceInformation) {
      entityMergeEngine.mergeEntity(rdf, dereferenceResultList.getResult());
    }
    LOGGER.debug("Merging completed.");
  }

  private List<EnrichmentResultList> dereferenceFields(Set<String> fieldsForDereferencing)
      throws DereferenceOrEnrichException {
    List<EnrichmentResultList> dereferenceInformation = new ArrayList<>();
    try {
      for (String url : fieldsForDereferencing) {
        if (url == null) {
          continue;
        }
        LOGGER.debug("== Processing {}", url);
        EnrichmentResultList result = dereferenceClient.dereference(url);
        if (result != null && result.getResult() != null && !result.getResult().isEmpty()) {
          dereferenceInformation.add(result);
        } else {
          LOGGER.debug("==== Null or empty value received from {}", url);
        }
      }
    } catch (RuntimeException e) {
      throw new DereferenceOrEnrichException(
          "Exception occurred while trying to perform dereferencing.", e);
    }
    return dereferenceInformation;
  }
}
