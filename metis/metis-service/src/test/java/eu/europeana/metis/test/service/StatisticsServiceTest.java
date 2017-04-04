package eu.europeana.metis.test.service;

import eu.europeana.metis.mapping.statistics.DatasetStatistics;
import eu.europeana.metis.service.StatisticsService;
import eu.europeana.metis.test.configuration.TestConfig;
import eu.europeana.metis.utils.ArchiveUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;

/**
 * Created by ymamakis on 6/27/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class StatisticsServiceTest {

    @Autowired
    StatisticsService service;

    @Test
    @Ignore
    public void testCalculateStatistics() throws IOException, XMLStreamException {
       List<String> records =  ArchiveUtils.extractRecords(this.getClass().getClassLoader().
                getResourceAsStream("Repox_Import_2059205-valid_items_transform_8313.tgz"));
        DatasetStatistics statistics = service.calculateStatistics("testdataset",records);
        Assert.assertNotNull(statistics);
        Assert.assertNotNull(statistics.getStatistics().get("/rdf:RDF/edm:ProvidedCHO@rdf:about"));
        //TODO fix!!
       // Assert.assertTrue(statistics.getStatistics().get("/rdf:RDF/edm:ProvidedCHO@rdf:about")
       //         .getValues().iterator().next().getOccurence()>0);
    }
}
