package eu.europeana.normalization;

import java.util.List;
import eu.europeana.normalization.model.NormalizationBatchResult;
import eu.europeana.normalization.model.NormalizationResult;
import eu.europeana.normalization.util.NormalizationException;

/**
 * This interface allows access to this library's normalization functionality.
 * 
 * @author jochen
 */
public interface Normalizer {

  /**
   * This method normalizes one EDM record (which is a string representing an EDM XML file),
   * reporting on the process.
   * 
   * @param edmRecord The record to normalize.
   * @return The normalized record, again as an EDM XML file, along with a report on the
   *         normalization.
   * @throws NormalizationException In case there was a problem.
   */
  NormalizationResult normalize(String edmRecord) throws NormalizationException;

  /**
   * This method is a convenience method for calling {@link #normalize(String)} for multiple EDM
   * records.
   * 
   * @param edmRecords The records to normalize.
   * @return The normalized records, long with reports on the normalizations.
   * @throws NormalizationException In case there was a problem.
   */
  NormalizationBatchResult normalize(List<String> edmRecords) throws NormalizationException;

}
