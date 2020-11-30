package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.RDF;
import java.net.URL;
import java.util.List;

public class FieldType {

  private List<EntityType> candidateTypes;

  public void appendReferenceToRecord(URL url, RDF rdf){

  }

  public List<EntityType> getCandidateTypes() {
    return candidateTypes;
  }

  public void setCandidateTypes(List<EntityType> candidateTypes) {
    this.candidateTypes = candidateTypes;
  }
}
