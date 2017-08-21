/*
a * Copyright 2005-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.enrichment.service;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.metis.utils.EntityClass;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.metis.utils.InputValue;
import eu.europeana.enrichment.api.external.ObjectIdSerializer;
import eu.europeana.enrichment.api.internal.*;
import eu.europeana.enrichment.utils.MongoDatabaseUtils;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main enrichment class
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
@SuppressWarnings("rawtypes")
@Component
public class MemcachedInternalEnricher {

    private final static ObjectMapper obj = new ObjectMapper();

    private MemcachedClient client;
    private String mongoHost;
    public MemcachedInternalEnricher(String mongoHost, MemcachedProvider provider) {
        this.mongoHost = mongoHost;
        SimpleModule sm = new SimpleModule("test", Version.unknownVersion());
        sm.addSerializer(new ObjectIdSerializer());
        obj.registerModule(sm);
        System.out.println("Using memcached");
        client = provider.getClient();
        if(client.get("enrichmentstatus")!=null){
            System.out.println("Status does not exist");

            populate();
        } else {
            System.out.println("Status exists");
        }

    }

    public void recreate(){
        client.delete("enrichmentstatus");
        populate();
    }
    public void remove(List<String> uris) {
        for (String str : uris) {
            client.delete("concept:parent:"+str);
            client.delete("agent:parent:"+str);
            client.delete("timespan:parent:"+str);
            client.delete("place:parent:"+str);
            client.delete("concept:uri:"+str);
            client.delete("agent:uri:"+str);
            client.delete("timespan:uri:"+str);
            client.delete("place:uri:"+str);
            List<String> conceptLangValues = (List<String>)client.get("concept:entity:"+str);
            for(String key:conceptLangValues){
                List<String> clientUris = (List<String>) client.get(key);
                clientUris.remove(str);
                client.add(key,100000,clientUris);
            }
            client.delete("concept:entity:"+str);
            List<String> agentLangValues = (List<String>)client.get("agent:entity:"+str);
            for(String key:agentLangValues){
                List<String> clientUris = (List<String>) client.get(key);
                clientUris.remove(str);
                client.add(key,100000,clientUris);
            }
            client.delete("agent:entity:"+str);
            List<String> placeLangValues = (List<String>)client.get("place:entity:"+str);
            for(String key:placeLangValues){
                List<String> clientUris = (List<String>) client.get(key);
                clientUris.remove(str);
                client.add(key,100000,clientUris);
            }
            client.delete("place:entity:"+str);
            List<String> timespanLangValues = (List<String>)client.get("timespan:entity:"+str);
            for(String key:timespanLangValues){
                List<String> clientUris = (List<String>) client.get(key);
                clientUris.remove(str);
                client.add(key,100000,clientUris);
            }
            client.delete("timespan:entity:"+str);

        }
    }

    public void populate() {
        client.add("enrichmentstatus",1000000, "started");
        System.out.println("Initializing");
        MongoDatabaseUtils.dbExists(mongoHost, 27017);
        List<MongoTerm> agentsMongo = MongoDatabaseUtils.getAllAgents();
        System.out.println("Found agents: " + agentsMongo.size());
        for (MongoTerm agent : agentsMongo) {

            AgentTermList atl = (AgentTermList) MongoDatabaseUtils.findByCode(agent.getCodeUri(), "people");
            if (atl != null) {
                try {
                    EntityWrapper ag = new EntityWrapper();
                    ag.setOriginalField("");
                    ag.setClassName(AgentImpl.class.getName());
                    ag.setContextualEntity(this.getObjectMapper().writeValueAsString(atl.getRepresentation()));
                    ag.setUrl(agent.getCodeUri());
                    ag.setOriginalValue(agent.getOriginalLabel());
                    List<String> agentDefLabel = (List<String>) client.get("agent:entity:def:"+StringUtils.replace(agent.getLabel()," ",""));
                    if(agentDefLabel==null){
                        agentDefLabel = new ArrayList<>();
                    }
                    agentDefLabel.add(agent.getCodeUri());
                    client.add("agent:entity:def:"+StringUtils.replace(agent.getLabel()," ",""),1000000,agentDefLabel);
                    List<String> agentCodeUri = (List<String>)client.get("agent:entity:"+agent.getCodeUri());
                    if(agent.getCodeUri()==null){
                        agentCodeUri = new ArrayList<>();
                    }
                    agentCodeUri.add("agent:entity:def:"+StringUtils.replace(agent.getLabel()," ",""));
                    client.add("agent:entity:"+agent.getCodeUri(),1000000,agentCodeUri);
                    if(agent.getLang()!=null){
                        List<String> agentLangLabel = (List<String>) client.get("agent:entity:"+agent.getLang()+":"+
                                StringUtils.replace(agent.getLabel()," ",""));
                        if(agentLangLabel==null){
                            agentLangLabel = new ArrayList<>();
                        }
                        agentLangLabel.add(agent.getCodeUri());
                        client.add("agent:entity:"+agent.getLang()+":"+StringUtils.replace(agent.getLabel()," ",""),1000000,agentLangLabel);
                    }
                    client.add("agent:uri:"+agent.getCodeUri(),1000000,obj.writeValueAsString(ag));

                    List<String> parents = this.findAgentParents(atl.getParent());
                    if(parents!=null && parents.size()>0) {
                        client.add("agent:parent:" + agent.getCodeUri(),1000000, parents);
                    }


                    if (atl.getOwlSameAs() != null) {
                        for (String sameAs : atl.getOwlSameAs()) {
                            client.add("agent:sameas:"+sameAs,1000000,agent.getCodeUri());
                        }
                    }
                } catch (IOException var14) {
                    var14.printStackTrace();
                }
            }
        }


        List<MongoTerm> conceptsMongo1 = MongoDatabaseUtils.getAllConcepts();
        System.out.println("Found concepts: " + conceptsMongo1.size());
        for (MongoTerm concept : conceptsMongo1) {
            ConceptTermList atl = (ConceptTermList) MongoDatabaseUtils.findByCode(concept.getCodeUri(), "concept");
            if (atl != null) {
                try {
                    EntityWrapper ag = new EntityWrapper();
                    ag.setOriginalField("");
                    ag.setClassName(ConceptImpl.class.getName());
                    ag.setContextualEntity(this.getObjectMapper().writeValueAsString(atl.getRepresentation()));
                    ag.setUrl(concept.getCodeUri());
                    ag.setOriginalValue(concept.getOriginalLabel());
                    List<String> agentDefLabel = (List<String>) client.get("concept:entity:def:"+StringUtils.replace(concept.getLabel()," ",""));
                    if(agentDefLabel==null){
                        agentDefLabel = new ArrayList<>();
                    }
                    agentDefLabel.add(concept.getCodeUri());
                    client.add("concept:entity:def:"+StringUtils.replace(concept.getLabel()," ",""),1000000,agentDefLabel);
                    List<String> agentCodeUri = (List<String>)client.get("concept:entity:"+concept.getCodeUri());
                    if(concept.getCodeUri()==null){
                        agentCodeUri = new ArrayList<>();
                    }
                    agentCodeUri.add("concept:entity:def:"+StringUtils.replace(concept.getLabel()," ",""));
                    client.add("concept:entity:"+concept.getCodeUri(),1000000,agentCodeUri);
                    if(concept.getLang()!=null){
                        List<String> agentLangLabel = (List<String>) client.get("concept:entity:"+concept.getLang()+":"+StringUtils.replace(concept.getLabel()," ",""));
                        if(agentLangLabel==null){
                            agentLangLabel = new ArrayList<>();
                        }
                        agentLangLabel.add(concept.getCodeUri());
                        client.add("concept:entity:"+concept.getLang()+":"+StringUtils.replace(concept.getLabel()," ",""),1000000,agentLangLabel);
                    }
                    client.add("concept:uri:"+concept.getCodeUri(),1000000,obj.writeValueAsString(ag));

                    List<String> parents = this.findAgentParents(atl.getParent());
                    if(parents!=null && parents.size()>0) {
                        client.add("concept:parent:" + concept.getCodeUri(),1000000, parents);
                    }


                    if (atl.getOwlSameAs() != null) {
                        for (String sameAs : atl.getOwlSameAs()) {
                            client.add("concept:sameas:"+sameAs,1000000,concept.getCodeUri());
                        }
                    }
                } catch (IOException var14) {
                    var14.printStackTrace();
                }
            }
        }


        List<MongoTerm> placesMongo2 = MongoDatabaseUtils.getAllPlaces();
        System.out.println("Found places: " + placesMongo2.size());

        for (MongoTerm place : placesMongo2) {
            PlaceTermList atl = (PlaceTermList) MongoDatabaseUtils.findByCode(place.getCodeUri(), "place");
            if (atl != null) {
                try {
                    EntityWrapper ag = new EntityWrapper();
                    ag.setOriginalField("");
                    ag.setClassName(PlaceImpl.class.getName());
                    ag.setContextualEntity(this.getObjectMapper().writeValueAsString(atl.getRepresentation()));
                    ag.setUrl(place.getCodeUri());
                    ag.setOriginalValue(place.getOriginalLabel());
                    List<String> agentDefLabel = (List<String>) client.get("place:entity:def:"+StringUtils.replace(place.getLabel()," ",""));
                    if(agentDefLabel==null){
                        agentDefLabel = new ArrayList<>();
                    }
                    agentDefLabel.add(place.getCodeUri());
                    client.add("place:entity:def:"+StringUtils.replace(place.getLabel()," ",""),1000000,agentDefLabel);
                    List<String> agentCodeUri = (List<String>)client.get("place:entity:"+place.getCodeUri());
                    if(place.getCodeUri()==null){
                        agentCodeUri = new ArrayList<>();
                    }
                    agentCodeUri.add("place:entity:def:"+StringUtils.replace(place.getLabel()," ",""));
                    client.add("place:entity:"+place.getCodeUri(),1000000,agentCodeUri);
                    if(place.getLang()!=null){
                        List<String> agentLangLabel = (List<String>) client.get("place:entity:"+place.getLang()+":"+StringUtils.replace(place.getLabel()," ",""));
                        if(agentLangLabel==null){
                            agentLangLabel = new ArrayList<>();
                        }
                        agentLangLabel.add(place.getCodeUri());
                        client.add("place:entity:"+place.getLang()+":"+StringUtils.replace(place.getLabel()," ",""),1000000,agentLangLabel);
                    }
                    client.add("place:uri:"+place.getCodeUri(),1000000,obj.writeValueAsString(ag));

                    List<String> parents = this.findAgentParents(atl.getParent());
                    if(parents!=null && parents.size()>0) {
                        client.add("place:parent:" + place.getCodeUri(),1000000, parents);
                    }


                    if (atl.getOwlSameAs() != null) {
                        for (String sameAs : atl.getOwlSameAs()) {
                            client.add("place:sameas:"+sameAs,1000000,place.getCodeUri());
                        }
                    }
                } catch (IOException var14) {
                    var14.printStackTrace();
                }
            }
        }

        List<MongoTerm> timespanMongo3 = MongoDatabaseUtils.getAllTimespans();
        System.out.println("Found timespans: " + timespanMongo3.size());
        for (MongoTerm timespan : timespanMongo3) {
            TimespanTermList atl = (TimespanTermList) MongoDatabaseUtils.findByCode(timespan.getCodeUri(), "period");
            if (atl != null) {
                try {
                    EntityWrapper ag = new EntityWrapper();
                    ag.setOriginalField("");
                    ag.setClassName(TimespanImpl.class.getName());
                    ag.setContextualEntity(this.getObjectMapper().writeValueAsString(atl.getRepresentation()));
                    ag.setUrl(timespan.getCodeUri());
                    ag.setOriginalValue(timespan.getOriginalLabel());
                    List<String> agentDefLabel = (List<String>) client.get("timespan:entity:def:"+StringUtils.replace(timespan.getLabel()," ",""));
                    if(agentDefLabel==null){
                        agentDefLabel = new ArrayList<>();
                    }
                    agentDefLabel.add(timespan.getCodeUri());
                    client.add("timespan:entity:def:"+StringUtils.replace(timespan.getLabel()," ",""),1000000,agentDefLabel);
                    List<String> agentCodeUri = (List<String>)client.get("timespan:entity:"+timespan.getCodeUri());
                    if(timespan.getCodeUri()==null){
                        agentCodeUri = new ArrayList<>();
                    }
                    agentCodeUri.add("timespan:entity:def:"+timespan.getLabel());
                    client.add("timespan:entity:"+timespan.getCodeUri(),1000000,agentCodeUri);
                    if(timespan.getLang()!=null){
                        List<String> agentLangLabel = (List<String>) client.get("timespan:entity:"+timespan.getLang()+":"+StringUtils.replace(timespan.getLabel()," ",""));
                        if(agentLangLabel==null){
                            agentLangLabel = new ArrayList<>();
                        }
                        agentLangLabel.add(timespan.getCodeUri());
                        client.add("timespan:entity:"+timespan.getLang()+":"+StringUtils.replace(timespan.getLabel()," ",""),1000000,agentLangLabel);
                    }
                    client.add("timespan:uri:"+timespan.getCodeUri(),1000000,obj.writeValueAsString(ag));

                    List<String> parents = this.findAgentParents(atl.getParent());
                    if(parents!=null && parents.size()>0) {
                        client.add("timespan:parent:" + timespan.getCodeUri(),1000000, parents);
                    }


                    if (atl.getOwlSameAs() != null) {
                        for (String sameAs : atl.getOwlSameAs()) {
                            client.add("timespan:sameas:"+sameAs,1000000,timespan.getCodeUri());
                        }
                    }
                } catch (IOException var14) {
                    var14.printStackTrace();
                }
            }
        }

    }


    /**
     * The internal enrichment functionality not to be exposed yet as there is a
     * strong dependency to the external resources to recreate the DB The
     * enrichment is performed by lowercasing every value so that searchability
     * in the DB is enhanced, but the Capitalized version is always retrieved
     *
     * @param values The values to enrich
     * @return A list of enrichments
     * @throws Exception
     */
    protected List<? extends EntityWrapper> tag(List<InputValue> values)
            throws JsonGenerationException, JsonMappingException, IOException {

        List<EntityWrapper> entities = new ArrayList<EntityWrapper>();
        for (InputValue inputValue : values) {
            for (EntityClass voc : inputValue.getVocabularies()) {
                entities.addAll(findEntities(StringUtils.replace(inputValue.getValue()
                        .toLowerCase()," ",""), inputValue.getOriginalField(), voc, inputValue.getLanguage()));
            }
        }
        return entities;
    }

    private List<? extends EntityWrapper> findEntities(String lowerCase,
                                                       String field, EntityClass className, String lang)
            throws JsonGenerationException, JsonMappingException, IOException {
        List<EntityWrapper> entities = new ArrayList<EntityWrapper>();
        switch (className) {
            case AGENT:
                entities.addAll(findAgentEntities(lowerCase, field, lang));
                break;
            case CONCEPT:
                entities.addAll(findConceptEntities(lowerCase, field, lang));
                break;
            case PLACE:
                entities.addAll(findPlaceEntities(lowerCase, field, lang));
                break;
            case TIMESPAN:
                entities.addAll(findTimespanEntities(lowerCase, field, lang));
            default:
                break;
        }
        return entities;
    }

    private List<EntityWrapper> findConceptEntities(String value,
                                                    String originalField, String lang) throws JsonGenerationException,
            JsonMappingException, IOException {
        Set<EntityWrapper> concepts = new HashSet<>();

        if (StringUtils.isEmpty(lang) || lang.length() != 2) {
            lang = "def";
        }
        if(client.get("concept:entity:"+lang+":"+value)!=null){

           List<String> urisToCheck = (List<String>) client.get("concept:entity:"+lang+":"+value);
            for (String uri : urisToCheck) {
                EntityWrapper entity = obj.readValue((String)client.get("concept:uri:"+uri),EntityWrapper.class);
                entity.setOriginalField(originalField);
                concepts.add(entity);
                if(client.get("concept:parent:"+uri)!=null){
                    List<String> parents = (List<String>) client.get("concept:parent:"+uri);
                    for(String parent:parents){
                        EntityWrapper parentEntity = obj.readValue((String)client.get("concept:uri:"+parent),EntityWrapper.class);
                        concepts.add(parentEntity);
                    }
                }

            }
        }
        List<EntityWrapper> list = new ArrayList<>();
        list.addAll(concepts);
        return list;
    }

    private List<String> findConceptParents(String parent) throws JsonGenerationException, JsonMappingException, IOException {
        ArrayList parentEntities = new ArrayList();
        MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "concept");
        if (parents != null) {
            parentEntities.add(parents.getCodeUri());
            if (parents.getParent() != null && !parent.equals(parents.getParent())) {
                parentEntities.addAll(this.findConceptParents(parents.getParent()));
            }
        }

        return parentEntities;
    }

    private List<? extends EntityWrapper> findAgentEntities(String value,
                                                            String originalField, String lang) throws JsonGenerationException,
            JsonMappingException, IOException {
        Set agents = new HashSet<>();
        if (StringUtils.isEmpty(lang) || lang.length() != 2) {
            lang = "def";
        }
        if(client.get("agent:entity:"+lang+":"+value)!=null){
            List<String> urisToCheck =  (List<String>)client.get("agent:entity:"+lang+":"+value);
            for (String uri : urisToCheck) {
                EntityWrapper entity = obj.readValue((String)client.get("agent:uri:"+uri),EntityWrapper.class);
                entity.setOriginalField(originalField);
                agents.add(entity);
                if(client.get("agent:parent:"+uri)!=null){
                    List<String> parents = (List<String>) client.get("agent:parent:"+uri);
                    for(String parent:parents){
                        EntityWrapper parentEntity = obj.readValue((String)client.get("agent:uri:"+parent),EntityWrapper.class);
                        agents.add(parentEntity);
                    }
                }

            }
        }

        List list = new ArrayList<>();
        list.addAll(agents);
        return list;
    }

    private List<String> findAgentParents(String parent) throws JsonGenerationException, JsonMappingException, IOException {
        ArrayList parentEntities = new ArrayList();
        MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "people");
        if (parents != null) {
            parentEntities.add(parents.getCodeUri());
            if (parents.getParent() != null && !parent.equals(parents.getParent())) {
                parentEntities.addAll(this.findAgentParents(parents.getParent()));
            }
        }

        return parentEntities;
    }

    private List<? extends EntityWrapper> findPlaceEntities(String value,
                                                            String originalField, String lang) throws JsonGenerationException,
            JsonMappingException, IOException {
        Set places = new HashSet<>();
        if (StringUtils.isEmpty(lang) || lang.length() != 2) {
            lang = "def";
        }
        if(client.get("place:entity:"+lang+":"+value)!=null){
            List<String> urisToCheck = (List<String>) client.get("place:entity:"+lang+":"+value);
            for (String uri : urisToCheck) {
                EntityWrapper entity = obj.readValue((String)client.get("place:uri:"+uri),EntityWrapper.class);
                entity.setOriginalField(originalField);
                places.add(entity);
                if(client.get("place:parent:"+uri)!=null){
                    List<String> parents = (List<String>) client.get("place:parent:"+uri);
                    for(String parent:parents){
                        EntityWrapper parentEntity = obj.readValue((String)client.get("place:uri:"+parent),EntityWrapper.class);
                        places.add(parentEntity);
                    }
                }

            }
        }


        List list = new ArrayList<>();
        list.addAll(places);
        return list;
    }

    private List<String> findPlaceParents(String parent) throws JsonGenerationException, JsonMappingException, IOException {
        ArrayList parentEntities = new ArrayList();
        MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "place");
        if (parents != null) {
            parentEntities.add(parents.getCodeUri());
            if (parents.getParent() != null && !parent.equals(parents.getParent())) {
                parentEntities.addAll(this.findPlaceParents(parents.getParent()));
            }
        }

        return parentEntities;
    }


    private List<? extends EntityWrapper> findTimespanEntities(String value,
                                                               String originalField, String lang) throws JsonGenerationException,
            JsonMappingException, IOException {
        Set timespans = new HashSet<>();
        if (StringUtils.isEmpty(lang) || lang.length() != 2) {
            lang = "def";
        }
        if(client.get("timespan:entity:"+lang+":"+value)!=null){
            List<String> urisToCheck = (List<String>) client.get("timespan:entity:"+lang+":"+value);
            for (String uri : urisToCheck) {
                EntityWrapper entity = obj.readValue((String)client.get("timespan:uri:"+uri),EntityWrapper.class);
                entity.setOriginalField(originalField);
                timespans.add(entity);
                if(client.get("timespan:parent:"+uri)!=null){
                    List<String> parents =  (List<String>)client.get("timespan:parent:"+uri);
                    for(String parent:parents){
                        EntityWrapper parentEntity = obj.readValue((String)client.get("timespan:uri:"+parent),EntityWrapper.class);
                        timespans.add(parentEntity);
                    }
                }

            }
        }

        List list = new ArrayList<>();
        list.addAll(timespans);
        return list;
    }

    private List<String> findTimespanParents(String parent) throws JsonGenerationException, JsonMappingException, IOException {
        ArrayList parentEntities = new ArrayList();
        MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "period");
        if (parents != null) {
            parentEntities.add(parents.getCodeUri());
            if (parents.getParent() != null && !parent.equals(parents.getParent())) {
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException var5) {
                    Logger.getLogger(MemcachedInternalEnricher.class.getName()).log(Level.SEVERE, (String) null, var5);
                }

                parentEntities.addAll(this.findTimespanParents(parents.getParent()));
            }
        }

        return parentEntities;
    }

    public EntityWrapper getByUri(String uri) throws IOException {
        if(client.get("agent:uri:"+uri)!=null){
            return obj.readValue((String)client.get("agent:uri:"+uri),EntityWrapper.class);
        }
        if(client.get("concept:uri:"+uri)!=null){
            return obj.readValue((String)client.get("concept:uri:"+uri),EntityWrapper.class);
        }
        if(client.get("timespan:uri:"+uri)!=null){
            return obj.readValue((String)client.get("timespan:uri:"+uri),EntityWrapper.class);
        }
        if(client.get("place:uri:"+uri)!=null){
            return obj.readValue((String)client.get("place:uri:"+uri),EntityWrapper.class);
        }

        if(client.get("agent:sameas:"+uri)!=null){
            return obj.readValue((String)client.get("agent:uri:"+(String)client.get("agent:sameas:"+uri)),EntityWrapper.class);
        }
        if(client.get("concept:sameas:"+uri)!=null){
            return obj.readValue((String)client.get("concept:uri:"+(String)client.get("concept:sameas:"+uri)),EntityWrapper.class);
        }
        if(client.get("timespan:sameas:"+uri)!=null){
            return obj.readValue((String)client.get("timespan:uri:"+(String)client.get("timespan:sameas:"+uri)),EntityWrapper.class);
        }
        if(client.get("place:sameas:"+uri)!=null){
            return obj.readValue((String)client.get("place:uri:"+(String)client.get("place:sameas:"+uri)),EntityWrapper.class);
        }

        return null;
    }

    private ObjectMapper getObjectMapper() {
        return obj;
    }


}
