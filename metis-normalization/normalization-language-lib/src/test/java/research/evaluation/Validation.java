package research.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

import eu.europeana.normalization.language.LanguagesVocabulary;
import eu.europeana.normalization.language.nal.EuropeanLanguagesNal;
import eu.europeana.normalization.language.nal.LanguageMatcher;
import eu.europeana.normalization.language.nal.NalLanguage;

public class Validation {
	String value;
	List<NalLanguage> normalizedValues;
	boolean invalidCase;
	
	public Validation(CSVRecord rec, EuropeanLanguagesNal nal) {
		value=rec.get(0);
		String validation = rec.get(4);
		invalidCase=!validation.equals("n") && !validation.equals("y");
		
		if(validation.equals("n"))
			System.out.println("Was incorrec case: "+value);
		
		if(!invalidCase) {
			normalizedValues=new ArrayList<>(2);
			if(validation.equals("y")) {
				for(int i=1; i<=3; i++) {
					String v = rec.get(i).trim();
					if(!v.isEmpty()) {
						v=v.substring(0, v.indexOf('('));
						normalizedValues.add(nal.lookupIsoCode(v));
					} else if (i==1){
						throw new RuntimeException("empty result: "+value);
					} else
						break;
				}
			} else if(validation.equals("n")) {
				for(int i=5; i<=7; i++) {
					String v = rec.get(i).trim();
					if(!v.isEmpty()){
						v=v.substring(0, v.indexOf('('));
						normalizedValues.add(nal.lookupIsoCode(v));
					} else if (i==5){
						throw new RuntimeException("uncorrected wrong case: "+value);
					} else
						break;
				}				
			}
		}
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<NalLanguage> getNormalizedValues() {
		return normalizedValues;
	}

	public void setNormalizedValues(List<NalLanguage> normalizedValues) {
		this.normalizedValues = normalizedValues;
	}

	public boolean isInvalidCase(LanguagesVocabulary languagesVocabulary) {
		if (invalidCase)
			return true;
		for(NalLanguage l: normalizedValues) {
			if (l.getNormalizedLanguageId(languagesVocabulary) == null)
				return true;
		}
		return false;
	}

	public void setInvalidCase(boolean invalidCase) {
		this.invalidCase = invalidCase;
	}
	
	
	
}
