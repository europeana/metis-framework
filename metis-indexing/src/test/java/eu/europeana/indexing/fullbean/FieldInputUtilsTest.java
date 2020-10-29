package eu.europeana.indexing.fullbean;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.LiteralType.Lang;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.metis.schema.jibx.ResourceType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * FieldInputUtils unit tests
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
class FieldInputUtilsTest {

  @Test
  void testCreateLiteralMapFromObject() {
    LiteralType obj = new LiteralType();
    Lang lang = new Lang();
    lang.setLang("en");
    obj.setLang(lang);
    obj.setString("str");
    Map<String, List<String>> testMap = FieldInputUtils.createLiteralMapFromString(obj);
    assertNotNull(testMap);
    assertEquals(1, testMap.size());
    assertEquals("en", testMap.keySet().iterator().next());
    assertEquals("str", testMap.get("en").get(0));

    assertNull(FieldInputUtils.createLiteralMapFromString(null));
    assertNull(FieldInputUtils.createLiteralMapFromString(new LiteralType()));
  }

  @Test
  void testCreateLiteralMapFromString() {
    Map<String, List<String>> testMap = FieldInputUtils.createMapFromString("str");
    assertNotNull(testMap);
    assertEquals(1, testMap.size());
    assertEquals("def", testMap.keySet().iterator().next());
    assertEquals("str", testMap.get("def").get(0));

    assertNull(FieldInputUtils.createMapFromString(null));
    assertNull(FieldInputUtils.createMapFromString(" "));
  }

  @Test
  void testCreateResourceOrLiteralMapFromString() {

    ResourceOrLiteralType obj = new ResourceOrLiteralType();
    ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
    lang.setLang("en");
    obj.setLang(lang);
    obj.setString("str");
    Map<String, List<String>> testMap = FieldInputUtils.createResourceOrLiteralMapFromString(obj);
    assertNotNull(testMap);
    assertEquals(1, testMap.size());
    assertEquals("en", testMap.keySet().iterator().next());
    assertEquals("str", testMap.get("en").get(0));

    ResourceOrLiteralType obj2 = new ResourceOrLiteralType();
    Resource res = new Resource();
    res.setResource("str");
    obj2.setResource(res);
    Map<String, List<String>> testMap2 = FieldInputUtils.createResourceOrLiteralMapFromString(obj2);
    assertNotNull(testMap2);
    assertEquals(1, testMap2.size());
    assertEquals("def", testMap2.keySet().iterator().next());
    assertEquals("str", testMap2.get("def").get(0));

    ResourceOrLiteralType obj3 = new ResourceOrLiteralType();
    ResourceOrLiteralType.Lang lang3 = new ResourceOrLiteralType.Lang();
    lang3.setLang("en");
    obj3.setLang(lang3);
    obj3.setString("str");
    Resource res2 = new Resource();
    res2.setResource("str2");
    obj3.setResource(res2);
    Map<String, List<String>> testMap3 = FieldInputUtils.createResourceOrLiteralMapFromString(obj3);
    assertNotNull(testMap3);
    assertEquals(1, testMap3.size());
    assertEquals("en", testMap3.keySet().iterator().next());
    assertEquals("str", testMap3.get("en").get(0));
    assertEquals("str2", testMap3.get("en").get(1));

    assertNull(FieldInputUtils.createResourceOrLiteralMapFromString(null));
    assertNull(FieldInputUtils.createResourceOrLiteralMapFromString(new ResourceOrLiteralType()));
  }

  @Test
  void testCreateLiteralMapFromList() {
    List<LiteralType> listA = new ArrayList<>();
    List<LiteralType> listB = new ArrayList<>();
    List<LiteralType> listC = new ArrayList<>();
    LiteralType ltA = new LiteralType();

    LiteralType ltB = new LiteralType();
    LiteralType ltC = new LiteralType();
    LiteralType ltD = new LiteralType();
    LiteralType ltE = new LiteralType();
    LiteralType ltF = new LiteralType();
    Lang lang = new Lang();
    lang.setLang("en");
    ltA.setString("strA");
    ltA.setLang(lang);
    ltB.setString("strB");
    ltB.setLang(lang);
    listA.add(ltA);
    listA.add(ltB);
    Map<String, List<String>> mapA = FieldInputUtils.createLiteralMapFromList(listA);
    assertNotNull(mapA);
    assertEquals(1, mapA.size());
    assertEquals("en", mapA.keySet().iterator().next());
    assertEquals("strA", mapA.get("en").get(0));
    assertEquals("strB", mapA.get("en").get(1));

    ltC.setString("strC");
    ltC.setLang(lang);
    ltD.setString("strD");
    listB.add(ltC);
    listB.add(ltD);
    Map<String, List<String>> mapB = FieldInputUtils.createLiteralMapFromList(listB);
    assertNotNull(mapB);
    assertEquals(2, mapB.size());
    assertTrue(mapB.containsKey("def"));
    assertTrue(mapB.containsKey("en"));
    assertEquals("strC", mapB.get("en").get(0));
    assertEquals("strD", mapB.get("def").get(0));

    ltE.setString("strE");
    ltF.setString("strF");
    listC.add(ltE);
    listC.add(ltF);
    Map<String, List<String>> mapC = FieldInputUtils.createLiteralMapFromList(listC);
    assertNotNull(mapC);
    assertEquals(1, mapC.size());
    assertTrue(mapC.containsKey("def"));
    assertEquals("strE", mapC.get("def").get(0));
    assertEquals("strF", mapC.get("def").get(1));

    assertNull(FieldInputUtils.createLiteralMapFromList(null));
    assertNull(FieldInputUtils.createLiteralMapFromList(Collections.emptyList()));
  }

