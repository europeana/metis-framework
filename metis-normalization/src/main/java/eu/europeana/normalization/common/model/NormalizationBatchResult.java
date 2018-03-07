package eu.europeana.normalization.common.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class NormalizationBatchResult {

  private NormalizationReport report;
  private List<String> normalizedRecordsInEdmXml;
  private int errors = 0;

  public NormalizationBatchResult() {
    normalizedRecordsInEdmXml = new ArrayList<>();
  }

  public NormalizationBatchResult(List<NormalizationResult> results) {
    normalizedRecordsInEdmXml = new ArrayList<>(results.size());
    report = new NormalizationReport();
    for (NormalizationResult r : results) {
      normalizedRecordsInEdmXml.add(r.getNormalizedRecordInEdmXml());
      if (r.isError()) {
        errors++;
      }
      report.mergeWith(r.getReport());
    }
  }


  public NormalizationReport getReport() {
    return report;
  }

  public void setReport(NormalizationReport report) {
    this.report = report;
  }

  public List<String> getNormalizedRecordsInEdmXml() {
    return normalizedRecordsInEdmXml;
  }

  public void setNormalizedRecordsInEdmXml(List<String> normalizedRecordsInEdmXml) {
    this.normalizedRecordsInEdmXml = normalizedRecordsInEdmXml;
  }

  public int getErrors() {
    return errors;
  }

  public void setErrors(int errors) {
    this.errors = errors;
  }

  @Override
  public String toString() {
    ObjectMapper mapper = new ObjectMapper();
    String json;
    try {
      json = mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      json = e.getMessage();
    }
    return json;
  }

}
