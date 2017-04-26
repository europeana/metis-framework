/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.annocultor.converters.geonames;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import eu.annocultor.context.Concepts;
import eu.annocultor.context.Concepts.SKOS;
import eu.annocultor.context.Namespaces;
import eu.annocultor.tagger.terms.Term;
import eu.annocultor.tagger.terms.TermList;
import eu.annocultor.tagger.vocabularies.VocabularyOfTerms;
import eu.annocultor.triple.LiteralValue;
import eu.annocultor.triple.Property;
import eu.annocultor.triple.ResourceValue;
import eu.annocultor.triple.Triple;
import eu.annocultor.utils.SesameWriter;

/**
 * Text-level parser of the Geonames file. 
 * In this file 6.5 mln small RDF files, one per feature (this is how they call their geo-objects)
 * are merged in a single txt file, separated with the id of the feature 
 * described in each small file.
 * 
 * @author Borys Omelayenko
 *
 */
public class GeonamesCsvToRdf 
{

    private static final String NS_GEONAMES_INSTANCES = "http://sws.geonames.org/";
    private static final String NS_GEONAMES_ONTOLOGY = "http://www.geonames.org/ontology#";
    private static final String NS_EUROPEANA_SCHEMA = "http://www.europeana.eu/resolve/ontology/";
    private static final String NS_WGS_SCHEMA = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    
    public static Properties countryToContinent = new Properties();

    public static int geonameid = 0;         // integer id of record in geonames database
    public static int name = 1;              // name of geographical point (utf8) varchar(200)
    public static int asciiname = 2;         // name of geographical point in plain ascii characters, varchar(200)
    public static int alternatenames = 3;    // alternatenames, comma separated varchar(5000)
    public static int latitude = 4;          // latitude in decimal degrees (wgs84)
    public static int longitude = 5;         // longitude in decimal degrees (wgs84)
    public static int featureClass = 6;     // see http////www.geonames.org/export/codes.html, char(1)
    public static int featureCode = 7;      // see http////www.geonames.org/export/codes.html, varchar(10)
    public static int countryCode = 8;      // ISO-3166 2-letter country code, 2 characters
    public static int cc2 = 9;               // alternate country codes, comma separated, ISO-3166 2-letter country code, 60 characters
    public static int admin1code = 10;       // fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
    public static int admin2code = 11;       // code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80) 
    public static int admin3code = 12;       // code for third level administrative division, varchar(20)
    public static int admin4code = 13;       // code for fourth level administrative division, varchar(20)
    public static int population = 14;        // bigint (4 byte int) 
    public static int elevation = 15;         // in meters, integer
    public static int gtopo30 = 16;           // average elevation of 30'x30' (ca 900mx900m) area in meters, integer
    public static int timezone = 17;          // the timezone id (see file timeZone.txt)
    public static int modificationDate = 18;  // date of last modification in yyyy-MM-dd format

