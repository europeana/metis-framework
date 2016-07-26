package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.internal.MongoTermList;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymamakis on 3/3/16.
 */
public class MongoDatabaseUtilsMain {

    public static void main (String[] args){
        MongoDatabaseUtils.dbExists("136.243.103.29",27017);
        /**List<String> str = new ArrayList<>();
        str.add("http://sws.geonames.org/3094802/");
        MongoDatabaseUtils.delete(str);*/

        try {
           MongoTermList termList =  MongoDatabaseUtils.findByLabel("Victor Hugo","people");
            System.out.println(termList.getCodeUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
