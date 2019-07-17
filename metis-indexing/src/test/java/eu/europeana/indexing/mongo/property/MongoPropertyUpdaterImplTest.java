package eu.europeana.indexing.mongo.property;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MongoPropertyUpdaterImplTest {
	
	@Test
	void testMapEquals() {
		Map<String,List<String>> mapA = new HashMap<>();
		Map<String,List<String>> mapB = new HashMap<>();
		Map<String,List<String>> mapC = new HashMap<>();
		Map<String,List<String>> mapD = new HashMap<>();
		Map<String,List<String>> mapE = new HashMap<>();
		List<String> listA = new ArrayList<>();
		List<String> listB = new ArrayList<>();
		List<String> listC = new ArrayList<>();
		List<String> listD = new ArrayList<>();
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
		assertTrue(MongoPropertyUpdaterImpl.mapEquals(mapA, mapB));
		assertFalse(MongoPropertyUpdaterImpl.mapEquals(mapA, mapC));
		assertFalse(MongoPropertyUpdaterImpl.mapEquals(mapA, mapD));
		assertFalse(MongoPropertyUpdaterImpl.mapEquals(mapA, mapE));
	}

	@Test
	void testArrayEquals() {
		String[] arrA = new String[]{"1","2","3"};
		String[] arrB = new String[]{"1","3","2"};
		String[] arrC = new String[]{"1","2"};
		String[] arrD = new String[]{"1","2","4"};
		assertTrue(MongoPropertyUpdaterImpl.arrayEquals(arrA, arrB));
		assertFalse(MongoPropertyUpdaterImpl.arrayEquals(arrA, arrC));
		assertFalse(MongoPropertyUpdaterImpl.arrayEquals(arrA, arrD));
	}
}
