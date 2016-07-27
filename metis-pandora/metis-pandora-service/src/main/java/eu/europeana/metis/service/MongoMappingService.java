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

import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.model.Mappings;
import eu.europeana.metis.mapping.persistence.MongoMappingDao;
import eu.europeana.metis.utils.MongoUpdateUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A service that wraps the Mongomapping DAO
 * Created by ymamakis on 6/13/16.
 */
@Service
public class MongoMappingService {
    @Autowired
    private MongoMappingDao dao;

    /**
     * Save a mapping
     * @param mapping the mapping to save
     * @return The id of the mapping
     */
    public String saveMapping(Mapping mapping){
        return dao.save(mapping).getId().toString();
    }

    /**
     * Update a mapping
     * @param mapping The mapping to update
     * @return The id of the mapping
     */
    public String updateMapping(Mapping mapping){
        UpdateOperations<Mapping> ops = dao.createUpdateOperations();
        Query<Mapping> query = dao.createQuery();
        MongoUpdateUtils.update(ops,"dataset",mapping.getDataset());
        MongoUpdateUtils.update(ops,"name",mapping.getName());
        MongoUpdateUtils.update(ops,"organization",mapping.getOrganization());
        MongoUpdateUtils.update(ops,"lastModified",mapping.getLastModified());
        MongoUpdateUtils.update(ops,"mappings",mapping.getMappings());
        MongoUpdateUtils.update(ops,"parameters",mapping.getParameters());
        MongoUpdateUtils.update(ops,"targetSchema",mapping.getTargetSchema());
        MongoUpdateUtils.update(ops,"xsl",mapping.getXsl());
        dao.update(query.filter("_id",mapping.getObjId()),ops);
        return mapping.getObjId().toString();
    }

    /**
     * Delete a mapping by id
     * @param id  The id to delete
     */
    public void deleteMapping(String id){
        dao.deleteById(new ObjectId(id));
    }

    /**
     * Get a mapping by id
     * @param id The id to search for
     * @return The Mapping
     */
    public Mapping getByid(String id){
        return dao.get(new ObjectId(id));
    }

    /**
     * Get a mapping by name
     * @param name The name of the mapping to search for
     * @return The Mapping
     */
    public Mapping getByName(String name){
        return dao.findOne(dao.createQuery().filter("name",name));
    }

    /**
     * Get all the mappings for an organization
     * @param organization The organization id to search for
     * @return A list of mappings
     */
    public List<Mapping> getMappingByOrganization(String organization){
        return dao.find(dao.createQuery().filter("organization",organization)).asList();
    }

    /**
     * Clear the validation statistics for a mapping
     * @param name The name of the mapping
     */
    public Mapping clearValidationStatistics(String name){
        Mapping mapping = getByName(name);
        if (mapping.getMappings()!=null){
            Mappings mappings = mapping.getMappings();
            if(mappings.getAttributes()!=null && mappings.getAttributes().size()>0) {
                List<Attribute> attributes = new ArrayList<>();
                for(Attribute attr:mappings.getAttributes()) {
                   attributes.add(clearFieldStatistics(attr));
                }
                mappings.setAttributes(attributes);
            }
            if(mappings.getElements()!=null && mappings.getElements().size()>0){
                List<Element> elements = new ArrayList<>();
                for(Element elem:mappings.getElements()) {
                    elements.add(clearFieldStatistics(elem));
                }
                mappings.setElements(elements);
            }
            mapping.setMappings(mappings);
        }
        return mapping;
    }

    private <T extends Attribute> T clearFieldStatistics(T field) {
        field.setFlags(null);
        field.setStatistics(null);
        if(field instanceof  Element){
            List<Attribute> attrs = ((Element)field).getAttributes();
            List<Element> elems= ((Element)field).getElements();
            if(attrs!=null && attrs.size()>0){
                List<Attribute> newAttr = new ArrayList<>();
                for(Attribute attr:attrs) {
                    newAttr.add(clearFieldStatistics(attr));
                }
                ((Element) field).setAttributes(newAttr);
            }
            if(elems!=null && elems.size()>0){
                List<Element> newElements = new ArrayList<>();
                for(Element elem:elems) {
                    newElements.add(clearFieldStatistics(elem));
                }
                ((Element) field).setElements(newElements);
            }

        }
        return field;
    }

    /**
     * Get a list of names of all the mappings for an organization
     * @param organization The organization to search for
     * @return The list of names of all the mappings for this organization
     */
    public List<String> getMappingNamesByOrganization(String organization){
        return convertMappingsToStrings(getMappingByOrganization(organization));

    }

    /**
     * Get all the templates
     * @return The names of all the templates
     */
    public List<String> getTemplates(){
        return convertMappingsToStrings(dao.find(dao.createQuery().field("name").startsWith("template_")).asList());
    }

    private List<String> convertMappingsToStrings(List<Mapping> mappings){
        if(mappings!=null && mappings.size()>0){
            List<String> names = new ArrayList<String>();
            for(Mapping mapping:mappings){
                names.add(mapping.getName());
            }
            return names;
        }
        return null;
    }

    public String setSchematronRulesForMapping(Mapping mapping, Set<String> schematronRules){
        mapping.setSchematronRules(schematronRules);
        return updateMapping(mapping);
    }

    public String setNamespacesForMapping(Mapping mapping, Map<String,String> namespaces){
        mapping.getMappings().setNamespaces(namespaces);
        return updateMapping(mapping);
    }

}

