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
package eu.europeana.hierarchies.service.utils;

import eu.europeana.hierarchy.InputNode;
import eu.europeana.hierarchy.ListValue;
import eu.europeana.hierarchy.StringValue;

import java.util.*;

/**
 * Util class to convert a Map of parameters to a node
 * Created by ymamakis on 1/22/16.
 */
public class InputNodeCreator {

    /**
     * Convert a map of values to a Neo4j request node
     * @param map The map to convert
     * @return The resulting node
     */
    public static InputNode createInputNodeFromMap(Map<String,Object> map){
        InputNode inputNode = new InputNode();
        Set<StringValue> stringValues = new HashSet<StringValue>();
        Set<ListValue> listValues = new HashSet<ListValue>();
        for(Map.Entry<String,Object> entry:map.entrySet()){
            if (entry.getValue().getClass().isAssignableFrom(String.class)){
                StringValue val = new StringValue();
                val.setKey(entry.getKey());
                val.setValue((String)entry.getValue());
                stringValues.add(val);
            } else if(entry.getValue().getClass().isAssignableFrom(ArrayList.class)){
                ListValue val = new ListValue();
                val.setKey(entry.getKey());
                val.setValue((List<String>)entry.getValue());
                listValues.add(val);
            }
        }
        inputNode.setListValues(listValues.size()>0?listValues:null);
        inputNode.setStringValues(stringValues.size()>0?stringValues:null);
        return (inputNode.getStringValues()==null&&inputNode.getListValues()==null)?null:inputNode;
    }

}
