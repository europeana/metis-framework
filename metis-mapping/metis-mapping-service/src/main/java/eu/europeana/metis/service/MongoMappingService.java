package eu.europeana.metis.service;

import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.ConditionMapping;
import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.model.Mappings;
import eu.europeana.metis.mapping.model.SimpleMapping;
import eu.europeana.metis.mapping.persistence.AttributeDao;
import eu.europeana.metis.mapping.persistence.ElementDao;
import eu.europeana.metis.mapping.persistence.MappingSchemaDao;
import eu.europeana.metis.mapping.persistence.MappingsDao;
import eu.europeana.metis.mapping.persistence.MongoMappingDao;
import eu.europeana.metis.mapping.persistence.StatisticsDao;
import eu.europeana.metis.mapping.statistics.Statistics;
import eu.europeana.metis.utils.MongoUpdateUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service that wraps the Mongomapping DAO
 * Created by ymamakis on 6/13/16.
 */
@Service
public class MongoMappingService implements MappingService {
    @Autowired
    private MongoMappingDao dao;

    @Autowired
    private MappingsDao mappingsDao;

    @Autowired
    private MappingSchemaDao mappingsSchemaDao;

    @Autowired
    private ElementDao elementDao;

    @Autowired
    private AttributeDao attributeDao;

    @Autowired
    private StatisticsDao dsDao;
    /**
     * Save a mapping
     *
     * @param mapping the mapping to save
     * @return The id of the mapping
     */
    @Override
    public String saveMapping(Mapping mapping) {
        persistElementsAndAttributes(mapping.getMappings());
        mappingsDao.save(mapping.getMappings());
        mappingsSchemaDao.save(mapping.getTargetSchema());
        return dao.save(mapping).getId().toString();
    }

    /**
     * Update a mapping
     *
     * @param mapping The mapping to update
     * @return The id of the mapping
     */
    @Override
    public String updateMapping(Mapping mapping) {
        UpdateOperations<Mapping> ops = dao.createUpdateOperations();
        Query<Mapping> query = dao.createQuery();
        MongoUpdateUtils.update(ops, "dataset", mapping.getDataset());
        MongoUpdateUtils.update(ops, "name", mapping.getName());
        MongoUpdateUtils.update(ops, "organization", mapping.getOrganization());
        MongoUpdateUtils.update(ops, "lastModified", mapping.getLastModified());
        MongoUpdateUtils.update(ops, "mappings", mapping.getMappings());
        MongoUpdateUtils.update(ops, "parameters", mapping.getParameters());
        MongoUpdateUtils.update(ops, "targetSchema", mapping.getTargetSchema());
        MongoUpdateUtils.update(ops, "xsl", mapping.getXsl());
        persistElementsAndAttributes(mapping.getMappings());
        //TODO: update?
        //mappingsDao.(mapping.getMappings());
        //mappingsSchemaDao.save(mapping.getTargetSchema());
        dao.update(query.filter("_id", mapping.getObjId()), ops);
        return mapping.getObjId().toString();
    }

    /**
     * Delete a mapping by id
     *
     * @param id The id to delete
     */
    @Override
    public void deleteMapping(String id) {
        dao.deleteById(new ObjectId(id));
    }

    /**
     * Get a mapping by id
     *
     * @param id The id to search for
     * @return The Mapping
     */
    @Override
    public Mapping getByid(String id) {
        return dao.get(new ObjectId(id));
    }

    /**
     * Get a mapping by name
     *
     * @param name The name of the mapping to search for
     * @return The Mapping
     */
    @Override
    public Mapping getByName(String name) {
        return dao.findOne(dao.createQuery().filter("name", name));
    }

    /**
     * Get all the mappings for an organization
     *
     * @param organization The organization id to search for
     * @return A list of mappings
     */
    @Override
    public List<Mapping> getMappingByOrganization(String organization) {
        return dao.find(dao.createQuery().filter("organization", organization)).asList();
    }

