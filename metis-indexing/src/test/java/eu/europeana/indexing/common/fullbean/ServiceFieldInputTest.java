package eu.europeana.indexing.common.fullbean;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.solr.entity.ServiceImpl;
import eu.europeana.metis.schema.jibx.ConformsTo;
import eu.europeana.metis.schema.jibx.Implements;
import eu.europeana.metis.schema.jibx.Label;
import eu.europeana.metis.schema.jibx.LiteralType.Lang;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.metis.schema.jibx.Service;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServiceFieldInputTest {
  @Test
  void testService() {
    Service service = getService();
    ServiceImpl serviceImpl = new ServiceFieldInput().apply(service);
    assertServiceFieldInput(service, serviceImpl);
  }

  private static void assertServiceFieldInput(Service service, ServiceImpl serviceImpl) {
    assertEquals(service.getAbout(), serviceImpl.getAbout());
    assertEquals(service.getConformsToList().getFirst().getResource().getResource(), serviceImpl.getDctermsConformsTo()[0]);
    assertEquals(service.getImplementList().getFirst().getResource(), serviceImpl.getDoapImplements()[0]);
    assertTrue(serviceImpl.getRdfsLabel().containsKey(service.getLabelList().getFirst().getLang().getLang()));
    assertEquals(service.getLabelList().getFirst().getString(), serviceImpl.getRdfsLabel().values().iterator().next().getFirst());
  }

  private static Service getService() {
    Service service = new Service();
    service.setAbout("about");

    Lang lang = new Lang();
    lang.setLang("en");

    List<Label> labelList = new ArrayList<>();
    Label label = new Label();
    label.setLang(lang);
    label.setString("label");
    labelList.add(label);
    service.setLabelList(labelList);

    List<ConformsTo> conformsToList = new ArrayList<>();
    ConformsTo conformsTo = new ConformsTo();
    Resource conformsToResource = new Resource();
    conformsToResource.setResource("resource");
    conformsTo.setResource(conformsToResource);
    conformsToList.add(conformsTo);
    service.setConformsToList(conformsToList);

    List<Implements> implementList = new ArrayList<>();
    Implements implement = new Implements();
    implement.setResource("resource");
    implementList.add(implement);
    service.setImplementList(implementList);

    return service;
  }
}
