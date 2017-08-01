package eu.europeana.metis.preview.rest.client;


import eu.europeana.metis.preview.common.model.ExtendedValidationResult;
import java.io.File;

/**
 * Created by ymamakis on 9/12/16.
 */
public class PreviewClientTest {
    public static void main(String[] args){
        PreviewClient client = new PreviewClient("http://metis-preview-test.cfapps.io/");
       // ExtendedValidationResult result = client.previewRecords(new File("/home/ymamakis/git/metis-framework/metis-preview/metis-preview-client/src/main/resources/test.xml.zip"),"000002",true);
        ExtendedValidationResult result = client.previewRecords(new File("/home/ymamakis/Downloads/Item_33532982.zip"),"000009",true,"EDM_external2internal_v2_repox.xsl",true);
        System.out.println(result.getPortalUrl());
    }
}
