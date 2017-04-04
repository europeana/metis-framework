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
package eu.europeana.metis.utils;

import eu.europeana.metis.mapping.model.Mapping;
import org.mongodb.morphia.query.UpdateOperations;

/**
 * Update fields in Mongo
 * Created by ymamakis on 6/13/16.
 */
public class MongoUpdateUtils {

    /**
     * Update a field (or embedded class) in Mongop
     * @param ops The update operations
     * @param field The field to update
     * @param value The value to assign
     * @param <T> Anything
     */
    public static <T> void update(UpdateOperations<Mapping> ops, String field, T value){
        if(value == null){
            ops.unset(field);
        } else {
            ops.set(field,value);
        }
    }
}
