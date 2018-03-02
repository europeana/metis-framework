package eu.europeana.metis.mapping.model;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * A conditional mapping for the given field. A conditional mapping specifies logical and
 * relational operations between the given field and an field from the original data that
 * is mapped to the field.
 *
 * Created by gmamakis on 8-4-16.
 */
@XmlRootElement
@Entity
public class ConditionMapping extends SimpleMapping {


    @Embedded
    private List<Clause> clauses;
    private String conditionalLogicalOperator;

    /**
     * The list of clauses for the conditional mapping
     * @see Clause
     * @return The list of clauses for the conditional mapping
     */
    @XmlElement(name = "clauses")
    public List<Clause> getClauses() {
        return clauses;
    }

    /**
     * The list of clauses for the conditional mapping
     * @param clauses The list of clauses for the conditional mapping
     */
    public void setClauses(List<Clause> clauses) {
        this.clauses = clauses;
    }

    /**
     * The logical operator connecting the list of clauses (e.g. AND, OR)
     * @return The logical operator connecting the clauses
     */
    @XmlElement(name = "logicalop",defaultValue = "AND")
    public String getConditionalLogicalOperator() {
        return conditionalLogicalOperator;
    }

    /**
     * The logical oerator connecting the clausees
     * @param conditionalLogicalOperator
     */
    public void setConditionalLogicalOperator(String conditionalLogicalOperator) {
        this.conditionalLogicalOperator = conditionalLogicalOperator;
    }


}
