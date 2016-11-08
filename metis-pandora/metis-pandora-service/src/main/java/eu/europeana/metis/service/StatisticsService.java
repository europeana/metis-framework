/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.service;

import eu.europeana.metis.mapping.persistence.DatasetStatisticsDao;
import eu.europeana.metis.mapping.persistence.StatisticsDao;
import eu.europeana.metis.mapping.statistics.DatasetStatistics;
import eu.europeana.metis.mapping.statistics.Statistics;
import eu.europeana.metis.mapping.utils.XMLUtils;
import org.mongodb.morphia.query.ArraySlice;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLStreamException;
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
    private StatisticsDao statisticsDao;

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
            XMLUtils.analyzeRecord(datasetId,record,statisticsMap);
        }
        statistics.setStatistics(statisticsMap);
        for(Map.Entry<String,Statistics> statisticsEntry:statisticsMap.entrySet()){
            statisticsDao.save(statisticsEntry.getValue());

        }
        dao.save(statistics);
        return statistics;
    }

    public DatasetStatistics get(String datasetId, int from, int to){
        Query<DatasetStatistics> query = dao.getDatastore().createQuery(DatasetStatistics.class);
        query.filter("datasetId",datasetId);
        query.project("statistics",new ArraySlice(from,to-from));
        return dao.findOne(query);
    }



}
