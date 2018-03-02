package generation;

import eu.europeana.hierarchies.generation.CreateNodeResource;
import eu.europeana.hierarchy.InputNode;
import eu.europeana.hierarchy.ParentNode;
import eu.europeana.hierarchy.StringValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.test.server.HTTP;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit test for the create resource
 * Created by ymamakis on 1/25/16.
 */
@Ignore
@RunWith(JUnit4.class)
public class CreateNodeResourceTest {
    ServerControls server;


    @Before
    public void prepare(){
        server = TestServerBuilders.newInProcessBuilder().withExtension("/nodes",CreateNodeResource.class).newServer();
    }

    @Test
    public void testOneNodeCreation() throws IOException{

        InputNode node = new InputNode();
        Set<StringValue> stringValues = new HashSet<>();
        StringValue sv = new StringValue();
        sv.setKey("rdf:about");
        sv.setValue("/test/id");
        stringValues.add(sv);
        node.setStringValues(stringValues);
        HTTP.Response resp = HTTP.POST(server.httpURI().resolve("/nodes").resolve("/node/generate").toString(),null);

        ParentNode pNode = resp.content();

        Assert.assertNotNull(pNode);
    }


}
