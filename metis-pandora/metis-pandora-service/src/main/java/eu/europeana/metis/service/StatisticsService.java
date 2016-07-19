package eu.europeana.metis.service;

import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.model.Mappings;
import eu.europeana.metis.mapping.persistence.DatasetStatisticsDao;
import eu.europeana.metis.mapping.statistics.DatasetStatistics;
import eu.europeana.metis.mapping.statistics.Statistics;
import eu.europeana.metis.mapping.utils.XMLUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  A statistics calculation service
 *
 * Created by ymamakis on 6/15/16.
 */
@Service
public class StatisticsService {


    @Autowired
    private DatasetStatisticsDao dao;

    @Autowired
    private MongoMappingService mappingService;

    /**
     * Calculate the statistics for a dataset
     * @param datasetId The id of the dataset
     * @param records The list of records
     * @return The dataset statistics
     * @throws XMLStreamException
     */
    public DatasetStatistics calculateStatistics(String datasetId,List<String> records) throws XMLStreamException{
        DatasetStatistics statistics = new DatasetStatistics();
        statistics.setDatasetId(datasetId);
        Map<String,Statistics> statisticsMap = new HashMap<>();
        for(String record:records){
            XMLUtils.analyzeRecord(record,statisticsMap);
        }
        statistics.setStatistics(statisticsMap);
        dao.save(statistics);
        return statistics;
    }


    /**
     * Append the statistics for the mapping
     * @param datasetId The dataset id
     * @param mapping The mapping
     * @return The populated mapping
     */
    public Mapping appendStatisticsToMapping(String datasetId, Mapping mapping){
        DatasetStatistics statistics = dao.find(dao.createQuery().filter("datasetId",datasetId)).get();
        if(statistics!=null){
            Mappings mappings = mapping.getMappings();
            if (mappings.getAttributes()!=null && mappings.getAttributes().size()>0){
                mappings.setAttributes(populateFieldStatistics(mappings.getAttributes(),statistics));
            }

            if(mappings.getElements()!=null && mappings.getElements().size()>0){
                mappings.setElements(populateFieldStatistics(mappings.getElements(),statistics));
            }
        }
        mappingService.updateMapping(mapping);
        return mapping;
    }

    private <T extends Attribute> List<T> populateFieldStatistics(List<T> fields,DatasetStatistics statistics){
        List<T> fieldsCopy = new ArrayList<>();
        Map<String, Statistics> map = statistics.getStatistics();
        for(T field : fields){
            if(map.containsKey(field.getxPathFromRoot()+"/"+field.getPrefix()+":"+field.getName())){
                field.setStatistics(map.get(field.getxPathFromRoot()+"/"+field.getPrefix()+":"+field.getName()));
            }
            if(field.getClass().isAssignableFrom(Element.class)){
                if (((Element)field).getAttributes()!=null && ((Element)field).getAttributes().size()>0){
                    ((Element)field).setAttributes(populateFieldStatistics(((Element)field).getAttributes(),statistics));
                }

                if(((Element)field).getElements()!=null && ((Element)field).getElements().size()>0){
                    ((Element)field).setElements(populateFieldStatistics(((Element)field).getElements(),statistics));
                }
            }

            fieldsCopy.add(field);
        }
        return fieldsCopy;
    }

}
