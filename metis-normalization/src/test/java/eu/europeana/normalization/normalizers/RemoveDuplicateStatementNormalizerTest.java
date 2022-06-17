package eu.europeana.normalization.normalizers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import eu.europeana.normalization.normalizers.RemoveDuplicateStatementNormalizer.TextAttributesPair;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;


class RemoveDuplicateStatementNormalizerTest {

  @Test
  void testConvertToMap() {

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
  void testPairEquals() {

    // Test with incompatible objects
    final TextAttributesPair pair = new TextAttributesPair(null, null);
    assertNotNull(pair);
    assertNotEquals(pair, new Object());

    // Test with itself
    assertEquals(pair, pair);

    // Test with actual and empty names
    assertEquals(new TextAttributesPair("A", null), new TextAttributesPair("A", null));
    assertNotEquals(new TextAttributesPair("A", null), new TextAttributesPair("B", null));
    assertEquals(new TextAttributesPair("", null), new TextAttributesPair(null, null));

    // Test with attribute values
    assertEquals(new TextAttributesPair("", Collections.singletonMap("A", "X")),
        new TextAttributesPair("", Collections.singletonMap("A", "X")));
    assertNotEquals(new TextAttributesPair("", Collections.singletonMap("A", "X")),
        new TextAttributesPair("", Collections.singletonMap("A", "x")));
    assertNotEquals(new TextAttributesPair("", Collections.singletonMap("A", "X")),
        new TextAttributesPair("", Collections.singletonMap("a", "X")));
    assertNotEquals(new TextAttributesPair("", Collections.singletonMap("A", "X")), new TextAttributesPair("", null));
    Map<String, String> multipleAttributes = new HashMap<>();
    multipleAttributes.put("A", "X");
    multipleAttributes.put("B", "Y");
    assertNotEquals(new TextAttributesPair("", Collections.singletonMap("A", "X")),
        new TextAttributesPair("", multipleAttributes));
    assertEquals(new TextAttributesPair("", multipleAttributes), new TextAttributesPair("", multipleAttributes));
  }
}
