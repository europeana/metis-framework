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

  @Override
  public boolean equals(Object other){
    if(other == this){
      return true;
    }

    if(other == null || getClass() != other.getClass()){
      return false;
    }

    ReferenceTermContext o = (ReferenceTermContext) other;

    return Objects.equals(o.getReference(), this.getReference());

  }

  @Override
  public int hashCode(){
    return Objects.hash(this.getReference());
  }
}
