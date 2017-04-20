package eu.europeana.metis.mapping.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MetisMappingUtil {

	/**
	 * To test JSON structure of the created model.
	 * @param jsonObject is an arbitrary Java object
	 * @return a JSON representation (as String) of the object
	 */
	public static String toJson(Object jsonObject) {
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();
	return gson.toJson(jsonObject);
	}
	
	/**
	 * The utility method to create the object model for a list of pairs "name":"value".<br/>
	 * In JSON structure it will look as follows:
	 * <pre>
	 *  "some_JSON_object": [
	 *  {
	 *  	"name1": "pairs[0].key",
	 *  	"name2": "pairs[0].value"
	 *  },
	 *  {
	 *  	"name1": "pairs[1].key",
	 *  	"name2": "pairs[1].value"
	 *  },
	 *   {
	 *  	"name1": "pairs[2].key",
	 *  	"name2": "pairs[2].value"
	 *  },
	 *  ...
	 *  ]
	 *  </pre>
	 * @param pairs is a list of pairs of values to be populated
	 * @param name1 is the name of the first parameter
	 * @param name2 is the name of the second parameter
	 * @return an object that is aligned with the described above JSON structure.
	 */
	public static List<Map<String, String>> buildSimplePairs(List<Entry<String, String>> pairs, String name1, String name2) {
		List<Map<String, String>> map = new ArrayList<>();
		for (Entry<String, String> pair : pairs) {
			Map<String, String> builtMap = new HashMap<>();
			builtMap.put(name1, pair.getKey());
			builtMap.put(name2, pair.getValue());
			map.add(builtMap);
		}
		return map;
	}
}
