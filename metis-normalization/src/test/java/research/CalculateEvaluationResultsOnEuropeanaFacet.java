package research;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import eu.europeana.normalization.languages.LanguageMatch;
import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.languages.Languages;
import eu.europeana.normalization.languages.LanguagesVocabulary;
import eu.europeana.normalization.settings.AmbiguityHandling;
import eu.europeana.normalization.languages.LanguageMatch.Type;
import research.evaluation.EvaluationStats;
import research.evaluation.ValidatedCases;

public class CalculateEvaluationResultsOnEuropeanaFacet {


  public static void main(String[] args) throws Throwable {
    LanguagesVocabulary targetVocab = LanguagesVocabulary.ISO_639_1;
//		LanguagesVocabulary targetVocab = LanguagesVocabulary.ISO_639_3;
    Languages europaEuLanguagesNal = Languages.getLanguages();
    europaEuLanguagesNal.setTargetVocabulary(targetVocab);
    europaEuLanguagesNal.initNormalizedIndex();
    LanguageMatcher normalizer =
        new LanguageMatcher(europaEuLanguagesNal, 4, AmbiguityHandling.NO_MATCH);

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
        validation.validateNoMatch(lbl, cnt);
      } else if (justCodeMatches && justOneMatch) {
        final LanguageMatch match = matches.get(0);
        if (match.getInput().equals(match.getMatch())) {
          validation.addAlreadyNormalized(cnt);
        } else {
          validation.addCodeMatch(cnt);
        }
      } else if (justLabelMatches && justOneMatch) {
        validation.validateLabelMatch(lbl, matchedLanguages, cnt);
      } else if (justCodeMatches || justLabelMatches) {
        validation.validateLabelWordAllMatch(lbl, matchedLanguages, cnt);
      } else if (allMatchesSucceeded) {
        validation.validateLabelWordMatch(lbl, matchedLanguages, cnt);
      }
    }
    
    System.out.println(evalStats);
    System.out.println();
    System.out.println(evalStats.toCsv());
  }
}
