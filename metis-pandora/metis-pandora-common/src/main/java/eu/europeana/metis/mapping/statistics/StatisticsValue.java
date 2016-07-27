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
