package research;

/* TestOnEuropeanaLanguageFacet.java - created on 06/05/2016, Copyright (c) 2011 The European Library, all rights reserved */

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import eu.europeana.normalization.languages.Languages;
import eu.europeana.normalization.languages.LanguagesVocabulary;
import eu.europeana.normalization.languages.LanguageMatch.Type;
import eu.europeana.normalization.languages.LanguageMatch;
import eu.europeana.normalization.languages.LanguageMatcher;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    Languages europaEuLanguagesNal = Languages.getLanguages();
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
        
        final List<LanguageMatch> matches = normalizer.match(lbl);
        final Set<Type> matchTypes =
            matches.stream().map(LanguageMatch::getType).distinct().collect(Collectors.toSet());
        final List<String> matchedLanguages =
            matches.stream().map(LanguageMatch::getMatch).distinct().collect(Collectors.toList());
        
        final boolean justCodeMatches = matchTypes.size() == 1 && matchTypes.contains(Type.CODE_MATCH);
        final boolean justLabelMatches = matchTypes.size() == 1 && matchTypes.contains(Type.LABEL_MATCH);
        final boolean justOneMatch = matches.size()==1;
        final boolean allMatchesSucceeded = !matchTypes.contains(Type.NO_MATCH);

        if (matches.isEmpty()) {
          System.out.println("not found " + lbl);
          noMatchCnt += cnt;
          exporter.exportNoMatch(lbl);
        } else if (justCodeMatches && justOneMatch) {
          final LanguageMatch match = matches.get(0);
          if (match.getInput().equals(match.getMatch())) {
            System.out.println("Already normal: " + lbl);
            okCnt += cnt;
          } else {
            System.out.println(" Normalized " + lbl + " ---> " + match.getMatch());
            exporter.exportCodeMatch(lbl, match.getMatch());
            normalizedFromCodeCnt += cnt;
          }
        } else if (justLabelMatches && justOneMatch) {
          System.out.println(" Normalized " + lbl + " ---> " + matchedLanguages.get(0));
          normalizedCnt += cnt;
          exporter.exportLabelMatch(lbl, matchedLanguages);
        } else if (justCodeMatches || justLabelMatches) {
          System.out.println(" Normalized " + lbl + " ---> " + matchedLanguages);
          normalizedWordAllCnt += cnt;
          exporter.exportLabelWordAllMatch(lbl, matchedLanguages);
        } else if (allMatchesSucceeded) {
          System.out.println(" Normalized " + lbl + " ---> " + matchedLanguages);
          normalizedWordCnt += cnt;
          exporter.exportLabelWordMatch(lbl, matchedLanguages);
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
