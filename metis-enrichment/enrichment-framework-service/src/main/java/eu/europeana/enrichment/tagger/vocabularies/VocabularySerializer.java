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
package eu.europeana.enrichment.tagger.vocabularies;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.enrichment.api.internal.CodeURI;
import eu.europeana.enrichment.api.internal.Term;
import eu.europeana.enrichment.api.internal.TermList;
import eu.europeana.enrichment.api.internal.Language.Lang;

/**
 * Encapsulates a vocabulary with (de-)serialization.
 * 
 * @author Borys Omelayenko
 * 
 */
public class VocabularySerializer {
	

	Logger log = LoggerFactory.getLogger(getClass().getName());

	private Vocabulary voc;
	private String vocabularyName;
	private Map<String, String> termParentMap = new HashMap<String, String>();

	public String getParentCode(String termCode) {
		return termParentMap.get(termCode);
	}

	public VocabularySerializer(Vocabulary voc, String vocabularyName) {
		this.voc = voc;
		this.vocabularyName = vocabularyName;
	}

	public Term parseValue(CodeURI code, CodeURI parent, String value, Lang lang)
			throws Exception {
		Term term = new Term(value, lang, code, vocabularyName);
		voc.putTerm(term);
		if (parent != null)
			termParentMap.put(code.getUri(), parent.getUri());
		return term;
	}

	public void parseValue(CodeURI code, CodeURI parent, String value)
			throws Exception {
		parseValue(code, parent, value, null);
	}

	public static class SerializedProperties {
		private Properties terms;
		private Properties parents;

		public SerializedProperties() {
			super();
			this.terms = new Properties();
			this.parents = new Properties();
		}

		public Properties getTerms() {
			return terms;
		}

		public Properties getParents() {
			return parents;
		}
	}

	public void deserializeFromProperties(SerializedProperties props)
			throws Exception {
		for (Map.Entry<Object, Object> entry : props.getTerms().entrySet()) {
			CodeURI termURI = new CodeURI(entry.getKey().toString());
			String labelLine = entry.getValue().toString();
			if (!labelLine.contains("."))
				throw new RuntimeException("Error in cache line " + labelLine);
			String[] sizes = labelLine.substring(0, labelLine.indexOf('.'))
					.split(",");
			labelLine = labelLine.substring(labelLine.indexOf('.') + 1);

			int start = 0;
			for (int i = 0; i < sizes.length; i++) {
				int langPos = sizes[i].indexOf("+");
				int size;
				if (langPos < 0) {
					size = Integer.parseInt(sizes[i]);
				} else {
					size = Integer.parseInt(sizes[i].substring(0, langPos));
				}
				String label = labelLine.substring(start, start + size);
				start += size + 1; // +1 for separator
				Lang lang = null;
				if (langPos >= 0) {
					int langSize = Integer.parseInt(sizes[i]
							.substring(langPos + 1));
					String langStr = labelLine.substring(start, start
							+ langSize);
					start += langSize + 1;
					lang = Lang.valueOf(langStr);
				}

				String parent = props.getParents().getProperty(
						termURI.toString());
				Term term = parseValue(termURI, parent == null ? null
						: new CodeURI(parent), label, lang);
				Collection<Term> additionalTerms = voc
						.expandVocabularyTermOnLoad(term);
				for (Term t : additionalTerms) {
					voc.putTerm(t);
				}
			}
		}
	}

	public SerializedProperties serializeToProperties() throws Exception {
		SerializedProperties props = new SerializedProperties();

		for (TermList termList : voc.listAllByCode()) {
			// make serialized line for a term
			String sizes = null;
			String values = null;
			String codeOfFirstTerm = null;
			Term parentOfFirstTerm = null;
			for (Term t : termList) {
				codeOfFirstTerm = t.getCode();
				parentOfFirstTerm = t.getParent();

				sizes = sizes == null ? ("" + t.getLabel().length()) : (sizes
						+ "," + t.getLabel().length());
				values = values == null ? t.getLabel() : (values + ";" + t
						.getLabel());
				if (t.getLang() != null) {
					sizes += "+" + t.getLang().getCode().length();
					values += "@" + t.getLang().getCode();
				}
			}

			// write this terms' line
			if (codeOfFirstTerm == null)
				throw new Exception("Empty vocabulary line");
			props.getTerms().put(codeOfFirstTerm, sizes + "." + values);

			// write parent
			if (parentOfFirstTerm != null)
				props.getParents().put(codeOfFirstTerm,
						parentOfFirstTerm.getCode());
		}
		return props;
	}

}
