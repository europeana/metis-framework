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
public class ConceptEnrichmentEntity extends AbstractEnrichmentEntity {

  private String[] broader;
  private String[] narrower;
  private String[] related;
  private String[] broadMatch;
  private String[] narrowMatch;
  private String[] exactMatch;
  private String[] relatedMatch;
  private String[] closeMatch;
  private Map<String, List<String>> notation;
  private String[] inScheme;

  public String[] getBroader() {
    return (StringArrayUtils.isNotBlank(broader) ? this.broader.clone() : null);
  }

  public void setBroader(String[] broader) {
    this.broader = broader == null ? null : broader.clone();
  }

  public String[] getNarrower() {
    return (StringArrayUtils.isNotBlank(narrower) ? this.narrower.clone() : null);
  }

  public void setNarrower(String[] narrower) {
    this.narrower = narrower == null ? null : narrower.clone();
  }

  public String[] getRelated() {
    return (StringArrayUtils.isNotBlank(related) ? this.related.clone() : null);
  }

  public void setRelated(String[] related) {
    this.related = related == null ? null : related.clone();
  }

  public String[] getBroadMatch() {
    return (StringArrayUtils.isNotBlank(broadMatch) ? this.broadMatch.clone() : null);
  }

  public void setBroadMatch(String[] broadMatch) {
    this.broadMatch = broadMatch == null ? null : broadMatch.clone();
  }

  public String[] getNarrowMatch() {
    return (StringArrayUtils.isNotBlank(narrowMatch) ? this.narrowMatch.clone() : null);
  }

  public void setNarrowMatch(String[] narrowMatch) {
    this.narrowMatch = narrowMatch == null ? null : narrowMatch.clone();
  }

  public String[] getRelatedMatch() {
    return (StringArrayUtils.isNotBlank(relatedMatch) ? this.relatedMatch.clone() : null);
  }

  public void setRelatedMatch(String[] relatedMatch) {
    this.relatedMatch = relatedMatch == null ? null : relatedMatch.clone();
  }

  public String[] getExactMatch() {
    return (StringArrayUtils.isNotBlank(exactMatch) ? this.exactMatch.clone() : null);
  }

  public void setExactMatch(String[] exactMatch) {
    this.exactMatch = exactMatch == null ? null : exactMatch.clone();
  }

  public String[] getCloseMatch() {
    return (StringArrayUtils.isNotBlank(closeMatch) ? this.closeMatch.clone() : null);
  }

  public void setCloseMatch(String[] closeMatch) {
    this.closeMatch = closeMatch == null ? null : closeMatch.clone();
  }

  public Map<String, List<String>> getNotation() {
    return this.notation;
  }

  public void setNotation(Map<String, List<String>> notation) {
    this.notation = notation;
  }

  public String[] getInScheme() {
    return (StringArrayUtils.isNotBlank(inScheme) ? this.inScheme.clone() : null);
  }

  public void setInScheme(String[] inScheme) {
    this.inScheme = inScheme == null ? null : inScheme.clone();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o.getClass() == this.getClass()) {
      return this.getAbout().equals(((ConceptEnrichmentEntity) o).getAbout());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.getAbout().hashCode();
  }
}
