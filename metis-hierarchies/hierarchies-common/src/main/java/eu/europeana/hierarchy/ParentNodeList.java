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
package eu.europeana.hierarchy;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * Wrapper for batch operation responses
 * Created by ymamakis on 1/22/16.
 */
@XmlRootElement
public class ParentNodeList {
    @XmlElement
    private Set<ParentNode> parentNodeList;

    public Set<ParentNode> getParentNodeList() {
        return parentNodeList;
    }

    public void setParentNodeList(Set<ParentNode> parentNodeList) {
        this.parentNodeList = parentNodeList;
    }
}
