package eu.europeana.normalization.client;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import eu.europeana.normalization.client.NormalizationClient;
import eu.europeana.normalization.model.NormalizedBatchResult;

/**
 * Command line test for the NormalizationLanguageClient
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/05/2016
 */
public class TestNormalizationClient {

    public static void main(String[] args) {
    	try {
            NormalizationClient client = new NormalizationClient("http://localhost:8080/rest/");

            List<String> recs=new ArrayList<>();
            
            FileInputStream in = new FileInputStream(new File("src/test/samples/edm-record.xml"));
    		recs.add(IOUtils.toString(in, "UTF-8"));
    		in.close();

    		in = new FileInputStream(new File("src/test/samples/edm-record-internal.xml"));
    		recs.add(IOUtils.toString(in, "UTF-8"));
    		in.close();
            
            
            NormalizedBatchResult normalizedEdm = client.normalize(recs);
                System.out.println(normalizedEdm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
