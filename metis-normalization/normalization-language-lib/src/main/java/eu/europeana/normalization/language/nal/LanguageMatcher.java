/* LanguageNormalizer.java - created on 16/03/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization.language.nal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.europeana.normalization.language.LanguagesVocabulary;
import eu.europeana.normalization.util.MapOfLists;
import eu.europeana.normalization.util.nlp.IndexUtilUnicode;

/**
 * Provides the matching algorithms for matching dc:language values with codes and labels in the Languages NAL
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/03/2016
 */
public class LanguageMatcher {
	
	public enum AmbiguityHandling { NO_MATCH, CHOOSE_FIRST, MATCH_ON_COREFERENCE }
	
    private static java.util.logging.Logger log               = java.util.logging.Logger.getLogger(LanguageMatcher.class.getName());

    protected static final Pattern LOCALE_CODE_PATTERN=Pattern.compile("\\s*(\\p{Alpha}\\p{Alpha})-\\p{Alpha}\\p{Alpha}\\s*");
    
    private Map<String, String>             targetIsoCodes          = new HashMap<String, String>();
    private Map<String, String>             isoCodes          = new HashMap<String, String>();
    private Map<String, String>             unambiguousLabels = new HashMap<String, String>();
    private MapOfLists<String, String>      ambiguousLabels   = new MapOfLists<String, String>();

    EuropeanLanguagesNal                    matchVocab;
    
    int minimumLabelLength=4;

    AmbiguityHandling ambiguityHandling=AmbiguityHandling.NO_MATCH;
    
    
    /**
     * Creates a new instance of this class.
     * 
     * @param matchingVocab
     */
    public LanguageMatcher(EuropeanLanguagesNal matchingVocab) {
        super();
        this.matchVocab = matchingVocab;
        initIndex();
    }

    /**
     * 
     */
    protected void initIndex() {
        for (NalLanguage l : matchVocab.getLanguages()) {
            index(l);
        }
    }

    /**
     * @param l
     */
    protected void index(NalLanguage l) {
        String norm = l.getNormalizedLanguageId(matchVocab.getTargetVocabulary());
        if(norm==null)
        	return;

// Set<String> labelsDedup=l.getAllLabelsAndCodes();
        Set<String> labelsDedup = l.getAllLabels();
        for (String label : labelsDedup) {
        	if(label.length()<minimumLabelLength)
        		continue;
            String labelNorm = normalizeLabelForIndex(label);
            if (ambiguousLabels.containsKey(labelNorm))
                ambiguousLabels.putIfNotExists(labelNorm, norm);
            else if (unambiguousLabels.containsKey(labelNorm)) {
            	if(!unambiguousLabels.get(labelNorm).equals(norm)) {
	                ambiguousLabels.put(labelNorm, norm);
	                ambiguousLabels.put(labelNorm, unambiguousLabels.remove(labelNorm));
            	}
            } else {
                unambiguousLabels.put(labelNorm, norm);
            }
        }
        if (l.getIso6391() != null) {
            if (isoCodes.containsKey(l.getIso6391()) && !isoCodes.get(l.getIso6391()).equals(norm))
                throw new RuntimeException("ambig iso code!!! : " + l.getIso6391());
            isoCodes.put(l.getIso6391(), norm);
            if(matchVocab.getTargetVocabulary()==LanguagesVocabulary.ISO_639_1)
            	targetIsoCodes.put(l.getIso6391(), norm);
        }
        if (l.getIso6392b() != null) {
            if (isoCodes.containsKey(l.getIso6392b()) &&
                !isoCodes.get(l.getIso6392b()).equals(norm))
                throw new RuntimeException("ambig iso code!!! : " + l.getIso6392b());
            isoCodes.put(l.getIso6392b(), norm);
            if(matchVocab.getTargetVocabulary()==LanguagesVocabulary.ISO_639_2b)
            	targetIsoCodes.put(l.getIso6392b(), norm);
        }
        if (l.getIso6392t() != null) {
            if (isoCodes.containsKey(l.getIso6392t()) &&
                !isoCodes.get(l.getIso6392t()).equals(norm))
                throw new RuntimeException("ambig iso code!!! : " + l.getIso6392t());
            isoCodes.put(l.getIso6392t(), norm);
            if(matchVocab.getTargetVocabulary()==LanguagesVocabulary.ISO_639_2t)
            	targetIsoCodes.put(l.getIso6392t(), norm);
        }
        if (l.getIso6393() != null) {
            if (isoCodes.containsKey(l.getIso6393()) && !isoCodes.get(l.getIso6393()).equals(norm))
                throw new RuntimeException("ambig iso code!!! : " + l.getIso6393());
            isoCodes.put(l.getIso6393(), norm);
            if(matchVocab.getTargetVocabulary()==LanguagesVocabulary.ISO_639_3)
            	targetIsoCodes.put(l.getIso6393(), norm);
        }
    }



    public void printStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("isoCodes: " + isoCodes.size()).append("\n");
        sb.append("unambiguousLabels: " + unambiguousLabels.size()).append("\n");
        sb.append("ambiguousLabels: " + ambiguousLabels.size()).append("\n");
        sb.append(ambiguousLabels.toString()).append("\n");
        System.out.println(sb.toString());
    }

    public List<String> findLabelMatches(String value) throws AmbiguousLabelMatchException {
        String valueNorm = normalizeLabelForIndex(value);
        String match = unambiguousLabels.get(valueNorm);
        if (match == null) {
            if (ambiguousLabels.size() == 0)
                return Collections.EMPTY_LIST;
            else {
                List<String> matches = ambiguousLabels.get(valueNorm);
                if (matches == null) return Collections.EMPTY_LIST;
                throw new AmbiguousLabelMatchException(matches);
//                return matches;
            }
        }
        ArrayList<String> ret = new ArrayList<String>(1);
        ret.add(match);
        return ret;
    }

