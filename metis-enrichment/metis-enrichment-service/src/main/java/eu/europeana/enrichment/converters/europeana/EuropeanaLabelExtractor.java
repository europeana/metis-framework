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
package eu.europeana.enrichment.converters.europeana;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eu.europeana.enrichment.tagger.preprocessors.LabelFilter;

/**
 * Preprocesses and filters values before they are looked up in a vocabulary
 * (followed by disambiguation). Improves recall.
 * 
 * @author Borys Omelayenko
 * 
 */
public class EuropeanaLabelExtractor extends LabelFilter {

	private boolean fullTextMode = false;
	private String pattern = ",";
	private int minLength = 2;

	public EuropeanaLabelExtractor(boolean fullTextMode) {
		this.fullTextMode = fullTextMode;
		if (fullTextMode) {
			pattern = "[ \\.,;:\\]\\[\\(\\)\"]";
			minLength = 4;
		}
	}

	@Override
	public List<String> extract(List<String> labels) throws Exception {
		List<String> extracted = new ArrayList<String>();
		for (String label : labels) {
			if (label != null) {
				if (extractSpain(extracted, label)) {
					break;
				}
				if (extractDfgCoverage(extracted, label)) {
					break;
				}
				split(extracted, label.split(pattern));
			}
		}
		return extracted;
	}

	// Espana-Aragon-Teruel-Teruel
	boolean extractSpain(List<String> extracted, String label) {
		if (label.startsWith("Espa\u00F1a-") && !label.contains(":")) { // \u00F1a-"))
																		// {
			String[] places = label.split("\\-");
			if (places.length == 4) {
				if (places.length > 0) {
					for (String place : places) {
						extracted.add(place);
					}
					return true;
				}
			}
		}
		return false;
	}

	// GB UNITED KINGDOM
	boolean extractDfgCoverage(List<String> extracted, String label) {
		if (precheckisDfgCoverage(label)) {
			if (StringUtils.capitalize(label).equals(label)) {
				String countryAbbreviated = StringUtils.substringBefore(label,
						" ");
				extracted.add(countryAbbreviated);
				String countrySpelled = StringUtils.substringAfter(label, " ");
				if (!StringUtils.isEmpty(countrySpelled)) {
					extracted.add(countrySpelled);
					return true;
				}
			}
		}
		return false;
	}

	boolean precheckisDfgCoverage(String label) {
		return label.length() > 3 && Character.isUpperCase(label.charAt(0))
				&& Character.isUpperCase(label.charAt(1));
	}

	void split(List<String> extracted, String[] lbls) {
		for (String l : lbls) {
			String lt = l.trim();
			if (lengthFilter(lt)) {
				if (specialCharacterFilter(lt)) {
					extracted.add(lt);
				}
			}
		}
	}

	private boolean specialCharacterFilter(String label) {

		// in full-text labels should be capitalised
		if (fullTextMode && !Character.isUpperCase(label.charAt(0))) {
			return false;
		}

		// otherwise make it lower case
		label = label.toLowerCase();
		for (int i = 0; i < label.length(); i++) {
			int words = 0;
			char c = label.charAt(i);
			if (Character.isLetter(c) || c == ' ' || c == '-') {
				if (c == ' ' || c == '-') {
					words++;
				}
				if (words > 3) {
					// multi-word names sound suspicious
					return false;
				}
				// ok
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean lengthFilter(String label) {
		return (label.length() > minLength && label.length() < 30);
	}
}
