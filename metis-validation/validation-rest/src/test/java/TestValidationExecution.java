import eu.europeana.validation.edm.model.ValidationResult;
import eu.europeana.validation.edm.model.ValidationResultList;
import eu.europeana.validation.edm.validation.ValidationExecutionService;
import eu.europeana.validation.edm.validation.ValidationManagementService;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by gmamakis on 18-12-15.
 */

public class TestValidationExecution {

    private static int i=0;
    @Before
    public void prepare(){
        if(i==0) {
            MongoProvider.start();
            ValidationManagementService service = new ValidationManagementService();
            try {
                service.createSchema("EDM-EXTERNAL", "EDM.xsd", "schematron/schematron.xsl", "undefined", this.getClass().
                        getClassLoader().getResourceAsStream("test_schema.zip"));
                service.createSchema("EDM-INTERNAL", "EDM-INTERNAL.xsd", "schematron/schematron-internal.xsl", "undefined", this.getClass().
                        getClassLoader().getResourceAsStream("test_schema.zip"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testSingleValidationSuccess() throws IOException, ExecutionException, InterruptedException {

        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473_test.xml"));
        ValidationExecutionService eservice = new ValidationExecutionService();
        ValidationResult result = eservice.singleValidation("EDM-INTERNAL","undefined",fileToValidate);
        Assert.assertEquals(true,result.isSuccess());
        Assert.assertNull(result.getRecordId());
        Assert.assertNull(result.getMessage());
        i++;
    }

    @Test
    public void testSingleValidationFailure() throws IOException, ExecutionException, InterruptedException {

        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473_wrong.xml"));
        ValidationExecutionService eservice = new ValidationExecutionService();
        ValidationResult result = eservice.singleValidation("EDM-INTERNAL","undefined",fileToValidate);
        Assert.assertEquals(false,result.isSuccess());
        Assert.assertNotNull(result.getRecordId());
        Assert.assertNotNull(result.getMessage());
        i++;
    }

    @Test
    public void testSingleValidationFailureWrongSchema() throws IOException, ExecutionException, InterruptedException {

        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473_test.xml"));
        ValidationExecutionService eservice = new ValidationExecutionService();
        ValidationResult result = eservice.singleValidation("EDM-EXTERNAL","undefined",fileToValidate);
        Assert.assertEquals(false,result.isSuccess());
        Assert.assertNotNull(result.getRecordId());
        Assert.assertNotNull(result.getMessage());
        i++;
    }

    @Test
    public void testBatchValidationSuccess() throws IOException, ExecutionException, InterruptedException, ZipException {


    String fileName = "src/test/resources/test";
        ZipFile file = new ZipFile("src/test/resources/test.zip");
        file.extractAll(fileName);

        File[] files = new File(fileName).listFiles();
        List<String> xmls = new ArrayList<>();
        for (File input : files) {
            xmls.add(IOUtils.toString(new FileInputStream(input)));
        }
        ValidationExecutionService eservice = new ValidationExecutionService();
        ValidationResultList result = eservice.batchValidation("EDM-INTERNAL","undefined",xmls);
        Assert.assertEquals(true,result.isSuccess());
        Assert.assertEquals(0, result.getResultList().size());

        FileUtils.forceDelete(new File(fileName));
        i++;
    }

    @Test
    public void testBatchValidationFailure() throws IOException, ExecutionException, InterruptedException, ZipException {

        String fileName = "src/test/resources/test_wrong";
        ZipFile file = new ZipFile("src/test/resources/test_wrong.zip");
        file.extractAll(fileName);

        File[] files = new File(fileName).listFiles();
        List<String> xmls = new ArrayList<>();
        for (File input : files) {
            xmls.add(IOUtils.toString(new FileInputStream(input)));
        }
        ValidationExecutionService eservice = new ValidationExecutionService();
        ValidationResultList result = eservice.batchValidation("EDM-INTERNAL","undefined",xmls);
        Assert.assertEquals(false,result.isSuccess());
        Assert.assertEquals(1, result.getResultList().size());


        FileUtils.forceDelete(new File(fileName));
        i++;
    }

    @Test
    public void testBatchValidationFailureWrongSchema() throws IOException, ExecutionException, InterruptedException, ZipException {

        String fileName = "src/test/resources/test";
        ZipFile file = new ZipFile("src/test/resources/test.zip");
        file.extractAll(fileName);

        File[] files = new File(fileName).listFiles();
        List<String> xmls = new ArrayList<>();
        for (File input : files) {
            xmls.add(IOUtils.toString(new FileInputStream(input)));
        }
        ValidationExecutionService eservice = new ValidationExecutionService();
        ValidationResultList result = eservice.batchValidation("EDM-EXTERNAL","undefined",xmls);
        Assert.assertEquals(false,result.isSuccess());
        Assert.assertEquals(1506, result.getResultList().size());


        FileUtils.forceDelete(new File(fileName));
        i++;
    }

    @After
    public void destroy(){
        if(i==5) {
            MongoProvider.stop();
        }
    }
}
