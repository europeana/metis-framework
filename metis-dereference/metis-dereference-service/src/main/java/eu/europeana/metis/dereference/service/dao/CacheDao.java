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
package eu.europeana.metis.dereference.service.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.dereference.ProcessedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;


/**
 * DAO for Cache (Redis)
 * Created by ymamakis on 2/11/16.
 */

public class CacheDao implements AbstractDao<ProcessedEntity> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheDao.class);
    private Jedis jedis;
    private ObjectMapper om = new ObjectMapper();

    public CacheDao(Jedis jedis){
        this.jedis = jedis;
    }

    @Override
    public ProcessedEntity get(String resourceId) {
        try {
            String entity = jedis.get(resourceId);
            if(entity!=null) {
                return om.readValue(entity, ProcessedEntity.class);
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to read entity " + resourceId, e);
        }
        return null;
    }

    @Override
    public void save(ProcessedEntity entity) {
        try {
            jedis.set(entity.getURI(), om.writeValueAsString(entity));
        } catch (JsonProcessingException e) {
            LOGGER.warn("Unable to save entity" , e);
        }
    }

    @Override
    public void delete(String resourceId) {
        jedis.del(resourceId);
    }

    @Override
    public void update(String resourceId, ProcessedEntity entity) {
        save(entity);
    }

    /**
     * Empty the cache of processed entities
     */
    public void emptyCache(){
        jedis.flushAll();
    }
}
