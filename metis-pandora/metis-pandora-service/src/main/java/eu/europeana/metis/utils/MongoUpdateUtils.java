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
