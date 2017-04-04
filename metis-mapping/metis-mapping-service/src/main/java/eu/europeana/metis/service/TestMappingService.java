package eu.europeana.metis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.statistics.Statistics;

import java.io.IOException;

/**
 * Created by ymamakis on 11/11/16.
 */
public class TestMappingService {

    public static void main(String args[]){
        try {
            ExampleMappingService service = new ExampleMappingService();
            Mapping mapping = new ObjectMapper().readValue(TestMappingService.class.getClassLoader().getResourceAsStream(
                "test_mapping.json"),Mapping.class);
            Statistics stats = service.getStatisticsForField(mapping.getMappings().getElements().get(0).getAttributes().get(0),null);
            System.out.println(new ObjectMapper().writeValueAsString(stats));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
