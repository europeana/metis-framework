package eu.europeana.enrichment.rest.client;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.rest.client.exceptions.SerializationException;
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
    ENRICHMENT, DEREFERENCE
  }

  /**
   * Returns the mode(s) that this worker supports.
   *
   * @return the set with modes. Is not null and should not be empty.
   */
  Set<Mode> getSupportedModes();

  /**
   * Performs dereference and enrichment on an input stream to produce a target byte array. This is
   * a wrapper for {@link #process(InputStream, Set<Mode>)} where the mode set has both {@link
   * Mode#ENRICHMENT} and {@link Mode#DEREFERENCE}.
   *
   * @param inputStream The RDF to be processed as an input stream. The stream is not closed.
   * @return The processed RDF as a byte array.
   * @throws EnrichmentException In case something goes wrong with processing the RDF
   * @throws DereferenceException In case something goes wrong with processing the RDF
   * @throws SerializationException In case something goes wrong with processing the RDF.
   */
  byte[] process(final InputStream inputStream)
      throws EnrichmentException, DereferenceException, SerializationException;

  /**
   * Performs dereference and enrichment on an input stream to produce a target byte array.
   *
   * @param inputStream The RDF to be processed as an input stream. The stream is not closed.
   * @return The processed RDF as a byte array.
   * @throws SerializationException In case something goes wrong with processing the RDF.
   * @throws EnrichmentException In case something goes wrong with processing the RDF.
   * @throws DereferenceException In case something goes wrong with processing the RDF.
   */
  byte[] process(final InputStream inputStream, Set<Mode> modes)
      throws SerializationException, EnrichmentException, DereferenceException;

  /**
   * Performs dereference and enrichment on an input String to produce a target String. This is a
   * wrapper for {@link #process(String, Set<Mode>)} where the mode set has both @link
   * Mode#ENRICHMENT} and {@link Mode#DEREFERENCE}.
   *
   * @param inputString The RDF to be processed as a String.
   * @return The processed RDF as a String.
   * @throws SerializationException In case something goes wrong with processing the RDF.
   * @throws EnrichmentException In case something goes wrong with processing the RDF.
   * @throws DereferenceException In case something goes wrong with processing the RDF.
   */
  String process(final String inputString)
      throws  EnrichmentException, DereferenceException, SerializationException;

  /**
   * Performs dereference and enrichment on an input String to produce a target String.
   *
   * @param inputString The RDF to be processed as a String.
   * @return The processed RDF as a String.
   * @throws SerializationException In case something goes wrong with processing the RDF.
   * @throws EnrichmentException In case something goes wrong with processing the RDF.
   * @throws DereferenceException In case something goes wrong with processing the RDF.
   */
  String process(final String inputString, Set<Mode> modes)
      throws SerializationException, EnrichmentException, DereferenceException;

  /**
   * Performs dereference and enrichment on an input RDF to produce a target RDF. This is a wrapper
   * for {@link #process(RDF, Set<Mode>)} where the mode set has both is @link
   * Mode#ENRICHMENT} and {@link Mode#DEREFERENCE}.
   *
   * @param inputRdf The RDF to be processed.
   * @return The processed RDF. Note: this may be the same object as the input object.
   * @throws EnrichmentException In case something goes wrong with processing the RDF.
   * @throws DereferenceException In case something goes wrong with processing the RDF.
   */
  RDF process(final RDF inputRdf)
      throws EnrichmentException, DereferenceException;

  /**
   * Performs dereference and enrichment on an input RDF to produce a target RDF.
   *
   * @param rdf The RDF to be processed.
   * @param modes A set of the processing modes to be applied.
   * @return The processed RDF. Note: this will be the same object as the input object.
   * @throws EnrichmentException In case something goes wrong with processing the RDF.
   * @throws DereferenceException In case something goes wrong with processing the RDF.
   */
  RDF process(final RDF rdf, Set<Mode> modes)
      throws EnrichmentException, DereferenceException;

}
