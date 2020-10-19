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
public class PlaceEnrichmentEntity extends AbstractEnrichmentEntity {

  private Map<String, List<String>> isPartOf;
  private Float latitude;
  private Float longitude;
  private Float altitude;
  private Map<String, Float> position;
  private Map<String, List<String>> dcTermsHasPart;

  public Map<String, List<String>> getIsPartOf() {
    return this.isPartOf;
  }

  public Float getLatitude() {
    if (this.latitude == null || this.longitude == null || (this.latitude == 0
        && this.longitude == 0)) {
      return null;
    }
    return this.latitude;
  }

  public Float getLongitude() {
    if (this.latitude == null || this.longitude == null || (this.latitude == 0
        && this.longitude == 0)) {
      return null;
    }
    return this.longitude;
  }

  public void setIsPartOf(Map<String, List<String>> isPartOf) {
    this.isPartOf = isPartOf;
  }

  public void setLatitude(Float latitude) {
    this.latitude = latitude;
  }

  public void setLongitude(Float longitude) {
    this.longitude = longitude;
  }

  public void setAltitude(Float altitude) {
    this.altitude = altitude;
  }

  public Float getAltitude() {
    if (this.latitude == null || this.longitude == null || (this.latitude == 0
        && this.longitude == 0)) {
      return null;
    }
    return this.altitude;
  }

  public void setPosition(Map<String, Float> position) {
    this.position = position;
  }

  public Map<String, Float> getPosition() {
    return this.position;
  }

  public void setDcTermsHasPart(Map<String, List<String>> dcTermsHasPart) {
    this.dcTermsHasPart = dcTermsHasPart;
  }

  public Map<String, List<String>> getDcTermsHasPart() {
    return this.dcTermsHasPart;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o.getClass() == this.getClass()) {
      return ((PlaceEnrichmentEntity) o).getAbout() != null ? this.getAbout()
          .equals(((PlaceEnrichmentEntity) o).getAbout()) : false;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (int) (this.getAbout() != null ? this.getAbout().hashCode()
        : this.latitude * 100 + this.longitude);
  }
}
