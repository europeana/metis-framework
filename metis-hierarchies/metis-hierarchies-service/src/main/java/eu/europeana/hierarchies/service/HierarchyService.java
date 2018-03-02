package eu.europeana.hierarchies.service;

import eu.europeana.hierarchies.service.cache.CacheEntry;
import eu.europeana.hierarchy.ParentNode;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hierarchy Service Interface
 * Created by ymamakis on 1/22/16.
 */
public interface HierarchyService {

    /**
     * Create a hierarchical node for a single record. The value of the object is either a String or a List of strings
     * @param record The record to create
     * @return The parent of the node if it exists or empty other wise
     */
    eu.europeana.hierarchy.ParentNode createNode(String collection,Map<String,Object> record) throws IOException;

    /**
     * Create the relationships for a given id
     * @param id The id to search for and create the relationships
     */
    void createRelationsForNode(String id);

    /**
     * Batch creation of hierarchical nodes
     * @param records The records to create
     * @return A set of parent nodes
     */
    Set<ParentNode> createNodes(String collection,List<Map<String,Object>> records) throws IOException;

    /**
     * Batch creation of relationships for nodes
     * @param ids The ids for which to create the relationships
     */
    void createRelationsForNodes(List<String> ids);

    /**
     * Create ordered and unordered relations for node
     * @param id The parent id for which to create ordered and unordered relations
     */
    void createOrderedAndUnorderedRelationsForNode(String id);

    /**
     * Batch operation for the creation of unordered relations for a node
     * @param ids The parent ids for which to create ordered and unordered relations
     */
    void createOrderedAndUnorderedRelationsForNodes(List<String> ids);

    /**
     * Delete a node and all its relations
     * @param id The node to delete
     */
    void deleteNode(String id);

    /**
     * Delete a list of nodes and all their relations
     * @param ids The nodes to delete
     */
    void deleteNodes(List<String> ids);

    /**
     * Delete a full collection by its id. The operation is unsafe for collections that have common mnemonics as HISPANA
     * @param collection The collection to delete
     */
    void deleteByCollection(String collection);

    /**
     * Retrieve the parents identified for a collection
     * @param collection The collection to retrieve the parents for
     * @return The list of parents from cache
     */
    CacheEntry retrieveParentsByCollection(String collection) throws IOException;
}