  @Test
  void testCreateResourceOrLiteralMapFromList() {

    List<ResourceOrLiteralType> listA = new ArrayList<>();
    List<ResourceOrLiteralType> listB = new ArrayList<>();
    List<ResourceOrLiteralType> listC = new ArrayList<>();
    ResourceOrLiteralType ltA = new ResourceOrLiteralType();

    ResourceOrLiteralType ltB = new ResourceOrLiteralType();
    ResourceOrLiteralType ltC = new ResourceOrLiteralType();
    ResourceOrLiteralType ltD = new ResourceOrLiteralType();
    ResourceOrLiteralType ltE = new ResourceOrLiteralType();
    ResourceOrLiteralType ltF = new ResourceOrLiteralType();
    ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
    lang.setLang("en");
    ltA.setString("strA");
    ltA.setLang(lang);
    ltB.setString("strB");
    ltB.setLang(lang);
    listA.add(ltA);
    listA.add(ltB);
    Map<String, List<String>> mapA = FieldInputUtils.createResourceOrLiteralMapFromList(listA);
    assertNotNull(mapA);
    assertEquals(1, mapA.size());
    assertEquals("en", mapA.keySet().iterator().next());
    assertEquals("strA", mapA.get("en").get(0));
    assertEquals("strB", mapA.get("en").get(1));

    ltC.setString("strC");
    ltC.setLang(lang);
    ltD.setString("strD");
    listB.add(ltC);
    listB.add(ltD);
    Map<String, List<String>> mapB = FieldInputUtils.createResourceOrLiteralMapFromList(listB);
    assertNotNull(mapB);
    assertEquals(2, mapB.size());
    assertTrue(mapB.containsKey("def"));
    assertTrue(mapB.containsKey("en"));
    assertEquals("strC", mapB.get("en").get(0));
    assertEquals("strD", mapB.get("def").get(0));

    ltE.setString("strE");
    ltF.setString("strF");
    listC.add(ltE);
    listC.add(ltF);
    Map<String, List<String>> mapC = FieldInputUtils.createResourceOrLiteralMapFromList(listC);
    assertNotNull(mapC);
    assertEquals(1, mapC.size());
    assertTrue(mapC.containsKey("def"));
    assertEquals("strE", mapC.get("def").get(0));
    assertEquals("strF", mapC.get("def").get(1));

    assertNull(FieldInputUtils.createResourceOrLiteralMapFromList(null));
    assertNull(FieldInputUtils.createResourceOrLiteralMapFromList(Collections.emptyList()));
  }

  @Test
  void testResourceOrLiteralListToArray() {
    List<ResourceOrLiteralType> rltList = new ArrayList<>();
    rltList.add(prepareRLT());
    String[] rltArray = FieldInputUtils.resourceOrLiteralListToArray(rltList);
    String[] arr = new String[] {"test string", "test resource"};
    assertArrayEquals(arr, rltArray);
    assertNotNull(FieldInputUtils.resourceOrLiteralListToArray(null));
  }

  @Test
  void testResourceListToArray() {
    List<ResourceType> rtList = new ArrayList<>();
    rtList.add(prepareRT());
    String[] rtArray = FieldInputUtils.resourceListToArray(rtList);
    String[] arr = new String[] {"test resource"};
    assertArrayEquals(arr, rtArray);
    assertNotNull(FieldInputUtils.resourceListToArray(null));
  }

  @Test
  void testGetResourceString() {
    assertEquals("test resource", FieldInputUtils.getResourceString(prepareRT()));
  }

  private ResourceOrLiteralType prepareRLT() {
    ResourceOrLiteralType rlt = new ResourceOrLiteralType();
    ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
    lang.setLang("en");
    rlt.setLang(lang);
    Resource res = new Resource();
    res.setResource("test resource");
    rlt.setResource(res);
    rlt.setString("test string");
    return rlt;
  }

  private ResourceType prepareRT() {
    ResourceType rt = new ResourceType();
    rt.setResource("test resource");
    return rt;
  }
}
