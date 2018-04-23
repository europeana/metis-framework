package eu.europeana.normalization.normalizers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import eu.europeana.normalization.normalizers.RemoveDuplicateStatementNormalizer.TextAttributesPair;


public class RemoveDuplicateStatementNormalizerTest {

  @Test
  public void testConvertToMap() {

    // Create attributes with proper values, two of which are very similar
    final String name1 = "a1";
    final String name2 = "A1";
    final String name3 = "a2";
    final String value12 = "v";
    final String value3 = "X";
    final Attr attribute1 = createAttribute(name1, value12);
    final Attr attribute2 = createAttribute(name2, value12);
    final Attr attribute3 = createAttribute(name3, value3);

    // Test conversion for attributes with proper values.
    final NamedNodeMap attributes1 = createNamedNodeMap(attribute1, attribute2, attribute3);
    final Map<String, String> map1 = TextAttributesPair.convertToMap(attributes1);
    assertEquals(3, map1.size());
    assertEquals(value12, map1.get(name1));
    assertEquals(value12, map1.get(name2));
    assertEquals(value3, map1.get(name3));

    // Test that blank and null attributes are ignored.
    final Attr attribute4 = createAttribute("a2", "");
    final Attr attribute5 = createAttribute("a3", null);
    final NamedNodeMap attributes2 = createNamedNodeMap(attribute4, attribute5);
    final Map<String, String> map2 = TextAttributesPair.convertToMap(attributes2);
    assertTrue(map2.isEmpty());
  }

  private Attr createAttribute(String name, String value) {
    final Attr attribute = mock(Attr.class);
    doReturn(name).when(attribute).getName();
    doReturn(name).when(attribute).getNodeName();
    doReturn(value).when(attribute).getValue();
    doReturn(value).when(attribute).getNodeValue();
    return attribute;
  }

  private NamedNodeMap createNamedNodeMap(Attr... attributes) {
    final NamedNodeMap map = mock(NamedNodeMap.class);
    doReturn(attributes.length).when(map).getLength();
    doAnswer(invocation -> attributes[(int) invocation.getArgument(0)]).when(map).item(anyInt());
    return map;
  }

  @Test
  public void testPairEquals() {

    // Test with incompatible objects
    final TextAttributesPair pair = new TextAttributesPair(null, null);
    assertFalse(pair.equals(null));
    assertFalse(pair.equals(new Object()));

    // Test with itself
    assertTrue(pair.equals(pair));

    // Test with actual and empty names
    assertTrue(new TextAttributesPair("A", null).equals(new TextAttributesPair("A", null)));
    assertFalse(new TextAttributesPair("A", null).equals(new TextAttributesPair("B", null)));
    assertTrue(new TextAttributesPair("", null).equals(new TextAttributesPair(null, null)));

    // Test with attribute values
    assertTrue(new TextAttributesPair("", Collections.singletonMap("A", "X"))
        .equals(new TextAttributesPair("", Collections.singletonMap("A", "X"))));
    assertFalse(new TextAttributesPair("", Collections.singletonMap("A", "X"))
        .equals(new TextAttributesPair("", Collections.singletonMap("A", "x"))));
    assertFalse(new TextAttributesPair("", Collections.singletonMap("A", "X"))
        .equals(new TextAttributesPair("", Collections.singletonMap("a", "X"))));
    assertFalse(new TextAttributesPair("", Collections.singletonMap("A", "X"))
        .equals(new TextAttributesPair("", null)));
    Map<String, String> multipleAttributes = new HashMap<>();
    multipleAttributes.put("A", "X");
    multipleAttributes.put("B", "Y");
    assertFalse(new TextAttributesPair("", Collections.singletonMap("A", "X"))
        .equals(new TextAttributesPair("", multipleAttributes)));
    assertTrue(new TextAttributesPair("", multipleAttributes)
        .equals(new TextAttributesPair("", multipleAttributes)));
  }
}
