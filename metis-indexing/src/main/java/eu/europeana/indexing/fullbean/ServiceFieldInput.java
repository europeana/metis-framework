package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.definitions.jibx.Service;
import eu.europeana.corelib.solr.entity.ServiceImpl;

/**
 * Converts a {@link Service} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to a
 * {@link ServiceImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
class ServiceFieldInput {

  ServiceImpl createServiceMongoFields(Service service) {
    ServiceImpl serv = new ServiceImpl();
    serv.setAbout(service.getAbout());
    if (service.getConformsToList() != null) {
      serv.setDcTermsConformsTo(
          FieldInputUtils.resourceOrLiteralListToArray(service.getConformsToList()));
    }

    if (service.getImplementList() != null) {
      serv.setDoapImplements(FieldInputUtils.resourceListToArray(service.getImplementList()));
    }
    return serv;
  }
}