// public List<String> findLabelSubstringMatches(String value) {
// String valueNorm = normalizeLabel(value);
// }

    public String findIsoCodeMatch(String valueP, String nonNormalizedValue) {
        String value = valueP.trim();
        if (value.length() > 3 || value.length() < 2) {
        	if(nonNormalizedValue!=null) {
	        	Matcher matcher = LOCALE_CODE_PATTERN.matcher(value);
				if (matcher.matches()) {
	        		return isoCodes.get(matcher.group(1));
	        	}
        	} 
    		return null;
        }
        String valueNorm = value.toLowerCase();
        return isoCodes.get(valueNorm);
    }

    public String findTargetIsoCodeMatch(String valueP, String nonNormalizedValue) {
    	String value = valueP.trim();
    	if (value.length() > 3 || value.length() < 2) {
    		if(nonNormalizedValue!=null) {
    			Matcher matcher = LOCALE_CODE_PATTERN.matcher(value);
    			if (matcher.matches()) {
    				return targetIsoCodes.get(matcher.group(1));
    			}
    		} 
    		return null;
    	}
    	String valueNorm = value.toLowerCase();
    	return targetIsoCodes.get(valueNorm);
    }
    
    /**
     * @param value
     * @return
     */
    public String normalizeLabelForIndex(String label) {
        return IndexUtilUnicode.encode(label);
    }

    public List<String> findLabelAllWordMatches(String lbl) {
        String lblEnc = normalizeLabelForIndex(lbl);
        String[] words = lblEnc.split("\\s+");

        HashSet<String> foundMatchesLabels = new HashSet<String>(10);
        HashSet<String> foundMatchesCodes = new HashSet<String>(10);
        for (int j = 0; j < words.length; j++) {
            String wrd = words[j];
            if (j == 0 || j == words.length - 1) {
                String findIsoCodeMatch = findIsoCodeMatch(wrd, null);
                if (findIsoCodeMatch != null) { 
                	foundMatchesCodes.add(findIsoCodeMatch);
                	continue;
                }
            }
// if (wrd.length()>2)

            List<String> labelMatches;
			try {
				labelMatches = findLabelMatches(wrd);
	            if (!labelMatches.isEmpty()) 
	            	foundMatchesLabels.add(labelMatches.get(0));
            	return Collections.emptyList();
			} catch (AmbiguousLabelMatchException e) {
            	if(ambiguityHandling==AmbiguityHandling.NO_MATCH)
            		return Collections.emptyList();
            	else if(ambiguityHandling==AmbiguityHandling.MATCH_ON_COREFERENCE)
            		return Collections.emptyList();//TODO
            	else if(ambiguityHandling==AmbiguityHandling.CHOOSE_FIRST)
        			foundMatchesLabels.add(e.getAmbigouosMatches().get(0));
            	else
            		throw new RuntimeException("not implemented: "+ambiguityHandling);
			}
        }
        if (foundMatchesCodes.isEmpty() && !foundMatchesLabels.isEmpty())
        	return new ArrayList<String>(foundMatchesLabels);
        if (!foundMatchesCodes.isEmpty() && foundMatchesLabels.isEmpty())
        	return new ArrayList<String>(foundMatchesCodes);

                return Collections.emptyList();//only considers matches when only labels are detected, ore only codes are detected.
    }
    
    
    
    /**
     * @param lbl
     * @return
     */
    public List<String> findLabelWordMatches(String lbl) {
        String lblEnc = normalizeLabelForIndex(lbl);
        String[] words = lblEnc.split("\\s+");

        HashSet<String> foundMatches = new HashSet<String>(10);
        for (int j = 0; j < words.length; j++) {
            String wrd = words[j];
            if (j == 0 || j == words.length - 1) {
                String findIsoCodeMatch = findIsoCodeMatch(wrd, null);
                if (findIsoCodeMatch != null) foundMatches.add(findIsoCodeMatch);
            }

// if (wrd.length()>2)
            List<String> labelMatches;
			try {
				labelMatches = findLabelMatches(wrd);
	            if (!labelMatches.isEmpty()) 
	            	foundMatches.add(labelMatches.get(0));
			} catch (AmbiguousLabelMatchException e) {
				if(ambiguityHandling==AmbiguityHandling.NO_MATCH) {
            	} else if(ambiguityHandling==AmbiguityHandling.MATCH_ON_COREFERENCE)
            		throw new RuntimeException("not implemented: "+ambiguityHandling);
            	else if(ambiguityHandling==AmbiguityHandling.CHOOSE_FIRST)
        			foundMatches.add(e.getAmbigouosMatches().get(0));
            	else
            		throw new RuntimeException("not implemented: "+ambiguityHandling);
			}
        }
        return new ArrayList<String>(foundMatches);
    }

	public void setMinimumLabelLength(int minimumLabelLength) {
		this.minimumLabelLength = minimumLabelLength;
	}

	public void setAmbiguityHandling(AmbiguityHandling ambiguityHandling) {
		this.ambiguityHandling = ambiguityHandling;
	}

}
