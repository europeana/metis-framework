package eu.europeana.metis.mapping.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Clauses for conditional mapping
 * Created by gmamakis on 8-4-16.
 */
@XmlRootElement
@Entity
public class Clause {

    @Id
    private ObjectId id;
    private String xPathMapping;
    private String valueMapping;
    private String conditionRelationOperator;

    /**
     * The mapping between the given field and the XPath from the original data
     *
     * @return The xPath of the field from the original data that has been mapped to
     * the field for which this clause is applied
     */
    @XmlElement(name = "xpath")
    public String getxPathMapping() {
        return xPathMapping;
    }

    /**
     * Set the xPath of the original field
     *
     * @param xPathMapping The XPath of the original field mapped to the given field
     */
    public void setxPathMapping(String xPathMapping) {
        this.xPathMapping = xPathMapping;
    }

    /**
     * The value mapping for the given clause. Value mapping is a user-provided
     * value for validating the field for the given clause
     *
     * @return The value mapping
     */
    @XmlElement(name = "value")
    public String getValueMapping() {
        return valueMapping;
    }

    /**
     * Set the value mapping for this clause
     *
     * @param valueMapping The value mapping for this clause
     */
    public void setValueMapping(String valueMapping) {
        this.valueMapping = valueMapping;
    }

    /**
     * The relational operator between the field the clause applies to,
     * the xPath of the original field and the provided value mappings.
     * Available operators are = and !=
     *
     * @return The relational operator
     */
    @XmlElement(name = "relationalop", defaultValue = "=")
    public String getConditionRelationOperator() {
        return conditionRelationOperator;
    }

    /**
     * Set the conditional relational operator
     * @param conditionRelationOperator The conditional relational operator
     */
    public void setConditionRelationOperator(String conditionRelationOperator) {
        this.conditionRelationOperator = conditionRelationOperator;
    }

    /**
     * The id for the clause
     * @return The id for the clause
     */
    @XmlElement
    public ObjectId getId() {
        return id;
    }

    /**
     * The id for the clause
     * @param id
     */
    public void setId(ObjectId id) {
        this.id = id;
    }
}
