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
public class AgentEnrichmentEntity extends AbstractEnrichmentEntity {

  private Map<String,List<String>> begin;
  private Map<String,List<String>> end;

  private String[] edmWasPresentAt;
  private Map<String,List<String>> edmHasMet;
  private Map<String,List<String>> edmIsRelatedTo;
  private String[] owlSameAs;
  private Map<String,List<String>> foafName;
  private Map<String,List<String>> dcDate;
  private Map<String,List<String>> dcIdentifier;

  private Map<String,List<String>> rdaGr2DateOfBirth;
  private Map<String,List<String>> rdaGr2DateOfDeath;
  private Map<String,List<String>> rdaGr2PlaceOfBirth;
  private Map<String,List<String>> rdaGr2PlaceOfDeath;
  private Map<String,List<String>> rdaGr2DateOfEstablishment;
  private Map<String,List<String>> rdaGr2DateOfTermination;
  private Map<String,List<String>> rdaGr2Gender;
  private Map<String,List<String>> rdaGr2ProfessionOrOccupation;
  private Map<String,List<String>> rdaGr2BiographicalInformation;

  public Map<String,List<String>> getBegin() {
    return this.begin;
  }

  public Map<String,List<String>> getEnd() {
    return this.end;
  }

  public void setBegin(Map<String,List<String>> begin) {
    this.begin = begin;
  }

  public void setEnd(Map<String,List<String>> end) {
    this.end = end;
  }

  public void setEdmWasPresentAt(String[] edmWasPresentAt) {
    this.edmWasPresentAt = edmWasPresentAt!=null? edmWasPresentAt.clone():null;
  }

  public String[] getEdmWasPresentAt() {
    return (StringArrayUtils.isNotBlank(this.edmWasPresentAt) ? this.edmWasPresentAt.clone() : null);
  }

  public void setEdmHasMet(Map<String,List<String>> edmHasMet) {
    this.edmHasMet = edmHasMet;
  }

  public Map<String,List<String>> getEdmHasMet() {
    return this.edmHasMet;
  }

  public void setEdmIsRelatedTo(Map<String,List<String>> edmIsRelatedTo) {
    this.edmIsRelatedTo = edmIsRelatedTo;
  }

  public Map<String,List<String>> getEdmIsRelatedTo() {
    return this.edmIsRelatedTo;
  }

  public void setOwlSameAs(String[] owlSameAs) {
    this.owlSameAs = owlSameAs!=null?owlSameAs.clone():null;
  }

  public String[] getOwlSameAs() {
    return (StringArrayUtils.isNotBlank(this.owlSameAs) ? this.owlSameAs.clone() : null);
  }

  public void setFoafName(Map<String,List<String>> foafName) {
    this.foafName = foafName;
  }

  public Map<String,List<String>> getFoafName() {
    return this.foafName;
  }

  public void setDcDate(Map<String,List<String>> dcDate) {
    this.dcDate = dcDate;
  }

  public Map<String,List<String>> getDcDate() {
    return this.dcDate;
  }

  public void setDcIdentifier(Map<String,List<String>> dcIdentifier) {
    this.dcIdentifier = dcIdentifier;
  }

  public Map<String,List<String>> getDcIdentifier() {
    return this.dcIdentifier;
  }

  public void setRdaGr2DateOfBirth(Map<String,List<String>> rdaGr2DateOfBirth) {
    this.rdaGr2DateOfBirth = rdaGr2DateOfBirth;
  }

  public Map<String,List<String>> getRdaGr2DateOfBirth() {
    return this.rdaGr2DateOfBirth;
  }

  public void setRdaGr2DateOfDeath(Map<String,List<String>> rdaGr2DateOfDeath) {
    this.rdaGr2DateOfDeath = rdaGr2DateOfDeath;
  }

  public Map<String,List<String>> getRdaGr2DateOfDeath() {
    return this.rdaGr2DateOfDeath;
  }

  public Map<String, List<String>> getRdaGr2PlaceOfBirth() {
    return rdaGr2PlaceOfBirth;
  }

  public void setRdaGr2PlaceOfBirth(Map<String, List<String>> rdaGr2PlaceOfBirth) {
    this.rdaGr2PlaceOfBirth = rdaGr2PlaceOfBirth;
  }

  public Map<String, List<String>> getRdaGr2PlaceOfDeath() {
    return rdaGr2PlaceOfDeath;
  }

  public void setRdaGr2PlaceOfDeath(Map<String, List<String>> rdaGr2PlaceOfDeath) {
    this.rdaGr2PlaceOfDeath = rdaGr2PlaceOfDeath;
  }

  public void setRdaGr2DateOfEstablishment(Map<String,List<String>> rdaGr2DateOfEstablishment) {
    this.rdaGr2DateOfEstablishment = rdaGr2DateOfEstablishment;
  }

  public Map<String,List<String>> getRdaGr2DateOfEstablishment() {
    return this.rdaGr2DateOfEstablishment;
  }

  public void setRdaGr2DateOfTermination(Map<String,List<String>> rdaGr2DateOfTermination) {
    this.rdaGr2DateOfTermination = rdaGr2DateOfTermination;
  }

  public Map<String,List<String>> getRdaGr2DateOfTermination() {
    return this.rdaGr2DateOfTermination;
  }

  public void setRdaGr2Gender(Map<String,List<String>> rdaGr2Gender) {
    this.rdaGr2Gender = rdaGr2Gender;
  }

  public Map<String,List<String>> getRdaGr2Gender() {
    return this.rdaGr2Gender;
  }

  public void setRdaGr2ProfessionOrOccupation(Map<String, List<String>> rdaGr2ProfessionOrOccupation) {
    this.rdaGr2ProfessionOrOccupation = rdaGr2ProfessionOrOccupation;
  }

  public Map<String,List<String>> getRdaGr2ProfessionOrOccupation() {
    return this.rdaGr2ProfessionOrOccupation;
  }

  public void setRdaGr2BiographicalInformation(Map<String,List<String>> rdaGr2BiographicalInformation) {
    this.rdaGr2BiographicalInformation = rdaGr2BiographicalInformation;
  }

  public Map<String,List<String>> getRdaGr2BiographicalInformation() {
    return this.rdaGr2BiographicalInformation;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null){
      return false;
    }
    if (o.getClass() == this.getClass()){
      return this.getAbout().equals(((AgentEnrichmentEntity) o).getAbout());
    }
    return false;
  }

  @Override
  public int hashCode(){
    int hash = 7;

    hash = 31 * hash + (null == this.getAbout()? 0 : this.getAbout().hashCode());
    return hash;
  }
}
