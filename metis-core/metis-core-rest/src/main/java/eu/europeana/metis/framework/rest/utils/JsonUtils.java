package eu.europeana.metis.framework.rest.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ymamakis on 11/11/16.
 */
public class JsonUtils {

    public static ModelAndView toJson(List<ModelAndView> object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String resultPage = "json";
        ModelAndView view = new ModelAndView(resultPage);
        int i=0;
        List<Map<String,Object>> orgs = new ArrayList<>();
        for(ModelAndView obj:object){
            orgs.add(obj.getModel());
            i++;
        }
        view.addObject("resultCount",i);
        view.addObject("results",orgs);
        return view;
    }


}
