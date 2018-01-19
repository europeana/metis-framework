/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.enrichment.service;

import eu.europeana.enrichment.utils.MongoDatabaseUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author ymamakis
 */
@Service
public class EntityRemover {

    private final String mongoHost;
    private final int mongoPort;
    private RedisInternalEnricher enricher;

    @Autowired
    public EntityRemover(RedisInternalEnricher enricher, String mongoHost, int mongoPort){
        this.mongoHost = mongoHost;
        this.mongoPort = mongoPort;
        this.enricher = enricher;
    }

    public void remove(List<String> uris) {
        MongoDatabaseUtils.dbExists(mongoHost, mongoPort);
        
        List<String> retUris = MongoDatabaseUtils.delete(uris);
        enricher.remove(retUris);
    }
}