package eu.europeana.normalization.normalizers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.w3c.dom.Document;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.util.NormalizationException;

/**
 * This class represents a normalizer with concatenated normalizer subtasks. This normalizer accepts
 * a list of other normalizers and executes them in the order given, providing the result of the
 * first as input to the second.
 */
public class ChainedNormalizer implements RecordNormalizeAction {

  private final List<RecordNormalizeAction> normalizations;

  /**
   * Constructor.
   * 
   * @param normalizations The normalizer subtasks.
   */
  public ChainedNormalizer(RecordNormalizeAction... normalizations) {
    this.normalizations = Arrays.stream(normalizations).collect(Collectors.toList());
  }

  @Override
  public NormalizationReport normalize(Document edm) throws NormalizationException {
    final NormalizationReport report = new NormalizationReport();
    for (RecordNormalizeAction normOp : normalizations) {
      report.mergeWith(normOp.normalize(edm));
    }
    return report;
  }
}
