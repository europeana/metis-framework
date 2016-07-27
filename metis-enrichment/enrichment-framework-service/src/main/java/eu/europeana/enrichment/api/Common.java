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
package eu.europeana.enrichment.api;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Common constants.
 * 
 * @author Borys Omelayenko
 * 
 */
public class Common {

	private static Pattern removeDiacriticPattern = Pattern
			.compile("\\p{InCombiningDiacriticalMarks}+");

	public static String removeDiacritics(String text) {
		String nfdNormalizedString = Normalizer.normalize(text,
				Normalizer.Form.NFD);
		return removeDiacriticPattern.matcher(nfdNormalizedString).replaceAll(
				"");
	}

}
