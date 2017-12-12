package eu.europeana.validation.client;

import eu.europeana.validation.model.Record;
import eu.europeana.validation.model.ValidationResult;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymamakis on 8/2/16.
 */
public class ClientTest {

    public static void main(String[] args) {
        ValidationClient client = new ValidationClient();
        ClientTest clientTest = new ClientTest();

        try {

            ValidationResult result = client.validateRecord("EDM-INTERNAL", FileUtils.readFileToString(clientTest.getFile("Item_35834473.xml")));
            System.out.println(result.isSuccess());
            System.out.println(result.getRecordId());
            System.out.println(result.getMessage());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }
}