    public boolean includeRecordInConversion(String featureCode, String population) {
        final long MIN_POPULATION = 10000;
        final String P_PPL = "P.PPL";
        final String allowedFeatureCodePrefixes[] = {
                "A", 
                P_PPL,
                //"P.PPLC", 
                //"P.PPLA",
                "S.CSTL",
                // there are too many churches in US
                // "S.CH",
                "S.ANS",
                "S.MNMT",
                "S.LIBR",
                "S.HSTS",
                "S.OPRA",
                "S.AMTH",
                "S.TMPL",
                "T.ISL"
        };
        //, "P", "S"};
        if (featureCode.isEmpty()) {
            return false;
        }
        for (String prefix : allowedFeatureCodePrefixes) {
            if (featureCode.startsWith(prefix)) {
                if (prefix.equals(P_PPL)) {
                    return (! population.isEmpty() 
                            && StringUtils.isNumeric(population) 
                            && Integer.parseInt(population) > MIN_POPULATION);
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public void write(String country, Triple triple, boolean isDescriptionOfCountry) throws Exception {
        String continent = countryToContinent.getProperty(country);
        if (isDescriptionOfCountry) {
            if (allCountries == null) {
                Namespaces namespaces = new Namespaces();
                namespaces.addNamespace(NS_EUROPEANA_SCHEMA, "europeana");
                allCountries = SesameWriter.createRDFXMLWriter(
                        new File (root, continent + "-all-countries.rdf"), 
                        namespaces, 
                        continent + "_" + country, 
                        "Selected fields pulled from Geonames about " + country + " done on " + new Date(),
                        1024,
                        1024,
                        "This dataset was selected and converted from Geonames http://download.geonames.org/export/dump/readme.txt",
                        "that is licensed under a Creative Commons Attribution 3.0 License,",
                        "see http://creativecommons.org/licenses/by/3.0/",
                        "The Data is provided \"as is\" without warranty or any representation of accuracy, timeliness or completeness."
                );
                allCountries.startRDF();
            }
            allCountries.handleTriple(triple);
        } 
        SesameWriter writer = files.get(country);
        if (writer == null) {
            Namespaces namespaces = new Namespaces();
            namespaces.addNamespace(NS_EUROPEANA_SCHEMA, "europeana");
            writer = SesameWriter.createRDFXMLWriter(
                    new File (root, continent + "/" + country + ".rdf"), 
                    namespaces, 
                    continent + "_" + country, 
                    "Selected fields pulled from Geonames about " + country + " done on " + new Date(),
                    1024,
                    1024,
                    "This dataset was selected and converted from Geonames http://download.geonames.org/export/dump/readme.txt",
                    "that is licensed under a Creative Commons Attribution 3.0 License,",
                    "see http://creativecommons.org/licenses/by/3.0/",
                    "The Data is provided \"as is\" without warranty or any representation of accuracy, timeliness or completeness.",
                    "Technical note: isPartOf may refer to concepts that are not described in these files."
            );
            files.put(country, writer);
            writer.startRDF();
        }
        writer.handleTriple(triple);
    }

    String continentToConvert = "EU";

    File root = new File("input_source");

    MultiValueMap altLabels = new MultiValueMap();
    MultiValueMap links = new MultiValueMap();
    MultiValueMap broader = new MultiValueMap();

    Map<String,SesameWriter> files = new HashMap<String, SesameWriter>();
    SesameWriter allCountries = null;

    void labels() throws Exception {
        System.out.println("Loading alt names");
        //Map<String, List<XmlValue>> altLabels = new HashMap<String, List<XmlValue>();
        LineIterator it = FileUtils.lineIterator(new File(root, "alternateNames.txt"), "UTF-8");
        try {
            while (it.hasNext()) {
                String text = it.nextLine();

                String[] fields = text.split("\t");
                //					if (fields.length != 4) {
                //						throw new Exception("Field names mismatch on " + text);
                //					}
                String uri = fields[1];
                String lang = fields[2];
                String label = fields[3];
                if ("link".equals(lang)) {
                    // wikipedia links
                    links.put(uri, label);
                } else {
                    if (lang.length() < 3) {
                        altLabels.put(uri, new LiteralValue(label, lang.isEmpty() ? null : lang));
                    } else {
                        // mostly postcodes
                        //System.out.println("LANG: " + lang + " on " + label);
                    }
                }
            }
        } finally {
            LineIterator.closeQuietly(it);
        }
    }

    Map<String, String> countryCodeToGeonamesCode = new HashMap<String, String>();
    
    void collectCountries() throws Exception {
        String query =
        "PREFIX europeana: <http://www.europeana.eu/resolve/ontology/> " +   
        "SELECT ?code ?label " + 
        "WHERE { ?code europeana:country ?label } ";

        VocabularyOfTerms vocabulary = new VocabularyOfTerms("countries", null);
        vocabulary.loadTermsSPARQL(query, new File("cache"), new File("../vocabularies/places/countries"), "*.rdf");
    
        for (TermList terms : vocabulary.listAllByCode()) {
            for (Term term : terms) {
                countryCodeToGeonamesCode.put(term.getLabel(), term.getCode());
            }
        }
    }
    
    void collectParents() throws Exception {
        System.out.println("Loading parents");
        LineIterator it = FileUtils.lineIterator(new File(root, "hierarchy.txt"), "UTF-8");
        try {
            while (it.hasNext()) {
                String text = it.nextLine();

                String[] fields = text.split("\t");
                String parent = NS_GEONAMES_INSTANCES + fields[0] + "/";
                String child = NS_GEONAMES_INSTANCES + fields[1] + "/";
                broader.put(child, parent);
            }
        } finally {
            LineIterator.closeQuietly(it);
        }
    }

    Set<String> allParents(String uri, String country) {
        Set<String> all = new HashSet<String>();
        Collection<String> directParents = broader.getCollection(uri);
        Stack<String> toCheckForParents = new Stack<String>();
        if (directParents != null) {
            toCheckForParents.addAll(directParents);
        }
        while (!toCheckForParents.isEmpty()) {
            String parent = toCheckForParents.pop();
            if (parent != null && all.add(parent)) {
                Collection<String> parents = broader.getCollection(parent);
                if (parents != null) {
                    toCheckForParents.addAll(directParents);
                }
            }
        }
        String countryUri = countryCodeToGeonamesCode.get(country);
        if (countryUri != null) {
            all.add(countryUri);
        }
        return all;
    }

    void features() throws Exception {
        System.out.println("Parsing features");
        // load country-continent match
        countryToContinent.load((new GeonamesCsvToRdf("EU")).getClass().getResourceAsStream("/country-to-continent.properties"));

        createDirsForContinents();

        long counter = 0;
        LineIterator it = FileUtils.lineIterator(new File(root, "allCountries.txt"), "UTF-8");
        try {
            while (it.hasNext()) {
                String text = it.nextLine();

                String[] fields = text.split("\t");
                if (fields.length != 19) {
                    throw new Exception("Field names mismatch on " + text);
                }

                // progress
                counter ++;
                if (counter % 100000 == 0) {
                    System.out.print("*");
                }
                String country = fields[countryCode];
                String continent = countryToContinent.getProperty(country);
                if (continent != null && continent.startsWith(continentToConvert)) {					

                    String id = fields[geonameid];
                    String uri = NS_GEONAMES_INSTANCES + id + "/";
                    String featureCodeField = fields[featureClass] + "." + fields[featureCode];
                    String populationValue = fields[population];
                    if (includeRecordInConversion(featureCodeField, populationValue)) {

                        boolean isDescriptionOfCountry = featureCodeField.startsWith("A.PCLI");

                        if (!fields[name].isEmpty()) {
                            write(country, new Triple(uri, SKOS.LABEL_PREFERRED, new LiteralValue(fields[name]), null), isDescriptionOfCountry);
                        }
                        //				String altLabels[] = fields[alternatenames].split(",");
                        //				for (String altLabel : altLabels) {
                        //					write(country, new Triple(uri, SKOS.LABEL_ALT, new LiteralValue(altLabel), null));					
                        //				}
                        Collection<LiteralValue> altLabelCollection = altLabels.getCollection(id);
                        if (altLabelCollection != null) {
                            for (LiteralValue xmlValue : altLabelCollection) {
                                write(country, new Triple(uri, SKOS.LABEL_ALT, xmlValue, null), isDescriptionOfCountry);					
                            }
                            altLabels.remove(id);
                        }
                        Collection<String> linkCollection = links.getCollection(id);
                        if (linkCollection != null) {
                            for (String link : linkCollection) {
                                // write(country, new Triple(uri, new Property(NS_EUROPEANA_SCHEMA + "link"), new LiteralValue(link), null));					
                            }
                            linkCollection.remove(fields[geonameid]);
                        }
                        if (fields[population].length() > 1) {
                            write(country, new Triple(uri, new Property(NS_EUROPEANA_SCHEMA + "population"), new LiteralValue(fields[population]), null), isDescriptionOfCountry);					
                        }
                        if (!fields[longitude].isEmpty()) {
                            write(country, new Triple(uri, new Property(NS_WGS_SCHEMA + "long"), new LiteralValue(fields[longitude]), null), isDescriptionOfCountry);					
                        }
                        if (!fields[latitude].isEmpty()) {
                            write(country, new Triple(uri, new Property(NS_WGS_SCHEMA + "lat"), new LiteralValue(fields[latitude]), null), isDescriptionOfCountry);					
                        }
                        if (!featureCodeField.isEmpty()) {
                            write(country, new Triple(uri, new Property(NS_EUROPEANA_SCHEMA + "division"), new ResourceValue(NS_GEONAMES_ONTOLOGY + featureCodeField), null), isDescriptionOfCountry);					
                        }
                        if (!country.isEmpty()) {
                            write(country, new Triple(uri, new Property(NS_EUROPEANA_SCHEMA + "country"), new LiteralValue(country), null), isDescriptionOfCountry);					
                        }
                        // alt label as country code
                        if (featureCodeField.startsWith("A.PCL")) {
                            write(country, new Triple(uri, SKOS.LABEL_ALT, new LiteralValue(country), null), isDescriptionOfCountry);					

                        }

                        for (String broaderUri : allParents(uri, country)) {
                            write(country, new Triple(uri, Concepts.DCTEMRS.IS_PART_OF, new ResourceValue(broaderUri), null), isDescriptionOfCountry);                    
                        }
                        //                        if (!fields[admin1code].isEmpty()) {
                        //                            write(country, new Triple(uri, new Property(NS_EUROPEANA_SCHEMA + "admin1"), new LiteralValue(fields[admin1code]), null), isDescriptionOfCountry);					
                        //                        }
                        //                        if (!fields[admin2code].isEmpty()) {
                        //                            write(country, new Triple(uri, new Property(NS_EUROPEANA_SCHEMA + "admin2"), new LiteralValue(fields[admin2code]), null), isDescriptionOfCountry);					
                        //                        }
                        //                        if (!fields[admin3code].isEmpty()) {
                        //                            write(country, new Triple(uri, new Property(NS_EUROPEANA_SCHEMA + "admin3"), new LiteralValue(fields[admin3code]), null), isDescriptionOfCountry);					
                        //                        }
                        //                        if (!fields[admin4code].isEmpty()) {
                        //                            write(country, new Triple(uri, new Property(NS_EUROPEANA_SCHEMA + "admin4"), new LiteralValue(fields[admin4code]), null), isDescriptionOfCountry);					
                        //                        }

                    }
                }

            }
        } finally {
            LineIterator.closeQuietly(it);
        }
        System.out.println("Finished conversion, flushing and closing output files");
        System.out.flush();
        for (Object country : countryToContinent.keySet()) {
            SesameWriter bf = files.get(country.toString());
            if (bf != null) {
                bf.endRDF();
            }
        }
        if (allCountries != null) {
            allCountries.endRDF();
        }
    }

    private void createDirsForContinents() {
        for (Object country : countryToContinent.keySet())
        {
            String continent = countryToContinent.getProperty(country.toString());
            File dir = new File(root, continent);
            if (!dir.exists()) {
                dir.mkdir();
            }
        }
    }


    public GeonamesCsvToRdf(String continentToConvert) {
        super();
        this.continentToConvert = continentToConvert;
    }

    public static void main(String[] args) 
    throws Exception
    {		
        String continents[] = {"EU", "AS", "NA", "SA", "AF", "OC"};

        for (String continent : continents) {
            System.out.println("Converting " + continent);
            GeonamesCsvToRdf converter = new GeonamesCsvToRdf(continent);
            converter.collectCountries();
            converter.collectParents();
            converter.labels();
            converter.features();
        }
    }
}
