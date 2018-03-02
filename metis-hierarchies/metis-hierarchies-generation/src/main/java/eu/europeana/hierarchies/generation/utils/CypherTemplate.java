package eu.europeana.hierarchies.generation.utils;

/**
 * Created by ymamakis on 1/22/16.
 */
public class CypherTemplate {

    public final static String FIRSTINSEQUENCE="start n = node:edmsearch2(rdf_about=%s) match (n)-[:`dcterms:hasPart`]->(child) "
            + "WHERE NOT (child)-[:`edm:isNextInSequence`]->() CREATE (child)-[:isFirstInSequence]->(n);";

    public final static String LASTINSEQUENCE="start n = node:edmsearch2(rdf_about=%s) match (n)-[:`dcterms:hasPart`]->(child) "
            + "WHERE NOT (child)<-[:`edm:isNextInSequence`]-() CREATE (child)-[:isLastInSequence]->(n);";

    public final static String LINKS =  "start from = node:edmsearch2(rdf_about=%s), to = node:edmsearch2(rdf_about=%s) " +
            "create unique (from)-[:`%s`]->(to)";
}
