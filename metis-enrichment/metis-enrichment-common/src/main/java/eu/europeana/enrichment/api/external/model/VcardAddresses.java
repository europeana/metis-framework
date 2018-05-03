package eu.europeana.enrichment.api.external.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class stores result of the parsing of XSLT/XML 
 * Wikidata content for the list of addresses. 
 * 
 * @author GrafR
 *
 */
@XmlRootElement(namespace = "http://www.w3.org/2006/vcard/ns#", name = "hasAddress")
@XmlAccessorType(XmlAccessType.FIELD)
public class VcardAddresses { 
  
  @XmlElement(name = "Address", namespace = "http://www.w3.org/2006/vcard/ns#")
  private List<VcardAddress> vcardAddresses = new ArrayList<VcardAddress>();
  
  public List<VcardAddress> getVcardAddresses() {
    return vcardAddresses;
  }

  public void setVcardAddresses(List<VcardAddress> vcardAddresses) {
    this.vcardAddresses = vcardAddresses;
  }
  
   
}
