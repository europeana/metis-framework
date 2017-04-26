package eu.europeana.hierarchies.test;

import eu.europeana.hierarchies.service.utils.InputNodeCreator;
import eu.europeana.hierarchy.InputNode;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for creating an InputNode from a map
 * Created by ymamakis on 1/29/16.
 */
public class InputNodeCreatorTest {

    /**
     * Test normal creation
     */
    @Test
    public void createInputNode(){
        Map<String,Object> toAdd = new HashMap<>();
        toAdd.put("string1","string1");
        List<String> strList = new ArrayList<>();
        strList.add("list1");
        toAdd.put("list1",strList);
        InputNode node = InputNodeCreator.createInputNodeFromMap(toAdd);
        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getStringValues());
        Assert.assertNotNull(node.getListValues());
        Assert.assertEquals(1,node.getStringValues().size());
        Assert.assertEquals(1,node.getListValues().size());
        Assert.assertTrue(StringUtils.equals("string1",node.getStringValues().iterator().next().getKey()));
        Assert.assertTrue(StringUtils.equals("string1",node.getStringValues().iterator().next().getValue()));
        Assert.assertTrue(StringUtils.equals("list1",node.getListValues().iterator().next().getKey()));
        Assert.assertEquals(1,node.getListValues().iterator().next().getValue().size());
        Assert.assertTrue(StringUtils.equals("list1",node.getListValues().iterator().next().getValue().get(0)));
    }

    /**
     * Test null node
     */
    @Test
    public void createInputNodeNull(){
        Map<String,Object> toAdd = new HashMap<>();
        InputNode node = InputNodeCreator.createInputNodeFromMap(toAdd);
        Assert.assertNull(node);
    }

    /**
     * Test only StringValue nodes
     */
    @Test
    public void createInputNodeStringValue(){
        Map<String,Object> toAdd = new HashMap<>();
        toAdd.put("string1","string1");
        InputNode node = InputNodeCreator.createInputNodeFromMap(toAdd);
        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getStringValues());
        Assert.assertNull(node.getListValues());
        Assert.assertEquals(1,node.getStringValues().size());
        Assert.assertTrue(StringUtils.equals("string1",node.getStringValues().iterator().next().getKey()));
        Assert.assertTrue(StringUtils.equals("string1",node.getStringValues().iterator().next().getValue()));
    }

    /**
     * Test only ListValue nodes
     */
    @Test
    public void createInputNodeListValues(){
        Map<String,Object> toAdd = new HashMap<>();
        List<String> strList = new ArrayList<>();
        strList.add("list1");
        toAdd.put("list1",strList);
        InputNode node = InputNodeCreator.createInputNodeFromMap(toAdd);
        Assert.assertNotNull(node);
        Assert.assertNull(node.getStringValues());
        Assert.assertNotNull(node.getListValues());
        Assert.assertEquals(1,node.getListValues().size());
        Assert.assertTrue(StringUtils.equals("list1",node.getListValues().iterator().next().getKey()));
        Assert.assertEquals(1,node.getListValues().iterator().next().getValue().size());
        Assert.assertTrue(StringUtils.equals("list1",node.getListValues().iterator().next().getValue().get(0)));
    }
}
