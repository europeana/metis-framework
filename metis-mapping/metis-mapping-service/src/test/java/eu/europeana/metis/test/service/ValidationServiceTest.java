package eu.europeana.metis.test.service;

import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.statistics.Statistics;
import eu.europeana.metis.mapping.statistics.StatisticsValue;
import eu.europeana.metis.mapping.validation.*;
import eu.europeana.metis.service.ValidationService;
import eu.europeana.metis.test.configuration.TestConfig;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by ymamakis on 6/27/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class ValidationServiceTest {

    @Autowired
    ValidationService service;
    ObjectId id = new ObjectId();
    @Test
    public void testCreateFlagForField(){
        Element element = new Element();
        element.setName("contributor");
        element.setPrefix("dc");
        element.setxPathFromRoot("/rdf:RDF/edm:ProvidedCHO");

        Assert.assertNotNull(service.createFlagForField(element,FlagType.BLOCKER,
                "test","is mandatory",id.toString()));

    }


    @Test
    public void testDeleteFlagForField(){
        Element element = new Element();
        element.setName("contributor");
        element.setPrefix("dc");
        element.setxPathFromRoot("/rdf:RDF/edm:ProvidedCHO");

        Assert.assertNotNull(service.createFlagForField(element,FlagType.BLOCKER,
                "test","is mandatory","testMappingId"));
        service.deleteFlagForField(element,"testMapping","test");
        Assert.assertNull(service.getFlagForField(element,id.toString(),"test"));
    }

    @Test
    public void testValidateFieldUri(){
        Element element = new Element();
        element.setName("contributor");
        element.setPrefix("dc");
        element.setxPathFromRoot("/rdf:RDF/edm:ProvidedCHO");

        ValidationRule rule = new ValidationRule();
        rule.setMessage("must be URI");
        rule.setFlagType(FlagType.BLOCKER);
        ValidationFunction function = new IsUriFunction();
        rule.setFunction(function);
        List<ValidationRule> rules = new ArrayList<>();
        rules.add(rule);
        element.setRules(rules);

        StatisticsValue statistics = new StatisticsValue();
        statistics.setValue("test cannot be uri");
        statistics.setOccurence(10l);
        List<StatisticsValue> stats = new ArrayList<>();
        stats.add(statistics);
        Statistics s = new Statistics();
        s.setValues(stats);
        s.setXpath("/rdf:RDF/edm:ProvidedCHO");
        //TODO find a way to test this and the other three
        //Assert.assertEquals(1,service.validateField(id.toString(),element).size());
    }

    @Test
    public void testValidateFieldUrl(){
        Element element = new Element();
        element.setName("contributor");
        element.setPrefix("dc");
        element.setxPathFromRoot("/rdf:RDF/edm:ProvidedCHO");

        ValidationRule rule = new ValidationRule();
        rule.setMessage("must be URI");
        rule.setFlagType(FlagType.BLOCKER);
        ValidationFunction function = new IsUrlFunction();
        rule.setFunction(function);
        List<ValidationRule> rules = new ArrayList<>();
        rules.add(rule);
        element.setRules(rules);

        StatisticsValue statistics = new StatisticsValue();
        statistics.setValue("test cannot be uri");
        statistics.setOccurence(10l);
        List<StatisticsValue> stats = new ArrayList<>();
        stats.add(statistics);
        Statistics s = new Statistics();
        s.setValues(stats);
        s.setXpath("/rdf:RDF/edm:ProvidedCHO");
       // Assert.assertEquals(1,service.validateField(id.toString(),element).size());
    }

    @Test
    public void testValidateFieldEnumeration(){
        Element element = new Element();
        element.setName("contributor");
        element.setPrefix("dc");
        element.setxPathFromRoot("/rdf:RDF/edm:ProvidedCHO");

        ValidationRule rule = new ValidationRule();
        rule.setMessage("must be URI");
        rule.setFlagType(FlagType.BLOCKER);
        IsEnumerationFunction function = new IsEnumerationFunction();
        HashSet<String> testSet = new HashSet<>();
        testSet.add("test cannot be uri");
        function.setValues(testSet);
        rule.setFunction(function);
        List<ValidationRule> rules = new ArrayList<>();
        rules.add(rule);
        element.setRules(rules);

        StatisticsValue statistics = new StatisticsValue();
        statistics.setValue("test cannot be uri");
        statistics.setOccurence(10l);
        List<StatisticsValue> stats = new ArrayList<>();
        stats.add(statistics);
        Statistics s = new Statistics();
        s.setValues(stats);
        s.setXpath("/rdf:RDF/edm:ProvidedCHO");
        //Assert.assertEquals(1,service.validateField(id.toString(),element).size());
    }
}
