package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.definitions.jibx.ProvidedCHOType;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;

/**
 * Converts a {@link ProvidedCHOType} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to a
 * {@link ProvidedCHOImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class ProvidedCHOFieldInput {

  ProvidedCHOImpl createProvidedCHOMongoFields(ProvidedCHOType providedCHO) {
    ProvidedCHOImpl mongoProvidedCHO = new ProvidedCHOImpl();
    mongoProvidedCHO.setAbout(providedCHO.getAbout());

    mongoProvidedCHO.setOwlSameAs(FieldInputUtils.resourceListToArray(providedCHO.getSameAList()));
    return mongoProvidedCHO;
  }
}
