package eu.europeana.metis.dereference.client;

import eu.europeana.metis.dereference.Vocabulary;
import org.apache.commons.io.IOUtils;

/**
 * Created by gmamakis on 1-3-16.
 */
public class DereferenceClientMain {

    public static void main (String[] args){
        try {
            DereferenceClient client = new DereferenceClient();
            Vocabulary geonames = new Vocabulary();
            geonames.setURI("http://sws.geonames.org/");
            geonames.setXslt(IOUtils.toString(DereferenceClientMain.class.getClassLoader().getResourceAsStream("geonames.xsl")));
            geonames.setRules("*");
            geonames.setName("Geonames");
            geonames.setIterations(0);
            client.createVocabulary(geonames);
            client.deleteEntity("http://sws.geonames.org/3020251");
            System.out.println(client.dereference("http://sws.geonames.org/3020251"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
