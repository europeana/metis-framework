package eu.europeana.enrichment.internal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import eu.europeana.corelib.utils.StringArrayUtils;
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
  private Map<String, List<String>> isPartOf;
  private Map<String, List<String>> dctermsHasPart;
  private String[] isNextInSequence;

  public Map<String, List<String>> getBegin() {
    return this.begin;
  }

  public Map<String, List<String>> getEnd() {
    return this.end;
  }

  public Map<String, List<String>> getIsPartOf() {
    return this.isPartOf;
  }

  public void setBegin(Map<String, List<String>> begin) {
    this.begin = begin;
  }

  public void setEnd(Map<String, List<String>> end) {
    this.end = end;
  }

  public void setIsPartOf(Map<String, List<String>> isPartOf) {
    this.isPartOf = isPartOf;
  }

  public Map<String, List<String>> getDctermsHasPart() {
    return this.dctermsHasPart;
  }

  public void setDctermsHasPart(Map<String, List<String>> dctermsHasPart) {
    this.dctermsHasPart = dctermsHasPart;
  }

  public String[] getIsNextInSequence() {
    return (StringArrayUtils.isNotBlank(isNextInSequence) ? this.isNextInSequence.clone() : null);
  }

  public void setIsNextInSequence(String[] isNextInSequence) {
    this.isNextInSequence = isNextInSequence != null ? isNextInSequence.clone() : null;
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
