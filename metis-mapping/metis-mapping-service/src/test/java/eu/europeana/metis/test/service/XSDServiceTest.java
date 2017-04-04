package eu.europeana.metis.test.service;

/**
 * Created by ymamakis on 6/27/16.
 */

import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.model.MappingSchema;
import eu.europeana.metis.mapping.persistence.MongoMappingDao;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class XSDServiceTest extends AbstractMappingTest{
    @Autowired
    XSDService service;

    @Autowired
    MongoMappingDao dao;
    @Test
    public void test() throws IOException{
        MappingSchema schema = new MappingSchema();
        schema.setId(new ObjectId());
            String id = service.generateTemplateFromTgz(FileUtils.readFileToByteArray(new File(this.getClass()
                    .getClassLoader().getResource("schema.tar.gz").getFile())),
                "schema/EDM-INTERNAL.xsd", "EDM-INTERNAL-TEST","RDF", schema ,generateNamespaces());
            Mapping mapping = dao.get(new ObjectId(id));
            Assert.assertEquals("template_EDM-INTERNAL-TEST",mapping.getName());
            Assert.assertNotNull(mapping.getMappings());
            Assert.assertNotNull(mapping.getMappings().getElements());
            Assert.assertNotNull(mapping.getMappings().getNamespaces());
            Assert.assertFalse(mapping.getMappings().isHasMappings());
    }



    @Test
    public void testUrl() throws IOException {
        MappingSchema schema = new MappingSchema();
        schema.setId(new ObjectId());
        String id = service.generateTemplateFromTgzUrl(this.getClass().getClassLoader().getResource(
            "schema.tar.gz").toString(),
            "schema/EDM-INTERNAL.xsd", "EDM-INTERNAL-TEST","RDF", schema, generateNamespaces());
        Mapping mapping = dao.get(new ObjectId(id));
        Assert.assertEquals("template_EDM-INTERNAL-TEST",mapping.getName());
        Assert.assertNotNull(mapping.getMappings());
        Assert.assertNotNull(mapping.getMappings().getElements());
        Assert.assertNotNull(mapping.getMappings().getNamespaces());
        Assert.assertFalse(mapping.getMappings().isHasMappings());
    }
}
