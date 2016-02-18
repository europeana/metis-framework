package eu.europeana.metis.framework.rest.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by gmamakis on 12-2-16.
 */
public class JsonUtils {
    /**
     * Convert an object to a Response
     * @param obj The object to convert
     * @return The ModelAndView of the object
     */
    public static ModelAndView toJson(Object obj){
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return new ModelAndView(objectMapper.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
