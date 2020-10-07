package eu.europeana.enrichment.rest.client;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceOrEnrichException;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import java.io.InputStream;
import java.util.Set;

/**
 * Classes that implement this interface perform the task of dereferencing and enrichment for a
 * given RDF document.
 */
public interface EnrichmentWorker {

  /**
   * Contains the Modes that are available for processing.
   */
  enum Mode {
    ENRICHMENT_ONLY, DEREFERENCE_ONLY, DEREFERENCE_AND_ENRICHMENT
  }

  /**
   * Returns the mode(s) that this worker supports.
   *
   * @return the set with modes. Is not null and should not be empty.
   */
  Set<Mode> getSupportedModes();

  /**
   * Performs dereference and enrichment on an input stream to produce a target byte array. This is
   * a wrapper for {@link #process(InputStream, Mode)} where the mode is {@link
   * Mode#DEREFERENCE_AND_ENRICHMENT}.
   *
   * @param inputStream The RDF to be processed as an input stream. The stream is not closed.
   * @return The processed RDF as a byte array.
   * @throws DereferenceOrEnrichException In case something goes wrong with processing the RDF.
   */
  byte[] process(final InputStream inputStream) throws DereferenceOrEnrichException;

  /**
   * Performs dereference and enrichment on an input stream to produce a target byte array.
   *
   * @param inputStream The RDF to be processed as an input stream. The stream is not closed.
   * @return The processed RDF as a byte array.
   * @throws DereferenceOrEnrichException In case something goes wrong with processing the RDF.
   */
  byte[] process(final InputStream inputStream, Mode mode) throws DereferenceOrEnrichException;

  /**
   * Performs dereference and enrichment on an input String to produce a target String. This is a
   * wrapper for {@link #process(String, Mode)} where the mode is {@link
   * Mode#DEREFERENCE_AND_ENRICHMENT}.
   *
   * @param inputString The RDF to be processed as a String.
   * @return The processed RDF as a String.
   * @throws DereferenceOrEnrichException In case something goes wrong with processing the RDF.
   */
  String process(final String inputString) throws DereferenceOrEnrichException;

  /**
   * Performs dereference and enrichment on an input String to produce a target String.
   *
   * @param inputString The RDF to be processed as a String.
   * @return The processed RDF as a String.
   * @throws DereferenceOrEnrichException In case something goes wrong with processing the RDF.
   */
  String process(final String inputString, Mode mode) throws DereferenceOrEnrichException;

  /**
   * Performs dereference and enrichment on an input RDF to produce a target RDF. This is a wrapper
   * for {@link #process(RDF, Mode)} where the mode is {@link Mode#DEREFERENCE_AND_ENRICHMENT}.
   *
   * @param inputRdf The RDF to be processed.
   * @return The processed RDF. Note: this may be the same object as the input object.
   * @throws DereferenceOrEnrichException In case something goes wrong with processing the RDF.
   */
  RDF process(final RDF inputRdf)
      throws DereferenceOrEnrichException, EnrichmentException, DereferenceException;

  /**
   * Performs dereference and enrichment on an input RDF to produce a target RDF.
   *
   * @param rdf The RDF to be processed.
   * @param mode The processing mode to be applied.
   * @return The processed RDF. Note: this will be the same object as the input object.
   * @throws DereferenceOrEnrichException In case something goes wrong with processing the RDF.
   */
  RDF process(final RDF rdf, Mode mode)
      throws DereferenceOrEnrichException, EnrichmentException, DereferenceException;

}
