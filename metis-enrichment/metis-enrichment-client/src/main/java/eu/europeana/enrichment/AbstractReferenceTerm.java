package eu.europeana.enrichment;


import java.net.URL;

public abstract class AbstractReferenceTerm implements ReferenceTerm{

  private final URL reference;

  public AbstractReferenceTerm(URL reference){
    this.reference = reference;
  }

  public URL getReference(){
    return reference;
  }

}
