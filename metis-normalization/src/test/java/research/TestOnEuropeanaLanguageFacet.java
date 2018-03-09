package research;

/* TestOnEuropeanaLanguageFacet.java - created on 06/05/2016, Copyright (c) 2011 The European Library, all rights reserved */

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import eu.europeana.normalization.common.language.LanguagesVocabulary;
import eu.europeana.normalization.common.language.nal.EuropeanLanguagesNal;
import eu.europeana.normalization.common.language.nal.LanguageMatcher;
import java.io.File;
import java.util.List;
import java.util.Map;

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

    LanguagesVocabulary targetVocab = LanguagesVocabulary.ISO_639_3;
    EuropeanLanguagesNal europaEuLanguagesNal = new EuropeanLanguagesNal();
    europaEuLanguagesNal.setTargetVocabulary(targetVocab);
    europaEuLanguagesNal.initNormalizedIndex();
    LanguageMatcher normalizer = new LanguageMatcher(europaEuLanguagesNal);

    CsvExporter exporter = new CsvExporter(new File("target"), europaEuLanguagesNal);
    try {
      Map<String, Object> map = JsonUtil.readJsonMap(new File(
//                    "src/research/europeana_language_facet_2015.json"),
          "src/research/europeana_language_facet_2016.json"));

      int okCnt = 0;
      int normalizedFromCodeCnt = 0;
      int normalizedCnt = 0;
      int normalizedWordCnt = 0;
      int normalizedWordAllCnt = 0;
      int noMatchCnt = 0;

      List<Map<String, Object>> facets = (List<Map<String, Object>>) map.get("facets");
      List<Map<String, Object>> labels = (List<Map<String, Object>>) facets.get(0).get(
          "fields");
      for (Map<String, Object> label : labels) {
        String lbl = (String) label.get("label");
        lbl = lbl.trim();
        Integer cnt = (Integer) label.get("count");
        String normalized = normalizer.findIsoCodeMatch(lbl, lbl);
        if (normalized != null && normalized.equalsIgnoreCase(lbl)) {
          System.out.println("Already normal: " + lbl);
          okCnt += cnt;
        } else if (normalized != null) {
          System.out.println(" Normalized " + lbl + " ---> " + normalized);
          exporter.exportCodeMatch(lbl, normalized);
          normalizedFromCodeCnt += cnt;
        } else {
          List<String> normalizeds = normalizer.findLabelMatches(lbl);
          if (!normalizeds.isEmpty()) {
            System.out.println(" Normalized " + lbl + " ---> " + normalizeds.get(0));
            normalizedCnt += cnt;
            exporter.exportLabelMatch(lbl, normalizeds);
          } else {
            normalizeds = normalizer.findLabelAllWordMatches(lbl);
            if (!normalizeds.isEmpty()) {
              System.out.println(" Normalized " + lbl + " ---> " + normalizeds);
              normalizedWordAllCnt += cnt;
              exporter.exportLabelWordAllMatch(lbl, normalizeds);
            } else {
              normalizeds = normalizer.findLabelWordMatches(lbl);
              if (!normalizeds.isEmpty()) {
                System.out.println(" Normalized " + lbl + " ---> " + normalizeds);
                normalizedWordCnt += cnt;
                exporter.exportLabelWordMatch(lbl, normalizeds);
              } else {
                System.out.println("not found " + lbl);
                noMatchCnt += cnt;
                exporter.exportNoMatch(lbl);
              }
            }
          }
        }
      }

      System.out.println("OK " + okCnt);
      System.out.println("normalizedFromCodeCnt " + normalizedFromCodeCnt);
      System.out.println("normalizedCnt " + normalizedCnt);
      System.out.println("normalizedWordAllCnt " + normalizedWordAllCnt);
      System.out.println("normalizedWordCnt " + normalizedWordCnt);
      System.out.println("no match " + noMatchCnt);

      //open dclang fields file,
//            for each rec
//               see if it is in the match cases
//               if so, add as example the uri, if max examples not reached
//               
//            expor final  result to a csv

      exporter.gatherCases(new File("C:\\Users\\nfrei\\Data\\ore_Proxy.dc_language.csv.gz"));
      exporter.exportEvaluationCsv(new File("target"));
      exporter.close();
    } catch (JsonGenerationException | JsonMappingException e) {
      e.printStackTrace();
    }
  }
}
