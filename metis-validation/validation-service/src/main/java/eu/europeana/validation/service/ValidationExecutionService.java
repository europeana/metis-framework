package eu.europeana.validation.service;


import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.model.ValidationResultList;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Schema service service
 * Created by gmamakis on 18-12-15.
 */
public class ValidationExecutionService {
    private static final ExecutorService es = Executors.newFixedThreadPool(10);
    private static final ExecutorCompletionService cs = new ExecutorCompletionService(es);
    @Autowired
    private ValidationManagementService service;

    /**
     * Perform single service given a schema.
     * @param schema The schema to perform service against.
     * @param document The document to validate against
     * @return A service result
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public ValidationResult singleValidation(final String schema,final String version, final String document) throws InterruptedException,ExecutionException{
            return submit(new Validator(schema, document, version, service,Configuration.getInstance().getResolver())).get();
    }

    private Future<ValidationResult> submit(Validator validator) throws InterruptedException {
        cs.submit(validator);
        return cs.take();
    }

    /**
     * Batch service given a schema
     * @param schema The schema to validate against
     * @param documents The documents to validate
     * @return A list of service results
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public ValidationResultList batchValidation(final String schema, final String version, List<String> documents) throws InterruptedException,ExecutionException{
        List<ValidationResult> results = new ArrayList<>();
        for(final String document : documents) {
           cs.submit(new Validator(schema,document, version, service,Configuration.getInstance().getResolver()));
        }
        for(int i=0;i<documents.size();i++) {
            Future<ValidationResult> future = cs.take();
            ValidationResult res = future.get();
            if(!res.isSuccess()){
                results.add(res);
            }
        }

        ValidationResultList resultList = new ValidationResultList();
        resultList.setResultList(results);
        if(resultList.getResultList().size()==0) {
            resultList.setSuccess(true);
        }
        return resultList;

    }

}
