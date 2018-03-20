package eu.europeana.hierarchies.generation;

import eu.europeana.hierarchy.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * REST API exposing the generation of nodes functionality within Neo4j
 * Created by ymamakis on 1/21/16.
 */
@Path("/nodes")
public class CreateNodeResource {

    private GraphDatabaseService service;
    private ObjectMapper mapper;
    public CreateNodeResource(@Context GraphDatabaseService service){
        this.service = service;
        mapper = new ObjectMapper();
    }

    /**
     * Create a node for an EDM entity
     * @param recordValues The values to persist
     * @return A serialized ParentNode
     * @throws IOException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/node/generate")
    public Response createNode(@FormParam("recordValues") String recordValues) throws IOException{

        String parent = createNodeFromInputNode(mapper.readValue(recordValues,InputNode.class));
        if(parent!=null){
            ParentNode pNode = new ParentNode();
            pNode.setId(parent);
            return Response.ok().entity(pNode).build();
        }
        return Response.ok().entity(new ParentNode()).build();
    }

    /**
     * Create a node for an EDM entity
     * @param recordValues The values to persist
     * @return A serialized ParentNode
     * @throws IOException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes/generate")
    public Response createNodes(@FormParam("recordValues") String recordValues) throws IOException{
        InputNodeList inputList = mapper.readValue(recordValues,InputNodeList.class);
        ParentNodeList pNodes = new ParentNodeList();
        Set<ParentNode> pNodeList = new HashSet<ParentNode>();
        for(InputNode inputNode:inputList.getInputNodeList()) {
            String parent = createNodeFromInputNode(inputNode);
            if (parent != null) {
                ParentNode pNode = new ParentNode();
                pNode.setId(parent);
                pNodeList.add(pNode);
            }
        }
        pNodes.setParentNodeList(pNodeList);
        return Response.ok().entity(pNodes).build();
    }


    private String createNodeFromInputNode(InputNode inputNode){
        Transaction tx = service.beginTx();
        Node node = service.createNode();
        for(StringValue val:inputNode.getStringValues()){
            node.setProperty(val.getKey(),val.getValue());
        }
        for(ListValue val:inputNode.getListValues()){
            node.setProperty(val.getKey(),val.getValue());
        }
        if(service.index().existsForNodes("edmsearch2")){
            service.index().forNodes("edmsearch2").add(node,"rdf_about",node.getProperty("rdf:about"));
        }
        tx.success();
        return node.getProperty("dcterms:isPartOf")!=null?node.getProperty("dcterms:isPartOf").toString():null;
    }


}
