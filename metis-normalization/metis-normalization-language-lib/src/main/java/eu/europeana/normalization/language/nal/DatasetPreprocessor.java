package eu.europeana.normalization.language.nal;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import eu.europeana.normalization.language.LanguagesVocabulary;
//import research.JsonUtil;

public class DatasetPreprocessor {
    //TODO this one does not compile
	/**
	HashMap<String, HashSet<String>> partialWordMatches=new HashMap<>();
	
	public DatasetPreprocessor() {
	}
	
	public void addCase(String lbl, Integer cnt, List<String> normalizeds) {
		//remove all word matches from label
		lbl=lbl.toLowerCase();
		for(String wrd: normalizeds) {
			wrd=wrd.toLowerCase();
		}
	}
	
    public static void main(String[] args) throws Exception {
    	LanguagesVocabulary targetVocab = LanguagesVocabulary.ISO_639_3;
        EuropeanLanguagesNal europaEuLanguagesNal = new EuropeanLanguagesNal();
        europaEuLanguagesNal.setTargetVocabulary(targetVocab);
        europaEuLanguagesNal.initNormalizedIndex();
		LanguageMatcher normalizer = new LanguageMatcher(europaEuLanguagesNal);
        normalizer.printStats();

        DatasetPreprocessor preprocessor=new DatasetPreprocessor();
        
        
        try {
            Map<String, Object> map = JsonUtil.readJsonMap(new File(
//                    "src/research/europeana_language_facet_2015.json"),
            		"src/research/europeana_language_facet_2016.json"));

            List<Map<String, Object>> facets = (List<Map<String, Object>>)map.get("facets");
            List<Map<String, Object>> labels = (List<Map<String, Object>>)facets.get(0).get(
                    "fields");
            for (Map<String, Object> label : labels) {
                String lbl = (String)label.get("label");
                lbl=lbl.trim();
                Integer cnt = (Integer)label.get("count");
                String normalized = normalizer.findIsoCodeMatch(lbl, lbl);
                if (normalized != null && normalized.equalsIgnoreCase(lbl)) {
                } else if (normalized != null) {
                } else {
                    List<String> normalizeds = normalizer.findLabelMatches(lbl);
                    if (!normalizeds.isEmpty()) {
                    } else {
                        normalizeds = normalizer.findLabelAllWordMatches(lbl);
                        if (!normalizeds.isEmpty()) {
                        }else {                    	
                            normalizeds = normalizer.findLabelWordMatches(lbl);
                            if (!normalizeds.isEmpty()) {
                            	preprocessor.addCase(lbl, cnt, normalizeds);
                            }
                        }
                    }
                }
            }

        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        }
    }
*/
	
}
