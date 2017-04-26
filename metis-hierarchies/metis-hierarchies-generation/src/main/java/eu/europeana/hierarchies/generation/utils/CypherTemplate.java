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
