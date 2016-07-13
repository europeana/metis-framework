package eu.europeana.hierarchy.relations;

import org.neo4j.graphdb.RelationshipType;

/**
 * Created by ymamakis on 1/22/16.
 */
public class Relation implements RelationshipType {

    private String name;

    /**
     * Default constructor for a Relation
     * @param name The name of the neo4j relation
     */
    public Relation(String name){
        this.name = name;
    }

    public String name() {
        return this.name;
    }

}