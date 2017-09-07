package research;

import eu.europeana.normalization.language.LanguagesVocabulary;
import eu.europeana.normalization.language.nal.AmbiguousLabelMatchException;
import eu.europeana.normalization.language.nal.EuropeanLanguagesNal;
import eu.europeana.normalization.language.nal.LanguageMatcher;
import java.io.File;
import java.util.List;
import java.util.Map;
import research.evaluation.EvaluationStats;
import research.evaluation.ValidatedCases;

public class CalculateEvaluationResultsOnEuropeanaFacet {


  public static void main(String[] args) throws Throwable {
    LanguagesVocabulary targetVocab = LanguagesVocabulary.ISO_639_1;
//		LanguagesVocabulary targetVocab = LanguagesVocabulary.ISO_639_3;
    EuropeanLanguagesNal europaEuLanguagesNal = new EuropeanLanguagesNal();
    europaEuLanguagesNal.setTargetVocabulary(targetVocab);
    europaEuLanguagesNal.initNormalizedIndex();
    LanguageMatcher normalizer = new LanguageMatcher(europaEuLanguagesNal);

    //		read all evaluation csvs
    ValidatedCases validation = new ValidatedCases(new File("src/research/evaluation"),
        europaEuLanguagesNal);
    EvaluationStats evalStats = new EvaluationStats();
    validation.setStats(evalStats);

//		navigate through facets

    Map<String, Object> map = JsonUtil.readJsonMap(new File(
//                "src/research/europeana_language_facet_2015.json"),
        "src/research/europeana_language_facet_2016.json"));

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> facets = (List<Map<String, Object>>) map.get("facets");
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> labels = (List<Map<String, Object>>) facets.get(0).get(
        "fields");
    for (Map<String, Object> label : labels) {
      String lbl = (String) label.get("label");
      lbl = lbl.trim();
      Integer cnt = (Integer) label.get("count");
      validation.addCount(cnt);

      String normalized = normalizer.findTargetIsoCodeMatch(lbl, lbl);
      if (normalized != null && normalized.equalsIgnoreCase(lbl)) {
//                System.out.println("Already normal: " + lbl);
        validation.addAlreadyNormalized(cnt);
//                evalStats.getNormalizationMethodStats().addAlreadyNormalized(cnt);
//                evalStats.getTargetCodesMatchesStats().addAlreadyNormalized(cnt);
      } else {
        normalized = normalizer.findIsoCodeMatch(lbl, lbl);
//            	if (normalized != null && normalized.equalsIgnoreCase(lbl)) {
//            		evalStats.addCodeMatch(cnt);
//	            } else 
        if (normalized != null) {
          validation.addCodeMatch(cnt);
        } else {
          List<String> normalizeds;
          try {
            normalizeds = normalizer.findLabelMatches(lbl);
            if (!normalizeds.isEmpty()) {
//			                    System.out.println(" Normalized " + lbl + " ---> " + normalizeds.get(0));
              validation.validateLabelMatch(lbl, normalizeds, cnt);
            } else {
              normalizeds = normalizer.findLabelAllWordMatches(lbl);
              if (!normalizeds.isEmpty()) {
//			                    	System.out.println(" Normalized " + lbl + " ---> " + normalizeds);
                validation.validateLabelWordAllMatch(lbl, normalizeds, cnt);
              } else {
                normalizeds = normalizer.findLabelWordMatches(lbl);
                if (!normalizeds.isEmpty()) {
//			                            System.out.println(" Normalized " + lbl + " ---> " + normalizeds);
                  validation.validateLabelWordMatch(lbl, normalizeds, cnt);
                } else {
//			                            System.out.println("not found " + lbl);
                  validation.validateNoMatch(lbl, cnt);
                }
              }
            }
          } catch (AmbiguousLabelMatchException e) {
            System.out
                .println(" Ambiguous (not matching): " + lbl + " ---> " + e.getAmbigouosMatches());
            validation.validateNoMatch(lbl, cnt);
          }

        }
      }
    }
    System.out.println(evalStats);
    System.out.println();
    System.out.println(evalStats.toCsv());
  }
}
