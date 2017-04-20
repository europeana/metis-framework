package eu.europeana.metis.test.service;

/**
 * Created by ymamakis on 6/27/16.
 */

import eu.europeana.metis.mapping.model.Clause;
import eu.europeana.metis.mapping.model.ConditionMapping;
import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.Function;
import eu.europeana.metis.mapping.model.FunctionType;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.model.MappingSchema;
import eu.europeana.metis.mapping.model.MappingType;
import eu.europeana.metis.mapping.model.XPathHolder;
import eu.europeana.metis.mapping.persistence.MongoMappingDao;
import eu.europeana.metis.service.XSDService;
import eu.europeana.metis.service.XSLTGenerationService;
import eu.europeana.metis.test.configuration.TestConfig;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestConfig.class})
public class XSLTGenerationServiceTest extends AbstractMappingTest{

    @Autowired
    XSLTGenerationService service;

    @Autowired
    XSDService xsdService;


    @Autowired
    MongoMappingDao dao;
    @Test
    public void test() throws IOException {
        MappingSchema s = new MappingSchema();
        s.setName("EDM-EXTERNAL-TEST");
        String id = xsdService.generateTemplateFromTgz(FileUtils.readFileToByteArray(new File(this.getClass()
                        .getClassLoader().getResource("schema.tar.gz").getFile())),
            "schema/EDM.xsd", "EDM-EXTERNAL-TEST","RDF",s,generateNamespaces());
        Mapping mapping = dao.get(new ObjectId(id));
        List<Element> elements = mapping.getMappings().getElements();
        Element elem = elements.get(0);
        List<Element> children = elem.getElements();
        Element child = children.get(0);
        Function function = new Function();
        function.setType(FunctionType.FUNCTION_CALL_SUBSTRING_AFTER);
        function.setArguments(new String[]{"test"});
        ConditionMapping sMapping = new ConditionMapping();
        sMapping.setType(MappingType.XPATH);
        sMapping.setFunction(function);
        sMapping.setSourceField("dc:contributor");
        List<ConditionMapping> simpleMappings = new ArrayList<>();
        sMapping.setConditionalLogicalOperator("AND");
        List<Clause> clauses = new ArrayList<>();
        Clause clause = new Clause();
        clause.setConditionRelationOperator("CONTAINS");
        clause.setxPathMapping("dc:contributor@rdf:resource");
        clause.setValueMapping("test");
        clauses.add(clause);
        sMapping.setClauses(clauses);
        simpleMappings.add(sMapping);
        child.setConditionalMappings(simpleMappings);
        children.set(0,child);
        elem.setElements(children);
        elem.setHasMapping(true);
        elements.set(0,elem);
        mapping.getMappings().setElements(elements);
        mapping.getMappings().setHasMappings(true);
        MappingSchema schema = mapping.getTargetSchema();

        XPathHolder holder = new XPathHolder();
        holder.setXpath("/repoxWrap");
        holder.setUriPrefix("rdf");
        holder.setName("RDF");
        schema.setRootPath(holder);
        mapping.setTargetSchema(schema);
        System.out.println("XSL is:" +service.generateXslFromMapping(mapping));

    }
}
