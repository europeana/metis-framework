package eu.europeana.hierarchies.service.cache;

import java.io.IOException;
import java.util.Set;

/**
 * Interface for a cache DAO
 * Created by ymamakis on 1/25/16.
 */
public interface CacheDAO {
    /**
     * Add a parent to a collection
     * @param collection The collection entry in cache
     * @param parent The parent String
     */
    void addParentToSet(String collection, String parent) throws IOException;

    /**
     * Add a set of parents to a collection
     * @param collection The collection entry in cache
     * @param parents The set of parents to append
     */
    void addParentsToSet(String collection, Set<String> parents) throws IOException;
    /**
     * Retrieve the parents of a collection
     * @param collection The collection to search for in cache
     * @return The Cache Entry for this collection
     */
    CacheEntry getByCollection(String collection) throws IOException;
}
