package eu.europeana.metis.service;

import eu.europeana.metis.mapping.common.Value;
import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.model.Mappings;
import eu.europeana.metis.mapping.persistence.FlagDao;
import eu.europeana.metis.mapping.persistence.MongoMappingDao;
import eu.europeana.metis.mapping.persistence.StatisticsDao;
import eu.europeana.metis.mapping.statistics.Statistics;
import eu.europeana.metis.mapping.statistics.StatisticsValue;
import eu.europeana.metis.mapping.validation.Flag;
import eu.europeana.metis.mapping.validation.FlagType;
import eu.europeana.metis.mapping.validation.ValidationRule;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The validation/flagging service
 * Created by ymamakis on 6/14/16.
 */
@Service
public class ValidationService {

    @Autowired
    private FlagDao dao;

    @Autowired
    private StatisticsDao dsDao;

    @Autowired
    private MongoMappingDao mappingDao;
    /**
     * Create a flag for a field
     * @param field The field to create the flag for
     * @param type The type of the flag
     * @param value The value for which to create the flag
     * @param message The message that explains the reason of the flag
     * @param mappingId The mapping id
     * @param <T> Element or Attribute
     * @return The newly created flag
     */
    public <T extends Attribute> Flag createFlagForField(T field, FlagType type, String value, String message, String mappingId){
        Flag flag = new Flag();
        flag.setMappingId(mappingId);
        flag.setFlagType(type);
        flag.setMessage(message);
        flag.setxPath(field.getxPathFromRoot() + "/" + field.getPrefix() + ":" + field.getName());
        Value val = new Value();
        val.setValue(value);
        flag.setValue(val);
        dao.save(flag);
        return flag;
    }

    /**
     * Delete a flag for a field
     * @param field The flag to delete the flag for
     * @param mappingId The mapping id
     * @param value The value for which to remove the flag
     * @param <T> Element or Attribute
     */
    public <T extends Attribute> void deleteFlagForField(T field, String mappingId, String value){
        dao.deleteByQuery(dao.createQuery().filter("mappingId",mappingId)
                .filter("xPath",field.getxPathFromRoot() + "/" + field.getPrefix() + ":" + field.getName())
        .filter("value.value",value));
    }
    /**
     * Get a flag for a field
     * @param field The flag to delete the flag for
     * @param mappingId The mapping id
     * @param value The value for which to remove the flag
     * @param <T> Element or Attribute
     */
    public <T extends Attribute> Flag getFlagForField(T field, String mappingId, String value){
       return dao.find(dao.createQuery().filter("mappingId",mappingId)
                .filter("xPath",field.getxPathFromRoot() + "/" + field.getPrefix() + ":" + field.getName())
                .filter("value.value",value)).get();
    }

    /**
     * Validate a field
     * @param mappingId The mapping id
     * @param field The field to validate
     * @param <T> Element or Attribute
     * @return A list of flags for the field
     */
    public <T extends Attribute> List<Flag> validateField(String mappingId, T field) {
        Mapping mapping = mappingDao.findOne("_id",mappingId);
        String dataset = mapping.getDataset();
        String xpath = extractXpathFromField(field);
        Query<Statistics> statQuery = dsDao.getDatastore().createQuery(Statistics.class);
        statQuery.filter("datasetId",dataset).filter("xpath",xpath);
        Statistics statistics = dsDao.find(statQuery).get();

        List<Flag> flags = new ArrayList<>();
        if (field.getRules() != null && field.getRules().size() > 0) {
            for (StatisticsValue oneStat : statistics.getValues()) {
                for (ValidationRule rule : field.getRules()) {
                    Flag flag = checkIfValid(mappingId, field, rule, oneStat.getValue());
                    if (flag != null) {
                        flags.add(flag);
                    }
                }
            }
        }
        return flags;
    }

    private <T extends Attribute> String extractXpathFromField(T field) {
        if(field.getMappings()!=null){
            return field.getMappings().get(0).getSourceField();
        }
        if(field.getConditionalMappings()!=null){
            return field.getConditionalMappings().get(0).getSourceField();
        }
        return null;
    }

    private <T extends Attribute> Flag checkIfValid(String mappingId, T field, ValidationRule rule, String value) {
        List<Flag> flags = retrieveByXpath(mappingId, field.getxPathFromRoot() + "/" + field.getPrefix() + ":" + field.getName());
        if (flags != null && flags.size() > 0) {
            for (Flag flag : flags) {
                if (StringUtils.equals(flag.getValue().getValue(), value)) {
                    return flag;
                }
            }
        }

        if (!rule.getFunction().execute(value)) {
            Flag flag = new Flag();
            flag.setMappingId(mappingId);
            flag.setFlagType(rule.getFlagType());
            flag.setMessage(rule.getMessage());
            flag.setxPath(field.getxPathFromRoot() + "/" + field.getPrefix() + ":" + field.getName());
            Value val = new Value();
            val.setValue(value);
            flag.setValue(val);
            dao.save(flag);
            return flag;
        }
        return null;
    }

    private List<Flag> retrieveByXpath(String mappingId, String xPath) {
        return dao.find(dao.createQuery().filter("mappingId", mappingId).filter("xPath", xPath)).asList();
    }

    /**
     * Validate a mapping
     * @param mapping The mapping to validate
     * @return A populated mapping
     */
    public Mapping validateMapping(Mapping mapping) {
        Mappings mappings = mapping.getMappings();

        if (mappings.getAttributes() != null && mappings.getAttributes().size() > 0) {
            mappings.setAttributes(validateFieldValues(mapping.getName(), mappings.getAttributes()));
        }
        if (mappings.getElements() != null && mappings.getElements().size() > 0) {
            mappings.setElements(validateFieldValues(mapping.getName(), mappings.getElements()));
        }

        mapping.setMappings(mappings);
        return mapping;
    }

    private <T extends Attribute> List<T> validateFieldValues(String name, List<T> fields) {

        List<T> fieldsCopy = new ArrayList<>();
        for (T field : fields) {

                List<Flag> flags = validateField(name, field);
                field.setFlags(flags);
            if (field.getClass().isAssignableFrom(Element.class)) {
                List<Attribute> attrs = ((Element) field).getAttributes();
                List<Element> elems = ((Element) field).getElements();
                if (attrs != null && attrs.size() > 0) {
                    ((Element) field).setAttributes(validateFieldValues(name, ((Element) field).getAttributes()));
                }
                if (elems != null && elems.size() > 0) {
                    ((Element) field).setElements(validateFieldValues(name, ((Element) field).getElements()));
                }
            }

            fieldsCopy.add(field);
        }
        return fieldsCopy;
    }

}
