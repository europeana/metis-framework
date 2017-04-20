package eu.europeana.metis.test.service;

import eu.europeana.metis.mapping.common.Value;
import eu.europeana.metis.mapping.model.*;
import eu.europeana.metis.mapping.statistics.Statistics;
import eu.europeana.metis.mapping.statistics.StatisticsValue;
import eu.europeana.metis.mapping.validation.Flag;
import eu.europeana.metis.mapping.validation.FlagType;
import eu.europeana.metis.service.MongoMappingService;
import eu.europeana.metis.service.XSDService;
import eu.europeana.metis.test.configuration.TestConfig;
import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymamakis on 6/27/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class MongoMappingServiceTest extends AbstractMappingTest {
    @Autowired
    private MongoMappingService service;
    @Autowired
    private XSDService xsdService;

    @Test
    public void testGetById() throws IOException {
        MappingSchema schema = new MappingSchema();
        schema.setId(new ObjectId());
        String id = xsdService.generateTemplateFromTgz(FileUtils.readFileToByteArray(new File(this.getClass()
                        .getClassLoader().getResource("schema.tar.gz").getFile())),
            "schema/EDM-INTERNAL.xsd", "EDM-INTERNAL-TEST","RDF", schema,generateNamespaces());
        Assert.assertNotNull(service.getByid(id));
    }

    @Test
    public void testGetByName() throws IOException {
        MappingSchema schema = new MappingSchema();
        schema.setId(new ObjectId());
        xsdService.generateTemplateFromTgz(FileUtils.readFileToByteArray(new File(this.getClass()
                        .getClassLoader().getResource("schema.tar.gz").getFile())),
            "schema/EDM-INTERNAL.xsd", "EDM-INTERNAL-TEST","RDF", schema,generateNamespaces());
        Assert.assertNotNull(service.getByName("template_EDM-INTERNAL-TEST"));
    }

    @Test
    public void testTemplates() throws IOException {
        MappingSchema schema = new MappingSchema();
        schema.setId(new ObjectId());
        xsdService.generateTemplateFromTgz(FileUtils.readFileToByteArray(new File(this.getClass()
                        .getClassLoader().getResource("schema.tar.gz").getFile())),
            "schema/EDM-INTERNAL.xsd", "EDM-INTERNAL-TEST","RDF", schema,generateNamespaces());
        Assert.assertNotNull(service.getTemplates());
    }

    @Test
    public void testSaveMapping() throws IOException {
        MappingSchema schema = new MappingSchema();
        schema.setId(new ObjectId());
        String id = xsdService.generateTemplateFromTgz(FileUtils.readFileToByteArray(new File(this.getClass()
                        .getClassLoader().getResource("schema.tar.gz").getFile())),
            "schema/EDM-INTERNAL.xsd", "EDM-INTERNAL-TEST","RDF", schema,generateNamespaces());
        Mapping m = service.getByid(id);
        m.setObjId(new ObjectId());
        m.setName("test");
        String newId = service.saveMapping(m);

        Assert.assertNotNull(newId);
        Assert.assertNotNull(service.getByid(newId));
    }

    @Test
    public void testDeleteMapping() throws IOException {
        MappingSchema schema = new MappingSchema();
        schema.setId(new ObjectId());
        String id = xsdService.generateTemplateFromTgz(FileUtils.readFileToByteArray(new File(this.getClass()
                        .getClassLoader().getResource("schema.tar.gz").getFile())),
            "schema/EDM-INTERNAL.xsd", "EDM-INTERNAL-TEST","RDF", schema,generateNamespaces());
        Mapping m = service.getByid(id);
        m.setObjId(new ObjectId());
        m.setName("test");
        String newId = service.saveMapping(m);

        Assert.assertNotNull(newId);
        Assert.assertNotNull(service.getByid(newId));
        service.deleteMapping(newId);
        Assert.assertNull(service.getByid(newId));
    }
    @Test
    public void testUpdateMapping() throws IOException {
        MappingSchema schema = new MappingSchema();
        schema.setId(new ObjectId());
        String id = xsdService.generateTemplateFromTgz(FileUtils.readFileToByteArray(new File(this.getClass()
                        .getClassLoader().getResource("schema.tar.gz").getFile())),
            "schema/EDM-INTERNAL.xsd", "EDM-INTERNAL-TEST","RDF", schema,generateNamespaces());
        Mapping m = service.getByid(id);
        m.setObjId(new ObjectId());
        m.setName("test");
        String newId = service.saveMapping(m);
        Assert.assertNotNull(newId);
        Assert.assertNotNull(service.getByid(newId));
        Mapping mapping = service.getByid(newId);
        mapping.setOrganization("TestOrg");
        String lastId = service.updateMapping(mapping);
        Mapping s = service.getByid(lastId);
        Assert.assertNotNull(s);
        Assert.assertEquals(s.getOrganization(),"TestOrg");
    }
    @Test
    public void testGetMappingByOrganization() throws IOException {
        MappingSchema schema = new MappingSchema();
        schema.setId(new ObjectId());
        String id = xsdService.generateTemplateFromTgz(FileUtils.readFileToByteArray(new File(this.getClass()
                        .getClassLoader().getResource("schema.tar.gz").getFile())),
            "schema/EDM-INTERNAL.xsd", "EDM-INTERNAL-TEST","RDF", schema,generateNamespaces());
        Mapping m = service.getByid(id);
        m.setObjId(new ObjectId());
        m.setName("test");
        String newId = service.saveMapping(m);
        Assert.assertNotNull(newId);
        Assert.assertNotNull(service.getByid(newId));
        Mapping mapping = service.getByid(newId);
        mapping.setOrganization("TestOrg");
        service.updateMapping(mapping);
        List<Mapping> s = service.getMappingByOrganization("TestOrg");
        Assert.assertNotNull(s);
        Assert.assertEquals(s.size(),1);
        service.deleteMapping(mapping.getObjId().toString());
    }

    @Test
    public void testGetMappingNamesByOrganization() throws IOException {
        MappingSchema schema = new MappingSchema();
        schema.setId(new ObjectId());
        String id = xsdService.generateTemplateFromTgz(FileUtils.readFileToByteArray(new File(this.getClass()
                        .getClassLoader().getResource("schema.tar.gz").getFile())),
            "schema/EDM-INTERNAL.xsd", "EDM-INTERNAL-TEST","RDF", schema,generateNamespaces());
        Mapping m = service.getByid(id);
        m.setObjId(new ObjectId());
        m.setName("test");
        String newId = service.saveMapping(m);
        Assert.assertNotNull(newId);
        Assert.assertNotNull(service.getByid(newId));
        Mapping mapping = service.getByid(newId);
        mapping.setOrganization("TestOrg");
        service.updateMapping(mapping);
        List<String> s = service.getMappingNamesByOrganization("TestOrg");
        Assert.assertNotNull(s);
        Assert.assertEquals(s.size(),1);
        Assert.assertEquals(s.get(0),"test");
        service.deleteMapping(mapping.getObjId().toString());
    }

    @Test
    public void testClearValidationStatistics() throws IOException {
        MappingSchema schema = new MappingSchema();
        schema.setId(new ObjectId());
        String id = xsdService.generateTemplateFromTgz(FileUtils.readFileToByteArray(new File(this.getClass()
                        .getClassLoader().getResource("schema.tar.gz").getFile())),
            "schema/EDM-INTERNAL.xsd", "EDM-INTERNAL-TEST","RDF", schema,generateNamespaces());
        Mapping m = service.getByid(id);
        m.setObjId(new ObjectId());
        m.setName("test");
        Mappings mappings = m.getMappings();
        List<Element> parentElements = new ArrayList<>();
        for(Element element : mappings.getElements()){
            appendStatisticsAndFlags(element,m.getObjId().toString());
            if(element.getAttributes()!=null && element.getAttributes().size()>0){
                List<Attribute> attributes = new ArrayList<>();
                for (Attribute attr:element.getAttributes()) {
                    attributes.add(appendStatisticsAndFlags(attr,m.getObjId().toString()));
                }
                element.setAttributes(attributes);
            }
            if(element.getElements()!=null && element.getElements().size()>0){
                List<Element> children = new ArrayList<>();
                for(Element child:element.getElements()){
                    children.add(appendStatisticsAndFlags(child,m.getObjId().toString()));
                }

            }
            parentElements.add(element);
        }
        mappings.setElements(parentElements);
        m.setMappings(mappings);
        service.saveMapping(m);
        Mapping ret1 = service.getByName(m.getName());
        Assert.assertEquals(3,ret1.getMappings().getElements().get(0).getFlags().size());
        ret1 = service.clearValidationStatistics(ret1.getName());
        service.updateMapping(ret1);
        Mapping ret = service.getByName(ret1.getName());
        Assert.assertNull(ret.getMappings().getElements().get(0).getFlags());
    }

    private <T extends Attribute> T appendStatisticsAndFlags(T child,String mappingId) {
        Statistics statistics = new Statistics();
        statistics.setXpath(child.getxPathFromRoot());
        List<StatisticsValue> values = new ArrayList<>();
        StatisticsValue value = new StatisticsValue();
        value.setOccurence(10);
        value.setValue("test");
        values.add(value);
        statistics.setValues(values);
        Flag flag = new Flag();
        flag.setMappingId(mappingId);
        Value val = new Value();
        val.setValue("testValue");
        flag.setValue(val);
        flag.setMessage("test message");
        flag.setFlagType(FlagType.WARNING);
        Flag flag1 = new Flag();
        flag1.setMappingId(mappingId);
        Value val1 = new Value();
        val1.setValue("testValue");
        flag1.setValue(val1);
        flag1.setMessage("test message");
        flag1.setFlagType(FlagType.BLOCKER);
        Flag flag2 = new Flag();
        flag2.setMappingId(mappingId);
        Value val2 = new Value();
        val2.setValue("testValue");
        flag2.setValue(val2);
        flag2.setMessage("test message");
        flag2.setFlagType(FlagType.OK);
        List<Flag> flags = new ArrayList<>();
        flags.add(flag);
        flags.add(flag1);
        flags.add(flag2);
        child.setFlags(flags);
        return child;
    }
}
