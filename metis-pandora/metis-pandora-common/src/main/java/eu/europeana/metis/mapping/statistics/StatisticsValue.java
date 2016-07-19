package eu.europeana.metis.mapping.statistics;

import eu.europeana.metis.mapping.common.Value;
import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A single statistical value
 * @see Value
 * Created by ymamakis on 6/15/16.
 */
@XmlRootElement
@Entity
public class StatisticsValue extends Value {
    private long occurence;

    /**
     * The occurrence of the Value
     * @return The occurrence of the Value
     */
    @XmlElement
    public long getOccurence() {
        return occurence;
    }

    /**
     * Set the occurrence of the Value
     * @param occurence Set the occurrence of the Value
     */
    public void setOccurence(long occurence) {
        this.occurence = occurence;
    }
}
