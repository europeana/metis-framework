package research;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class JsonUtil {

  public static Map<String, Object> readJsonMap(File input) throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    Map<String, Object> map = mapper.readValue(input,
        new TypeReference<Map<String, Object>>() {
        });

    return map;
  }
}
