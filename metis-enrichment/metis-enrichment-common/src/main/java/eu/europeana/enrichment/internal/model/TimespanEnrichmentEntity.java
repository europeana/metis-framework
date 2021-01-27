package eu.europeana.enrichment.internal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis
 * @since 2020-08-31
 */
@JsonInclude(Include.NON_EMPTY)
public class TimespanEnrichmentEntity extends AbstractEnrichmentEntity {

  private Map<String, List<String>> begin;
  private Map<String, List<String>> end;
  private Map<String, List<String>> dctermsHasPart;
  private String isNextInSequence;

  public Map<String, List<String>> getBegin() {
    return this.begin;
  }

  public void setBegin(Map<String, List<String>> begin) {
    this.begin = begin;
  }

  public Map<String, List<String>> getEnd() {
    return this.end;
  }

  public void setEnd(Map<String, List<String>> end) {
    this.end = end;
  }

  public Map<String, List<String>> getDctermsHasPart() {
    return this.dctermsHasPart;
  }

  public void setDctermsHasPart(Map<String, List<String>> dctermsHasPart) {
    this.dctermsHasPart = dctermsHasPart;
  }

  public String getIsNextInSequence() {
    return this.isNextInSequence;
  }

  public void setIsNextInSequence(String isNextInSequence) {
    this.isNextInSequence = isNextInSequence;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o.getClass() == this.getClass()) {
      return this.getAbout().equals(((TimespanEnrichmentEntity) o).getAbout());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.getAbout().hashCode();
  }
}
