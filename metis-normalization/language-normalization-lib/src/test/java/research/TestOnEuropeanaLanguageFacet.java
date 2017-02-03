package research;

/* TestOnEuropeanaLanguageFacet.java - created on 06/05/2016, Copyright (c) 2011 The European Library, all rights reserved */

import java.io.File;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.normalization.language.TargetLanguagesVocabulary;
import eu.europeana.normalization.language.nal.EuropeanLanguagesNal;
import eu.europeana.normalization.language.nal.LanguageMatcher;

/**
 * A test that was executed to explore and analyse the dc:language data in europeana. It uses an
 * output of the language facet from Europeana's API as source data, and applies several approaches
 * for normalizing values
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 06/05/2016
 */
public class TestOnEuropeanaLanguageFacet {

    public static void main(String[] args) throws Exception {

        EuropeanLanguagesNal europaEuLanguagesNal = new EuropeanLanguagesNal();
        LanguageMatcher normalizer = new LanguageMatcher(europaEuLanguagesNal,
                TargetLanguagesVocabulary.ISO_639_1);
        normalizer.printStats();

        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String, Object> map = mapper.readValue(new File(
                    "src/research/europeana_language_facet.json"),
                    new TypeReference<Map<String, Object>>() {
                    });

            int okCnt = 0;
            int normalizedFromCodeCnt = 0;
            int normalizedCnt = 0;
            int normalizedWordCnt = 0;
            int noMatchCnt = 0;
            int strangeValsCnt = 0;

            List<Map<String, Object>> facets = (List<Map<String, Object>>)map.get("facets");
            List<Map<String, Object>> labels = (List<Map<String, Object>>)facets.get(0).get(
                    "fields");
            for (Map<String, Object> label : labels) {
                String lbl = (String)label.get("label");
                Integer cnt = (Integer)label.get("count");
                String normalized = normalizer.findIsoCodeMatch(lbl);
                if (normalized != null && normalized.equalsIgnoreCase(lbl)) {
                    System.out.println("Already normal: " + lbl);
                    okCnt += cnt;
                } else if (normalized != null) {
                    System.out.println(" Normalized " + lbl + " ---> " + normalized);
                    normalizedFromCodeCnt += cnt;
                } else {
                    List<String> normalizeds = normalizer.findLabelMatches(lbl);
                    if (!normalizeds.isEmpty()) {
                        System.out.println(" Normalized " + lbl + " ---> " + normalizeds.get(0));
                        normalizedCnt += cnt;
                    } else {
                        if (!lbl.endsWith("[Metadata]") && !lbl.endsWith("[Resource]")) {
                            normalizeds = normalizer.findLabelWordMatches(lbl);
                            if (!normalizeds.isEmpty()) {
                                System.out.println(" Normalized " + lbl + " ---> " + normalizeds);
                                normalizedWordCnt += cnt;
                            } else {
                                if (!lbl.endsWith("[Metadata]") && !lbl.endsWith("[Resource]")) {
                                    System.out.println("not found " + lbl);
                                    noMatchCnt += cnt;
                                }
                            }
                        } else
                            strangeValsCnt += cnt;
                    }
                }
            }

            System.out.println("OK " + okCnt);
            System.out.println("normalizedFromCodeCnt " + normalizedFromCodeCnt);
            System.out.println("normalizedCnt " + normalizedCnt);
            System.out.println("normalizedWordCnt " + normalizedWordCnt);
            System.out.println("no match " + noMatchCnt);
            System.out.println("strange vals " + strangeValsCnt);

        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        }
    }
}
