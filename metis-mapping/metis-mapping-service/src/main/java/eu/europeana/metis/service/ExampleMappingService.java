package eu.europeana.metis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.ConditionMapping;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.model.SimpleMapping;
import eu.europeana.metis.mapping.statistics.DatasetStatistics;
import eu.europeana.metis.mapping.statistics.Statistics;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ymamakis on 11/11/16.
 */
public class ExampleMappingService implements MappingService {
    @Override
    public String saveMapping(Mapping mapping) {
        return null;
    }

    @Override
    public String updateMapping(Mapping mapping) {
        return null;
    }

    @Override
    public void deleteMapping(String id) {

    }

    @Override
    public Mapping getByid(String id) {
        return null;
    }

    @Override
    public Mapping getByName(String name) {
        try {
            return new ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream(
                "test_mapping.json"),Mapping.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Mapping> getMappingByOrganization(String organization) {
        return null;
    }

    @Override
    public Mapping clearValidationStatistics(String name) {
        return null;
    }

    @Override
    public <T extends Attribute> Statistics getStatisticsForField(T field, String dataset) {
        try {
            DatasetStatistics statistics = new ObjectMapper().readValue(this.getClass().getClassLoader().getResourceAsStream(
                "test_statistics.json"),DatasetStatistics.class);
            if(field.getMappings()!=null){
                SimpleMapping mapping = field.getMappings().get(0);
                return statistics.getStatistics().get(mapping.getSourceField());

            }
            if(field.getConditionalMappings()!=null){
                ConditionMapping mapping = field.getConditionalMappings().get(0);
                return statistics.getStatistics().get(mapping.getSourceField());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> getMappingNamesByOrganization(String organization) {
        return null;
    }

    @Override
    public List<String> getTemplates() {
        return null;
    }

    @Override
    public String setSchematronRulesForMapping(String mappingId, Set<String> schematronRules) {
        return null;
    }

    @Override
    public String setNamespacesForMapping(String mappingId, Map<String, String> namespaces) {
        return null;
    }

    @Override
    public void uploadXslForMapping(String mappingId, String xsl) {

    }
}
