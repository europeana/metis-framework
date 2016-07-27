package eu.europeana.enrichment.harvester.geonames;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.PlaceTermList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.JacksonDBCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * Created by ymamakis on 3/17/16.
 */
public class Indexer {
    private static ObjectMapper mapper = new ObjectMapper();
    public static void main(String[] args) {

        Mongo mongo = null;
        try {
            mongo = new Mongo("172.17.0.2", 27017);
            DB db = mongo.getDB("annocultor_db");
            JacksonDBCollection pColl = JacksonDBCollection.wrap(db.getCollection("place"), MongoTerm.class, String.class);
            JacksonDBCollection <PlaceTermList,String> wrapperColl =    JacksonDBCollection.wrap(
                    db.getCollection("TermList"), PlaceTermList.class,
                    String.class);
            wrapperColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", true));
            wrapperColl.createIndex(new BasicDBObject("owlSameAs",1), new BasicDBObject("unique",false));
            pColl.createIndex(new BasicDBObject("codeUri", 1), new BasicDBObject("unique", false));
            pColl.createIndex(new BasicDBObject("label", 1), new BasicDBObject("unique", false));
            pColl.createIndex(new BasicDBObject("lang", 1), new BasicDBObject("unique", false));

            File[] filesToRead = new File("/home/ymamakis/git/tools/annocultor_solr3.5/converters/geonames/input_source/").listFiles();
            for(File file:filesToRead){
                if(file.getName().endsWith("edm")){
                    readFileToDB(wrapperColl,pColl,file);
                    System.out.println("Finished " + file.getName());
                }
                if(file.isDirectory()&& file.getName().contains("EU")){
                    File[] childrenFiles = file.listFiles();
                    for(File child:childrenFiles){
                        if(child.getName().endsWith("edm")){
                            readFileToDB(wrapperColl,pColl,child);
                            System.out.println("Finished " + file.getName());
                        }
                    }
                }
            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readFileToDB(JacksonDBCollection<PlaceTermList, String> wrapperColl, JacksonDBCollection pColl, File child) throws IOException {
        List<String> placesToPersist = IOUtils.readLines(new FileInputStream(child));
        for (String placeString:placesToPersist){
            PlaceImpl place = mapper.readValue(placeString.getBytes(),PlaceImpl.class);
            PlaceTermList placeTermList = new PlaceTermList();
            placeTermList.setRepresentation(place);
            placeTermList.setEntityType(PlaceImpl.class.getSimpleName());
            placeTermList.setCodeUri(place.getAbout());
            if(place.getIsPartOf()!=null&& place.getIsPartOf().get("def")!=null) {
                placeTermList.setParent(place.getIsPartOf().get("def").get(0));
            }
            if(place.getOwlSameAs()!=null) {
                placeTermList.setOwlSameAs(place.getOwlSameAs());
            }
            wrapperColl.save(placeTermList);
            Map<String,List<String>> termsToPersist = place.getPrefLabel();
            for(Map.Entry<String,List<String>>entry:termsToPersist.entrySet()){
                String key = entry.getKey();
                for(String value:entry.getValue()){
                    MongoTerm term = new MongoTerm();
                    term.setCodeUri(place.getAbout());
                    term.setLang(StringUtils.isNotEmpty(key)?key:"def");
                    term.setOriginalLabel(value);
                    term.setLabel(value.toLowerCase());
                    pColl.save(term);
                }
            }

        }
    }
}
