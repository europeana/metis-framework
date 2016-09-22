package eu.europeana.metis.mapping.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.utils.XSLTGenerator;

import java.io.File;
import java.io.IOException;

/**
 * Created by ymamakis on 9/20/16.
 */
public class XSLTest {
    public static void main(String[] args){
        try {
            Mapping mapping = new ObjectMapper().readValue(new File("/home/ymamakis/Desktop/test_mapping.json"),Mapping.class);

            XSLTGenerator gen = new XSLTGenerator();
            System.out.println(gen.generateFromMappings(mapping));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
