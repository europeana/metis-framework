package eu.europeana.enrichment;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.List;

public abstract class ReferenceTerm {

  private URL reference;

  public ReferenceTerm(URL reference){
    this.reference = reference;
  }

  public URL getReference(){
    return reference;
  }

  public abstract List<EntityType> getCandidateTypes();

  public abstract boolean equals(ReferenceTerm referenceTerm);

  public abstract int hashCode();

}
