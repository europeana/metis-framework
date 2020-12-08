package eu.europeana.enrichment.api.internal;


import java.net.URL;
import java.util.Objects;

public abstract class AbstractReferenceTerm implements ReferenceTerm{

  private final URL reference;

  public AbstractReferenceTerm(URL reference){
    this.reference = reference;
  }

  @Override
  public URL getReference(){
    return reference;
  }

  public abstract boolean equals(Object other);

  public int hashCode(){
    return Objects.hash(this.getReference());
  }
}