    /**
     * Clear the validation statistics for a mapping
     *
     * @param name The name of the mapping
     */
    @Override
    public Mapping clearValidationStatistics(String name) {
        Mapping mapping = getByName(name);
        if (mapping.getMappings() != null) {
            Mappings mappings = mapping.getMappings();
            if (mappings.getAttributes() != null && mappings.getAttributes().size() > 0) {
                List<Attribute> attributes = new ArrayList<>();
                for (Attribute attr : mappings.getAttributes()) {
                    attributes.add(clearFieldStatistics(attr));
                }
                mappings.setAttributes(attributes);
            }
            if (mappings.getElements() != null && mappings.getElements().size() > 0) {
                List<Element> elements = new ArrayList<>();
                for (Element elem : mappings.getElements()) {
                    elements.add(clearFieldStatistics(elem));
                }
                mappings.setElements(elements);
            }
            mapping.setMappings(mappings);
        }
        return mapping;
    }

    @Override
    public <T extends Attribute> Statistics getStatisticsForField(T field, String dataset){
        Query<Statistics> q= dsDao.getDatastore().createQuery(Statistics.class);
        if(field.getMappings()!=null){
            SimpleMapping mapping = field.getMappings().get(0);

            q.filter("datasetId",dataset).filter("xpath",mapping.getSourceField());
            return dsDao.findOne(q);
        }
        if(field.getConditionalMappings()!=null){
            ConditionMapping mapping = field.getConditionalMappings().get(0);
            q.filter("datasetId",dataset).filter("xpath",mapping.getSourceField());
            return dsDao.findOne(q);
        }
        return null;
    }

    private <T extends Attribute> T clearFieldStatistics(T field) {
        field.setFlags(null);
        if (field instanceof Element) {
            List<Attribute> attrs = ((Element) field).getAttributes();
            List<Element> elems = ((Element) field).getElements();
            if (attrs != null && attrs.size() > 0) {
                List<Attribute> newAttr = new ArrayList<>();
                for (Attribute attr : attrs) {
                    newAttr.add(clearFieldStatistics(attr));
                }
                ((Element) field).setAttributes(newAttr);
            }
            if (elems != null && elems.size() > 0) {
                List<Element> newElements = new ArrayList<>();
                for (Element elem : elems) {
                    newElements.add(clearFieldStatistics(elem));
                }
                ((Element) field).setElements(newElements);
            }

        }
        return field;
    }

    /**
     * Get a list of names of all the mappings for an organization
     *
     * @param organization The organization to search for
     * @return The list of names of all the mappings for this organization
     */
    @Override
    public List<String> getMappingNamesByOrganization(String organization) {
        return convertMappingsToStrings(getMappingByOrganization(organization));

    }

    /**
     * Get all the templates
     *
     * @return The names of all the templates
     */
    @Override
    public List<String> getTemplates() {
        return convertMappingsToStrings(dao.find(dao.createQuery().field("name").startsWith("template_")).asList());
    }

    private List<String> convertMappingsToStrings(List<Mapping> mappings) {
        if (mappings != null && mappings.size() > 0) {
            List<String> names = new ArrayList<String>();
            for (Mapping mapping : mappings) {
                names.add(mapping.getName());
            }
            return names;
        }
        return null;
    }

    @Override
    public String setSchematronRulesForMapping(String mappingId, Set<String> schematronRules) {
        Mapping mapping = getByid(mappingId);
        mapping.setSchematronRules(schematronRules);
        return updateMapping(mapping);
    }

    @Override
    public String setNamespacesForMapping(String mappingId, Map<String, String> namespaces) {
        Mapping mapping = getByid(mappingId);
        mapping.getMappings().setNamespaces(namespaces);
        return updateMapping(mapping);
    }

    @Override
    public void uploadXslForMapping(String mappingId, String xsl){
        Mapping mapping = getByid(mappingId);
        mapping.setXsl(xsl);
        updateMapping(mapping);
    }


    private void persistElementsAndAttributes(Mappings mappings){
        if(mappings.getAttributes()!=null){
            for(Attribute attr: mappings.getAttributes()){
                attributeDao.save(attr);
            }
        }
        if(mappings.getElements()!=null){
            for(Element elem:mappings.getElements()){
                persistElementsAndAttributes(elem);
                elementDao.save(elem);
            }
        }
    }

    private void persistElementsAndAttributes(Element element){
        if(element.getAttributes()!=null){
            for(Attribute attr: element.getAttributes()){
                attributeDao.save(attr);
            }
        }
        if(element.getElements()!=null){
            for(Element elem:element.getElements()){
                persistElementsAndAttributes(elem);
                elementDao.save(elem);
            }
        }
    }
}

