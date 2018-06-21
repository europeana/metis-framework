package eu.europeana.indexing.mongo.property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class FieldUpdateUtilsTest {
	
	@Test
	public void testMapEquals() {
		Map<String,List<String>> mapA = new HashMap<String, List<String>>();
		Map<String,List<String>> mapB = new HashMap<String, List<String>>();
		Map<String,List<String>> mapC = new HashMap<String, List<String>>();
		Map<String,List<String>> mapD = new HashMap<String, List<String>>();
		Map<String,List<String>> mapE = new HashMap<String, List<String>>();
		List<String> listA = new ArrayList<String>();
		List<String> listB = new ArrayList<String>();
		List<String> listC = new ArrayList<String>();
		List<String> listD = new ArrayList<String>();
		listA.add("1");
		listA.add("2");
		listB.add("3");
		listB.add("4");
		listC.add("1");
		listC.add("2");
		listD.add("4");
		listD.add("3");
		mapA.put("1",listA);
		mapA.put("2", listB);
		mapB.put("1", listC);
		mapB.put("2", listD);
		mapC.put("1",listA);
		mapC.put("2", listA);
		mapD.put("1",listA);
		mapE.put("1", listA);
		mapE.put("3", listB);
		Assert.assertTrue(FieldUpdateUtils.mapEquals(mapA, mapB));
		Assert.assertFalse(FieldUpdateUtils.mapEquals(mapA, mapC));
		Assert.assertFalse(FieldUpdateUtils.mapEquals(mapA, mapD));
		Assert.assertFalse(FieldUpdateUtils.mapEquals(mapA, mapE));
	}

	@Test
	public void testArrayEquals() {
		String[] arrA = new String[]{"1","2","3"};
		String[] arrB = new String[]{"1","3","2"};
		String[] arrC = new String[]{"1","2"};
		String[] arrD = new String[]{"1","2","4"};
		Assert.assertTrue(FieldUpdateUtils.arrayEquals(arrA, arrB));
		Assert.assertFalse(FieldUpdateUtils.arrayEquals(arrA, arrC));
		Assert.assertFalse(FieldUpdateUtils.arrayEquals(arrA, arrD));
	}
}
