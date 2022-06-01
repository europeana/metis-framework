package eu.europeana.enrichment.api.external.model;

import eu.europeana.enrichment.utils.EntityValuesConverterUtils;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Place model class
 */
@XmlRootElement(namespace = "http://www.europeana.eu/schemas/edm/", name = "Place")
@XmlAccessorType(XmlAccessType.FIELD)
public class Place extends EnrichmentBase {

  @XmlElement(name = "isPartOf", namespace = "http://purl.org/dc/terms/")
  private List<LabelResource> isPartOf;
  @XmlElement(name = "hasPart", namespace = "http://purl.org/dc/terms/")
  private List<LabelResource> hasPartsList = new ArrayList<>();
  @XmlElement(name = "sameAs", namespace = "http://www.w3.org/2002/07/owl#")
  private List<Part> sameAs = new ArrayList<>();

  @XmlElement(name = "lat", namespace = "http://www.w3.org/2003/01/geo/wgs84_pos#")
  private String lat;
  @XmlElement(name = "long", namespace = "http://www.w3.org/2003/01/geo/wgs84_pos#")
  private String lon;
  @XmlElement(name = "alt", namespace = "http://www.w3.org/2003/01/geo/wgs84_pos#")
  private String alt;

  public  Place() {}

  // Used for creating XML entity from EM model class
  public Place(eu.europeana.entitymanagement.definitions.model.Place place) {
    super(place);
    init(place);
  }

  public List<LabelResource> getIsPartOf() {
    return unmodifiableListAcceptingNull(isPartOf);
  }

  public void setIsPartOf(List<LabelResource> isPartOf) {
    this.isPartOf = cloneListAcceptingNull(isPartOf);
  }

  public List<LabelResource> getHasPartsList() {
    return unmodifiableListAcceptingNull(hasPartsList);
  }

  public void setHasPartsList(List<LabelResource> hasPartsList) {
    this.hasPartsList = cloneListAcceptingNull(hasPartsList);
  }

  public List<Part> getSameAs() {
    return unmodifiableListAcceptingNull(sameAs);
  }

  public void setSameAs(List<Part> sameAs) {
    this.sameAs = cloneListAcceptingNull(sameAs);
  }

  public String getLat() {
    return lat;
  }

  public void setLat(String lat) {
    this.lat = lat;
  }

  public String getLon() {
    return lon;
  }

  public void setLon(String lon) {
    this.lon = lon;
  }

  public String getAlt() {
    return alt;
  }

  public void setAlt(String alt) {
    this.alt = alt;
  }

  private void init(eu.europeana.entitymanagement.definitions.model.Place place) {
    if (place.getIsPartOfArray() != null) {
      this.isPartOf = EntityValuesConverterUtils.convertListToLabelResource(place.getIsPartOfArray());
    }
    if (place.getHasPart() != null) {
      this.hasPartsList = EntityValuesConverterUtils.convertListToLabelResource(place.getHasPart());
    }
    if (place.getSameReferenceLinks() != null) {
      this.sameAs = EntityValuesConverterUtils.convertListToPart(place.getSameReferenceLinks());
    }
    if (place.getLatitude() != null) {
      this.lat = String.valueOf(place.getLatitude());
    }
    if (place.getLongitude() != null) {
      this.lon = String.valueOf(place.getLongitude());
    }
    if (place.getAltitude() != null) {
      this.alt = String.valueOf(place.getAltitude());
    }
  }

}
