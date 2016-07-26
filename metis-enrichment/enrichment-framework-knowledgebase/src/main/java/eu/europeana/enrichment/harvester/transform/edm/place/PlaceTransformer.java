package eu.europeana.enrichment.harvester.transform.edm.place;

import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.enrichment.harvester.transform.XslTransformer;
import org.apache.commons.lang.StringUtils;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by ymamakis on 3/17/16.
 */
public class PlaceTransformer implements XslTransformer<PlaceImpl> {

    @Override
    public PlaceImpl transform(String xsltPath, String resourceUri, Source doc) {
        StreamSource transformDoc = new StreamSource(new File(xsltPath));

        try {
            Transformer transformer = TransformerFactory
                    .newInstance().newTransformer(transformDoc);
            StreamResult out = new StreamResult(new StringWriter());
            transformer.setParameter("rdf_about", resourceUri);
            transformer.transform(doc, out);

            return normalize(PlaceTemplate.getInstance().transform(out.getWriter().toString(), resourceUri));
        } catch (TransformerFactoryConfigurationError | TransformerException e) {
            e.printStackTrace();

        }

        return null;
    }

    @Override
    public PlaceImpl normalize(PlaceImpl place) {

        String about = place.getAbout();
        if(place.getIsPartOf()!=null){
            Map<String,List<String>> map=place.getIsPartOf();
            Map<String,List<String>> newMap = new HashMap<>();
            for(Map.Entry<String,List<String>> entry:map.entrySet()){
                List<String> values = new ArrayList<>();
                for(String str:entry.getValue()){
                    if (!StringUtils.equals(str,about)){
                        values.add(str);
                    }
                }
                newMap.put(entry.getKey(),values);
            }

                place.setIsPartOf(newMap);

        }
        return place;
    }
}
