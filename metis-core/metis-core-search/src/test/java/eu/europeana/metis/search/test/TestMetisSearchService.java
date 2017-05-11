package eu.europeana.metis.search.test;

import eu.europeana.metis.core.search.service.MetisSearchService;
import junit.framework.Assert;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gmamakis on 22-2-17.
 */
@ContextConfiguration(classes=Config.class, loader=AnnotationConfigContextLoader.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestMetisSearchService {
    @Autowired
    MetisSearchService searchService;

    @Test
    public void testSearchService(){
        List<String> searchLabels = new ArrayList<>();
        searchLabels.add("test");

        try {
            searchService.addOrganizationForSearch("testId", "testOrganizationId", "testLabel",searchLabels);
            Assert.assertEquals(1,searchService.getSuggestions("tes").size());
            Assert.assertEquals("testLabel",searchService.getSuggestions("tes").get(0).getEngLabel());

            searchService.deleteFromSearch("testId");
            Assert.assertEquals(0,searchService.getSuggestions("tes").size());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
    }
}
