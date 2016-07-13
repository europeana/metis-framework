package eu.europeana.hierarchies.generation;

import eu.europeana.hierarchies.generation.utils.CypherTemplate;
import eu.europeana.hierarchy.relations.RelType;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * REST API for the generation of relations of nodes
 * Created by ymamakis on 1/22/16.
 */
@Path("/relations")
public class CreateRelationsResource {

    private GraphDatabaseService service;
    private Index<Node> nodeIndex;
    public CreateRelationsResource(@Context GraphDatabaseService service) {
        this.service = service;
        nodeIndex = service.index().forNodes("edmsearch2");
    }

    /**
     * Create standard EDM relations between nodes (edm:isNextInSequence, dcterms:isPartOf, dcterms:hasPart)
     * @param id The id for which to create the relations
     * @return OK
     */
    @POST
    @Path("/generate/{id}")
    public Response createRelationsForNode(@PathParam("id") String id) {
        Transaction tx = service.beginTx();
        IndexHits<Node> nodes = nodeIndex.get("rdf_about", id);
        if (nodes.size() > 0) {
            Node node = nodes.getSingle();
            if (node.getProperty(RelType.DCTERMS_ISPARTOF.getRelType()) != null) {
                String parentId = node.getProperty(RelType.DCTERMS_ISPARTOF.getRelType()).toString();
                IndexHits<Node> parentNodes = nodeIndex.get("rdf_about", parentId);
                if (parentNodes.size() > 0) {
                    node.setProperty("hasParent",parentId);
                    Node parentNode = parentNodes.getSingle();
                    parentNode.setProperty("hasChildren",true);
                    service.execute(String.format(CypherTemplate.LINKS,id,parentId,RelType.DCTERMS_ISPARTOF.getRelType()));
                    service.execute(String.format(CypherTemplate.LINKS,parentId,id,RelType.DCTERMS_HASPART.getRelType()));
                }
            }
            if(node.getProperty(RelType.EDM_ISNEXTINSEQUENCE.getRelType()) != null){
                String previousId = node.getProperty(RelType.EDM_ISNEXTINSEQUENCE.getRelType()).toString();
                IndexHits<Node> previousNodes = nodeIndex.get("rdf_about", previousId);
                if (previousNodes.size() > 0) {
                    service.execute(String.format(CypherTemplate.LINKS,id,previousId,RelType.EDM_ISNEXTINSEQUENCE.getRelType()));
                }
            }
        }
        tx.success();
        return Response.ok().build();
    }

    /**
     * Create the isFirstInSequence and isLastInSequence relations
     * @param id The parent id
     * @return OK
     */
    @POST
    @Path("/generate/order/{id}")
    public Response createOrdered(@PathParam("id") String id){
        Transaction tx = service.beginTx();
        service.execute(String.format(CypherTemplate.FIRSTINSEQUENCE,id));
        service.execute(String.format(CypherTemplate.LASTINSEQUENCE,id));
        tx.success();
        return Response.ok().build();
    }


    /**
     * Delete relations and nodes
     * @param id The id to remove
     * @return OK
     */
    @POST
    @Path("/delete/{id}")
    public Response deleteId(@PathParam("id") String id){
        Transaction tx = service.beginTx();
        IndexHits<Node> nodes = nodeIndex.get("rdf_about",id);
        if(nodes.size()>0){
            Node node = nodes.getSingle();
            Iterable<Relationship> rels = node.getRelationships();
            if(rels!=null){
                while (rels.iterator().hasNext()){
                    Relationship rel = rels.iterator().next();
                    rel.delete();
                }
            }
            nodeIndex.remove(node);
            node.delete();

        }
        tx.success();
        return Response.ok().build();
    }
}
