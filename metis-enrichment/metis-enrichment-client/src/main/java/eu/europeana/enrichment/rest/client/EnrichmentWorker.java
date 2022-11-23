package eu.europeana.enrichment.rest.client;

import eu.europeana.enrichment.rest.client.report.ProcessedResult;
import eu.europeana.metis.schema.jibx.RDF;
import java.io.InputStream;
import java.util.Set;

/**
 * Classes that implement this interface perform the task of dereferencing and enrichment for a given RDF document.
 */
public interface EnrichmentWorker {

  /**
   * Contains the Modes that are available for processing.
   */
  enum Mode {
    ENRICHMENT, DEREFERENCE
  }

  /**
   * Returns the mode(s) that this worker supports.
   *
   * @return the set with modes. Is not null and should not be empty.
   */
  Set<Mode> getSupportedModes();

  /**
   * Performs dereference and enrichment on an input stream to produce a target byte array. This is a wrapper for
   * {@link #process(InputStream, Set)} where the mode set has both {@link Mode#ENRICHMENT} and {@link Mode#DEREFERENCE}.
   *
   * @param inputStream The RDF to be processed as an input stream. The stream is not closed.
   * @return The ProcessedResult contains the processed RDF as a byte array and a report of messages.
   */
  ProcessedResult<byte[]> process(final InputStream inputStream);

  /**
   * Performs dereference and enrichment on an input stream to produce a target byte array.
   *
   * @param inputStream The RDF to be processed as an input stream. The stream is not closed.
   * @param modes enrichment, dereference mode or both.
   * @return The ProcessedResult contains The processed RDF as a byte array and a report of messages.
   */
  ProcessedResult<byte[]> process(final InputStream inputStream, Set<Mode> modes);

  /**
   * Performs dereference and enrichment on an input String to produce a target String. This is a wrapper for
   * {@link #process(String, Set)} where the mode set has both @link Mode#ENRICHMENT} and {@link Mode#DEREFERENCE}.
   *
   * @param inputString The RDF to be processed as a String.
   * @return The ProcessedResult contains the processed RDF as a String and a report of messages.
   */
  ProcessedResult<String> process(final String inputString);

  /**
   * Performs dereference and enrichment on an input String to produce a target String.
   *
   * @param inputString The RDF to be processed as a String.
   * @param modes enrichment, dereference mode or both.
   * @return The ProcessedResult contains the processed RDF as a String and a report of messages.
   */
  ProcessedResult<String> process(final String inputString, Set<Mode> modes);

  /**
   * Performs dereference and enrichment on an input RDF to produce a target RDF. This is a wrapper for {@link #process(RDF, Set)}
   * where the mode set has both is @link Mode#ENRICHMENT} and {@link Mode#DEREFERENCE}.
   *
   * @param inputRdf The RDF to be processed.
   * @return The ProcessedResult contains the processed RDF and a report of messages.
   * Note: this may be the same object as the input object.
   */
  ProcessedResult<RDF> process(final RDF inputRdf);

  /**
   * Performs dereference and enrichment on an input RDF to produce a target RDF.
   *
   * @param rdf The RDF to be processed.
   * @param modes A set of the processing modes to be applied.
   * @return The ProcessedResult contains the processed RDF and a report of messages.
   * Note: this will be the same object as the input object.
   */
  ProcessedResult<RDF> process(final RDF rdf, Set<Mode> modes);

  /**
   * Cleanups/Removes enrichment entities from a previous enrichment.
   *
   * @param rdf the RDF to be processed
   */
  void cleanupPreviousEnrichmentEntities(RDF rdf);

}
