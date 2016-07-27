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
package eu.europeana.hierarchies.service.cache;

import org.codehaus.jackson.map.ObjectMapper;
import redis.clients.jedis.Jedis;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Redis Implementation of Cache
 * Created by ymamakis on 1/25/16.
 */
public class RedisDAO implements CacheDAO{

    public RedisDAO(){

    }

    public RedisDAO(Jedis jedis){
        this.jedis= jedis;
    }

    @Inject
    private Jedis jedis;
    private ObjectMapper objMapper = new ObjectMapper();


    @Override
    public void addParentToSet(String collection, String parent) throws IOException {
        String entry = jedis.get(collection);
        CacheEntry cache = new CacheEntry();
        Set<String> parents = new HashSet<String>();
        if(entry!=null){
            cache = objMapper.readValue(entry,CacheEntry.class);
            parents = cache.getParents();
        }
        parents.add(parent);
        cache.setParents(parents);
        cache.setCollection(collection);
        jedis.set(collection, objMapper.writeValueAsString(cache));

    }

    @Override
    public void addParentsToSet(String collection, Set<String> parents)throws IOException {
        String entry = jedis.get(collection);
        CacheEntry cache = new CacheEntry();
        Set<String> parentsToAdd = new HashSet<String>();
        if(entry!=null){
            cache = objMapper.readValue(entry,CacheEntry.class);
            parents = cache.getParents();
        }
        parents.addAll(parents);
        cache.setParents(parentsToAdd);
        cache.setCollection(collection);
        jedis.set(collection, objMapper.writeValueAsString(cache));
    }

    @Override
    public CacheEntry getByCollection(String collection) throws IOException {
        String entry = jedis.get(collection);
        if(entry!=null){
            return objMapper.readValue(entry,CacheEntry.class);
        }
        return null;
    }
}
