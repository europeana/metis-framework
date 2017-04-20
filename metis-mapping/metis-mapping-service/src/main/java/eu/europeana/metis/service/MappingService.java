package eu.europeana.metis.service;

import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.statistics.Statistics;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ymamakis on 11/11/16.
 */
public interface MappingService {
    String saveMapping(Mapping mapping);

    String updateMapping(Mapping mapping);

    void deleteMapping(String id);

    Mapping getByid(String id);

    Mapping getByName(String name);

    List<Mapping> getMappingByOrganization(String organization);

    Mapping clearValidationStatistics(String name);

    <T extends Attribute> Statistics getStatisticsForField(T field, String dataset);

    List<String> getMappingNamesByOrganization(String organization);

    List<String> getTemplates();

    String setSchematronRulesForMapping(String mappingId, Set<String> schematronRules);

    String setNamespacesForMapping(String mappingId, Map<String, String> namespaces);

    void uploadXslForMapping(String mappingId, String xsl);
}
