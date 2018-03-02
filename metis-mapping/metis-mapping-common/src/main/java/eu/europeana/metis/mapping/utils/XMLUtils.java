package eu.europeana.metis.mapping.utils;

import eu.europeana.metis.mapping.statistics.Statistics;
import eu.europeana.metis.mapping.statistics.StatisticsValue;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

/**
 * XMLUtils
 */
public class XMLUtils {
    public static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";


    /**
     * Compute the common root xpath for all provided xpaths
     *
     * @param xpaths list of xpaths
     * @return common root of provided list
     */
    public static String commonRoot(Collection<String> xpaths) {

        String commonPath = "";
        String[][] folders = new String[xpaths.size()][];

        int c = 0;
        for (String xpath : xpaths) {
            folders[c++] = xpath.split("/"); //split on file separator
        }

        for (int j = 0; j < folders[0].length; j++) {
            String thisFolder = folders[0][j]; //grab the next folder name in the first path
            boolean allMatched = true; //assume all have matched in case there are no more paths
            for (int i = 1; i < folders.length && allMatched; i++) { //look at the other paths
                if (folders[i].length < j) { //if there is no folder here
                    allMatched = false; //no match
                    break; //stop looking because we've gone as far as we can
                }
                //otherwise
                allMatched &= folders[i][j].equals(thisFolder); //check if it matched
            }
            if (allMatched) { //if they all matched this folder name
                commonPath += thisFolder + "/"; //add it to the answer
            } else {//otherwise
                break;//stop looking
            }
        }
        return commonPath;
    }

    /**
     * Analyze a record and compute the statistics for the dataset it belongs to
     * @param record The record to analyze in XML
     * @param map The map of statistics already gathered during analysis of previous records of the dataset
     * @throws XMLStreamException
     */
    public static void analyzeRecord(String dataset,String record, Map<String, Statistics> map) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(record.getBytes()));
        String parent = "";
        while (reader.hasNext()) {
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    parent = parent + "/" + reader.getPrefix() + ":" + reader.getName().getLocalPart();
                    if (reader.getAttributeCount() > 0) {
                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            String attrPrefix = reader.getAttributePrefix(i);
                            String attrName = reader.getAttributeLocalName(i);
                            Statistics stats;
                            if (map.containsKey(parent + "/@" + attrPrefix + ":" + attrName)) {
                                stats = map.get(parent + "/@" + attrPrefix + ":" + attrName);
                            } else {
                                stats = new Statistics();
                                stats.setDatasetId(dataset);
                                stats.setId(new ObjectId());
                            }

                            stats.setXpath(parent + "/@" + attrPrefix + ":" + attrName);
                            List<StatisticsValue> values = stats.getValues();
                            if (values == null) {
                                values = new ArrayList<>();
                            }
                            boolean isContained = false;
                            for (StatisticsValue value : values) {

                                if (StringUtils.equals(value.getValue(), reader.getAttributeValue(i))) {
                                    isContained = true;
                                    value.setOccurence(value.getOccurence() + 1);
                                    break;
                                }
                            }
                            if (!isContained) {
                                StatisticsValue value = new StatisticsValue();
                                value.setOccurence(1);
                                value.setValue(reader.getAttributeValue(i));
                                value.setId(new ObjectId());
                                values.add(value);
                                stats.setValues(values);
                                map.put(parent + "/@" + attrPrefix + ":" + attrName, stats);
                            }
                        }
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    Statistics stats;
                    if (map.containsKey(parent)) {
                        stats = map.get(parent);
                    } else {
                        stats = new Statistics();
                        stats.setDatasetId(dataset);
                        stats.setId(new ObjectId());
                    }
                    stats.setXpath(parent);
                    List<StatisticsValue> values = stats.getValues();
                    if (values == null) {
                        values = new ArrayList<>();
                    }
                    boolean isContained = false;
                    for (StatisticsValue value : values) {

                        if (StringUtils.equals(value.getValue(), reader.getText())) {
                            isContained = true;
                            value.setOccurence(value.getOccurence() + 1);
                            break;
                        }
                    }
                    if (!isContained) {
                        StatisticsValue value = new StatisticsValue();
                        value.setOccurence(1);
                        value.setValue(reader.getText());
                        value.setId(new ObjectId());
                        values.add(value);
                        stats.setValues(values);
                        map.put(parent, stats);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    parent = StringUtils.substringBeforeLast(parent, "/");
                    break;
            }
            reader.next();
        }
    }
}
