package eu.europeana.validation.client;

import eu.europeana.validation.model.ValidationResult;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by ymamakis on 8/2/16.
 */
public class ClientTest {

    public static void main(String[] args) {
        ValidationManagementClient managementClient = new ValidationManagementClient();
        ValidationClient client = new ValidationClient();



        try {

            ValidationResult result = client.validateRecord("EDM-INTERNAL", FileUtils.readFileToString(new File("/home/ymamakis/git/metis-framework/metis-validation/validation-client/src/main/resources/test_internal.xml")),
                    "undefined"
            );
            System.out.println(result.isSuccess());
            System.out.println(result.getRecordId());
            System.out.println(result.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
