/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
import eu.europeana.validation.model.Record;
import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.model.ValidationResultList;
import eu.europeana.validation.service.ValidationExecutionService;
import eu.europeana.validation.service.ValidationManagementService;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by gmamakis on 18-12-15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestApplication.class, loader = AnnotationConfigContextLoader.class)
@WebAppConfiguration
public class TestValidationExecution {

    @Autowired
    ValidationManagementService service;
    @Autowired
    ValidationExecutionService validationExecutionService;
    private static int i=0;
    @Before
    public void prepare(){
        if(i==0) {
           // MongoProvider.start();

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
        ValidationResult result = validationExecutionService.singleValidation("EDM-INTERNAL","undefined",fileToValidate);
        Assert.assertEquals(true,result.isSuccess());
        Assert.assertNull(result.getRecordId());
        Assert.assertNull(result.getMessage());
        i++;
    }

    @Test
    public void testSingleValidationFailure() throws IOException, ExecutionException, InterruptedException {

        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473_wrong.xml"));
        ValidationResult result = validationExecutionService.singleValidation("EDM-INTERNAL","undefined",fileToValidate);
        Assert.assertEquals(false,result.isSuccess());
        Assert.assertNotNull(result.getRecordId());
        Assert.assertNotNull(result.getMessage());
        i++;
    }

    @Test
    public void testSingleValidationFailureWrongSchema() throws IOException, ExecutionException, InterruptedException {

        String fileToValidate = IOUtils.toString(new FileInputStream("src/test/resources/Item_35834473_test.xml"));
        ValidationResult result = validationExecutionService.singleValidation("EDM-EXTERNAL","undefined",fileToValidate);
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
        List<Record> xmls = new ArrayList<>();
        for (File input : files) {
            Record record = new Record();
            record.setRecord(IOUtils.toString(new FileInputStream(input)));
            xmls.add(record);
        }
        ValidationResultList result = validationExecutionService.batchValidation("EDM-INTERNAL","undefined",xmls);
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
        List<Record> xmls = new ArrayList<>();
        for (File input : files) {
            Record record = new Record();
            record.setRecord(IOUtils.toString(new FileInputStream(input)));
            xmls.add(record);
        }
        ValidationResultList result = validationExecutionService.batchValidation("EDM-INTERNAL","undefined",xmls);
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
        List<Record> xmls = new ArrayList<>();
        for (File input : files) {
            Record record = new Record();
            record.setRecord(IOUtils.toString(new FileInputStream(input)));
            xmls.add(record);
        }
        ValidationResultList result = validationExecutionService.batchValidation("EDM-EXTERNAL","undefined",xmls);
        Assert.assertEquals(false,result.isSuccess());
        Assert.assertEquals(1506, result.getResultList().size());


        FileUtils.forceDelete(new File(fileName));
        i++;
    }

    @After
    public void destroy(){
        if(i==5) {
           // MongoProvider.stop();
        }
    }
}
