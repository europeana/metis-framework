package eu.europeana.metis.framework.rest.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ymamakis on 11/11/16.
 */
public class JsonUtils {

    public static ModelAndView toJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return toJson(objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String resultPage = "json";
        Map<String, Object> model = new HashMap<>();
        return new ModelAndView(resultPage, model);

    }

    public static ModelAndView toJson(String json) {
        String resultPage = "json";
        Map<String, Object> model = new HashMap<>();
        model.put("json", json);
        return new ModelAndView(resultPage, model);
    }

}
