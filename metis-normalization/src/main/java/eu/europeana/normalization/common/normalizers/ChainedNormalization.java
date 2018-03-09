package eu.europeana.normalization.common.normalizers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.w3c.dom.Document;
import eu.europeana.normalization.common.RecordNormalization;
import eu.europeana.normalization.model.NormalizationReport;

public class ChainedNormalization implements RecordNormalization {

  private final List<RecordNormalization> normalizations;

  public ChainedNormalization() {
    this(new RecordNormalization[0]);
  }

  public ChainedNormalization(RecordNormalization... normalizations) {
    this.normalizations = Arrays.stream(normalizations).collect(Collectors.toList());
  }

  @Override
  public NormalizationReport normalize(Document edm) {
    NormalizationReport report = new NormalizationReport();
    for (RecordNormalization normOp : normalizations) {
      report.mergeWith(normOp.normalize(edm));
    }
    return report;
  }
}
