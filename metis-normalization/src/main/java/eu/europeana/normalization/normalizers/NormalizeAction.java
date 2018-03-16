package eu.europeana.normalization.normalizers;

/**
 * Instances of this class perform a single normalization action within the overall normalization
 * process. All instances must be able to produce an instance of {@link RecordNormalizeAction} that
 * reflects the normalization in the context of an EDM DOM tree.
 * 
 * @author jochen
 */
public interface NormalizeAction {

  /**
   * This method creates a record normalizer that reflects the normalization of this instance in the
   * context of an EDM DOM tree.
   * 
   * @return The record normalizer.
   */
  RecordNormalizeAction getAsRecordNormalizer();
}
