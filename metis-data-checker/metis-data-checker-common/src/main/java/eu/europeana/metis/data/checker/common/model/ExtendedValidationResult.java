package eu.europeana.metis.data.checker.common.model;

import eu.europeana.validation.model.ValidationResultList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Extended class with data checker URL for data checker service Created by ymamakis on 9/2/16.
 */
@XmlRootElement
public class ExtendedValidationResult extends ValidationResultList implements Serializable {

  private static final long serialVersionUID = 2405172041950251807L;

  private String portalUrl;
  private Date date;
  private List<String> records;

  /**
   * The portal url
   *
   * @return The data checker portal URL
   */
  @XmlElement
  public String getPortalUrl() {
    return portalUrl;
  }

  /**
   * Set the data checker portal URL
   *
   * @param portalUrl Set the data checker portal URL
   */
  public void setPortalUrl(String portalUrl) {
    this.portalUrl = portalUrl;
  }

  @XmlElement
  public Date getDate() {
    return Optional.ofNullable(date).map(Date::getTime).map(Date::new).orElse(null);
  }

  public void setDate(Date date) {
    this.date = Optional.ofNullable(date).map(Date::getTime).map(Date::new).orElse(null);
  }

  @XmlElement
  public List<String> getRecords() {
    return records == null ? null : new ArrayList<>(records);
  }

  public void setRecords(List<String> records) {
    this.records = records == null ? null : new ArrayList<>(records);
  }
}
