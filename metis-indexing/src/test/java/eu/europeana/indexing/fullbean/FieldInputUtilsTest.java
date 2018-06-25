package eu.europeana.indexing.fullbean;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.LiteralType.Lang;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.corelib.definitions.jibx.ResourceType;

/**
 * FieldInputUtils unit tests
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class FieldInputUtilsTest {

  @Test
  public void testCreateLiteralMapFromObject() {
    LiteralType obj = new LiteralType();
    Lang lang = new Lang();
    lang.setLang("en");
    obj.setLang(lang);
    obj.setString("str");
    Map<String, List<String>> testMap = FieldInputUtils.createLiteralMapFromString(obj);
    Assert.assertNotNull(testMap);
    Assert.assertEquals(1, testMap.size());
    Assert.assertEquals("en", testMap.keySet().iterator().next());
    Assert.assertEquals("str", testMap.get("en").get(0));
  }

  @Test
  public void testCreateLiteralMapFromString() {
    Map<String, List<String>> testMap = FieldInputUtils.createLiteralMapFromString("str");
    Assert.assertNotNull(testMap);
    Assert.assertEquals(1, testMap.size());
    Assert.assertEquals("def", testMap.keySet().iterator().next());
    Assert.assertEquals("str", testMap.get("def").get(0));
  }

  @Test
  public void testCreateResourceOrLiteralMapFromString() {

    ResourceOrLiteralType obj = new ResourceOrLiteralType();
    ResourceOrLiteralType.Lang lang = new ResourceOrLiteralType.Lang();
    lang.setLang("en");
    obj.setLang(lang);
    obj.setString("str");
    Map<String, List<String>> testMap = FieldInputUtils.createResourceOrLiteralMapFromString(obj);
    Assert.assertNotNull(testMap);
    Assert.assertEquals(1, testMap.size());
    Assert.assertEquals("en", testMap.keySet().iterator().next());
    Assert.assertEquals("str", testMap.get("en").get(0));

    ResourceOrLiteralType obj2 = new ResourceOrLiteralType();
    Resource res = new Resource();
    res.setResource("str");
    obj2.setResource(res);
    Map<String, List<String>> testMap2 = FieldInputUtils.createResourceOrLiteralMapFromString(obj2);
    Assert.assertNotNull(testMap2);
    Assert.assertEquals(1, testMap2.size());
    Assert.assertEquals("def", testMap2.keySet().iterator().next());
    Assert.assertEquals("str", testMap2.get("def").get(0));

    ResourceOrLiteralType obj3 = new ResourceOrLiteralType();
    ResourceOrLiteralType.Lang lang3 = new ResourceOrLiteralType.Lang();
    lang3.setLang("en");
    obj3.setLang(lang3);
    obj3.setString("str");
    Resource res2 = new Resource();
    res2.setResource("str2");
    obj3.setResource(res2);
    Map<String, List<String>> testMap3 = FieldInputUtils.createResourceOrLiteralMapFromString(obj3);
    Assert.assertNotNull(testMap3);
    Assert.assertEquals(1, testMap3.size());
    Assert.assertEquals("en", testMap3.keySet().iterator().next());
    Assert.assertEquals("str", testMap3.get("en").get(0));
    Assert.assertEquals("str2", testMap3.get("en").get(1));
  }

  @Test
  public void testCreateLiteralMapFromList() {
    List<LiteralType> listA = new ArrayList<LiteralType>();
    List<LiteralType> listB = new ArrayList<LiteralType>();
    List<LiteralType> listC = new ArrayList<LiteralType>();
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
    Assert.assertNotNull(mapA);
    Assert.assertEquals(1, mapA.size());
    Assert.assertEquals("en", mapA.keySet().iterator().next());
    Assert.assertEquals("strA", mapA.get("en").get(0));
    Assert.assertEquals("strB", mapA.get("en").get(1));

    ltC.setString("strC");
    ltC.setLang(lang);
    ltD.setString("strD");
    listB.add(ltC);
    listB.add(ltD);
    Map<String, List<String>> mapB = FieldInputUtils.createLiteralMapFromList(listB);
    Assert.assertNotNull(mapB);
    Assert.assertEquals(2, mapB.size());
    Assert.assertTrue(mapB.containsKey("def"));
    Assert.assertTrue(mapB.containsKey("en"));
    Assert.assertEquals("strC", mapB.get("en").get(0));
    Assert.assertEquals("strD", mapB.get("def").get(0));

    ltE.setString("strE");
    ltF.setString("strF");
    listC.add(ltE);
    listC.add(ltF);
    Map<String, List<String>> mapC = FieldInputUtils.createLiteralMapFromList(listC);
    Assert.assertNotNull(mapC);
    Assert.assertEquals(1, mapC.size());
    Assert.assertTrue(mapC.containsKey("def"));
    Assert.assertEquals("strE", mapC.get("def").get(0));
    Assert.assertEquals("strF", mapC.get("def").get(1));
  }

  @Test
  public void testCreateResourceOrLiteralMapFromList() {

    List<ResourceOrLiteralType> listA = new ArrayList<ResourceOrLiteralType>();
    List<ResourceOrLiteralType> listB = new ArrayList<ResourceOrLiteralType>();
    List<ResourceOrLiteralType> listC = new ArrayList<ResourceOrLiteralType>();
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
    Assert.assertNotNull(mapA);
    Assert.assertEquals(1, mapA.size());
    Assert.assertEquals("en", mapA.keySet().iterator().next());
    Assert.assertEquals("strA", mapA.get("en").get(0));
    Assert.assertEquals("strB", mapA.get("en").get(1));

    ltC.setString("strC");
    ltC.setLang(lang);
    ltD.setString("strD");
    listB.add(ltC);
    listB.add(ltD);
    Map<String, List<String>> mapB = FieldInputUtils.createResourceOrLiteralMapFromList(listB);
    Assert.assertNotNull(mapB);
    Assert.assertEquals(2, mapB.size());
    Assert.assertTrue(mapB.containsKey("def"));
    Assert.assertTrue(mapB.containsKey("en"));
    Assert.assertEquals("strC", mapB.get("en").get(0));
    Assert.assertEquals("strD", mapB.get("def").get(0));

    ltE.setString("strE");
    ltF.setString("strF");
    listC.add(ltE);
    listC.add(ltF);
    Map<String, List<String>> mapC = FieldInputUtils.createResourceOrLiteralMapFromList(listC);
    Assert.assertNotNull(mapC);
    Assert.assertEquals(1, mapC.size());
    Assert.assertTrue(mapC.containsKey("def"));
    Assert.assertEquals("strE", mapC.get("def").get(0));
    Assert.assertEquals("strF", mapC.get("def").get(1));

  }

  @Test
  public void testResourceOrLiteralListToArray() {
    List<ResourceOrLiteralType> rltList = new ArrayList<ResourceOrLiteralType>();
    rltList.add(prepareRLT());
    String[] rltArray = FieldInputUtils.resourceOrLiteralListToArray(rltList);
    String[] arr = new String[] {"test resource", "test string"};
    assertArrayEquals(arr, rltArray);
    assertNotNull(FieldInputUtils.resourceOrLiteralListToArray(null));
  }

  @Test
  public void testResourceListToArray() {
    List<ResourceType> rtList = new ArrayList<ResourceType>();
    rtList.add(prepareRT());
    String[] rtArray = FieldInputUtils.resourceListToArray(rtList);
    String[] arr = new String[] {"test resource"};
    assertArrayEquals(arr, rtArray);
    assertNotNull(FieldInputUtils.resourceListToArray(null));
  }

  @Test
  public void testGetResourceString() {
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
