package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.definitions.jibx.Service;
import eu.europeana.corelib.solr.entity.ServiceImpl;

/**
 * Created by ymamakis on 1/12/16.
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
