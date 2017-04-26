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
package eu.europeana.enrichment.api.internal;

import java.util.Properties;

import eu.europeana.enrichment.api.internal.Language.Lang;

/**
 * Terms stored in vocabularies.
 * 
 * @author Borys Omelayenko
 * 
 */
public class Term implements Comparable<Term> {
	private static final String SLASH = "/";
	private String label;
	// very private namespace
	private String ns;
	private String code;
	private Term parent;
	private String vocabularyName;
	private Lang lang;
	private Properties properties;
	private String disambiguatingComment;
	private String confidenceComment;

	public Term(String label, Lang lang, CodeURI termCode, String vocabularyName) {
		if (label == null || termCode == null || vocabularyName == null) {
			throw new NullPointerException("One of parameters is null");
		}
		this.label = label;
		this.lang = lang;
		this.code = termCode.toString();
		int p = code.lastIndexOf(SLASH);
		if (p > 0 && p < code.length()) {
			this.ns = code.substring(0, p + 1);
			this.code = code.substring(p + 1);
		} else {
			this.ns = null;
		}

		this.vocabularyName = vocabularyName;
		this.properties = new Properties();
	}

	public String getCode() {
		if (ns == null)
			return code;
		return ns + code;
	}

	public String getVocabularyName() {
		return vocabularyName;
	}

	public String getLabel() {
		return label;
	}

	public Lang getLang() {
		return lang;
	}

	public Term getParent() {
		return parent;
	}

	public void setParent(Term parent) {
		this.parent = parent;
	}

	public boolean hasParent(CodeURI potentialParentCode) {
		String potentialParentStr = potentialParentCode.toString();
		Term term = this.parent;
		while (term != null) {
			if (potentialParentStr.equals(term.getCode()))
				return true;
			term = term.parent;
		}
		return false;
	}

	public String getProperty(String propertyName) {
		return properties.getProperty(propertyName);
	}

	public void setProperty(String propertyName, String value) {
		properties.setProperty(propertyName, value);
	}

	@Override
	public String toString() {
		return "\"" + getCode() + "=" + label
				+ (lang == null ? "" : ("@" + lang)) + "\"";
	}

	public int compareTo(Term t) {
		int r;
		r = this.getVocabularyName().compareTo(t.getVocabularyName());
		if (r != 0)
			return r;
		r = this.getCode().compareTo(t.getCode());
		if (r != 0)
			return r;
		r = this.getLabel().compareTo(t.getLabel());
		if (r != 0)
			return r;
		if (this.getLang() == null && t.getLang() != null)
			return -1;
		if (this.getLang() != null && t.getLang() == null)
			return 1;
		if (this.getLang() == null && t.getLang() == null)
			return 0;
		r = this.getLang().compareTo(t.getLang());
		return r;
	}

	public String getDisambiguatingComment() {
		return disambiguatingComment;
	}

	public void setDisambiguatingComment(String disambiguatingComment) {
		this.disambiguatingComment = disambiguatingComment;
	}

	public String getConfidenceComment() {
		return confidenceComment;
	}

	public void setConfidenceComment(String confidenceComment) {
		this.confidenceComment = confidenceComment;
	}
}
