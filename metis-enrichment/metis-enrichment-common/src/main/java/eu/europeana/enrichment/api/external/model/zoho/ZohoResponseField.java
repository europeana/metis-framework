package eu.europeana.enrichment.api.external.model.zoho;

import eu.europeana.enrichment.api.external.model.TextProperty;

/**
 * This class supports representation of Zoho organization fields for API to Zoho organization
 * object that contains array of 'val'/'content' fields.
 * 
 * @author GrafR
 *
 */
public class ZohoResponseField implements TextProperty {

  private String val;
  private String content;

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getVal() {
    return val;
  }

  public void setVal(String val) {
    this.val = val;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return (obj.getClass() == this.getClass() && this.val.equals(((ZohoResponseField) obj).val));
  }

  @Override
  public int hashCode() {
    return val.hashCode();
  }

  @Override
  public String getKey() {
    return getVal();
  }

  @Override
  public String getValue() {
    return getContent();
  }

}
