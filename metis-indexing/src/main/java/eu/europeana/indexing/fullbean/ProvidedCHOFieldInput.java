package eu.europeana.indexing.fullbean;

import java.util.function.Function;
import eu.europeana.metis.schema.jibx.ProvidedCHOType;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;

/**
 * Converts a {@link ProvidedCHOType} from an {@link eu.europeana.metis.schema.jibx.RDF} to a
 * {@link ProvidedCHOImpl} for a {@link eu.europeana.metis.schema.edm.beans.FullBean}.
 */
final class ProvidedCHOFieldInput implements Function<ProvidedCHOType, ProvidedCHOImpl> {

  @Override
  public ProvidedCHOImpl apply(ProvidedCHOType providedCHO) {
    ProvidedCHOImpl mongoProvidedCHO = new ProvidedCHOImpl();
    mongoProvidedCHO.setAbout(providedCHO.getAbout());

    mongoProvidedCHO.setOwlSameAs(FieldInputUtils.resourceListToArray(providedCHO.getSameAList()));
    return mongoProvidedCHO;
  }
}
