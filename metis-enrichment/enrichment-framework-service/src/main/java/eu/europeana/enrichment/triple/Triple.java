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
package eu.europeana.enrichment.triple;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;

import eu.europeana.enrichment.api.Rule;
import eu.europeana.enrichment.api.internal.CodeURI;

/**
 * Triple with a subject, property, and value that corresponds to an RDF triple.
 * In addition, to the two RDF types (Resource and Literal), it has type
 * <code>Source</code> for the source XML/DB triples.
 * 
 * Each triple is assigned a target, and each file is stored to a separate
 * physical file.
 * 
 * 
 * @author Borys Omelayenko
 * 
 */
public class Triple {
	private String subject;

	private Property property;

	private Value value;

	private Rule rule;

	private String comment;

	/**
	 * Creates a triple.
	 * 
	 * @param subject
	 *            subject uri
	 * @param property
	 *            property uri
	 * @param value
	 *            either uri or a literal
	 * @param rule
	 *            the rule that created this triple
	 * @param comment
	 *            text human-oriented comment, optional
	 */
	public Triple(String subject, Property property, Value value, Rule rule,
			String... comment) throws URISyntaxException, MalformedURLException {
		this.subject = new CodeURI(subject).toString();
		this.property = property;
		this.value = value;
		this.rule = rule;
		this.comment = StringUtils.join(comment, ";");
	}

	public Rule getRule() {
		return rule;
	}

	public Triple copy() throws Exception {
		return new Triple(subject, property, value, rule);
	}

	public Triple changeProperty(Property newPropertyName) throws Exception {
		Triple triple = this.copy();
		triple.setProperty(newPropertyName);
		return triple;
	}

	public Triple changeSubject(String newSubject) throws Exception {
		Triple triple = this.copy();
		triple.setSubject(newSubject);
		return triple;
	}

	public Triple changeValue(Value newValue) throws Exception {
		Triple triple = this.copy();
		triple.setValue(newValue);
		return triple;
	}

	public Triple changePropertyAndValue(Property newPropertyName,
			Value newValue) throws Exception {
		Triple triple = this.copy();
		triple.setProperty(newPropertyName);
		triple.setValue(newValue);
		return triple;
	}

	public Triple changeRule(Rule rule) throws Exception {
		Triple triple = this.copy();
		triple.setRule(rule);
		return triple;
	}

	private void setRule(Rule rule) {
		this.rule = rule;
	}

	public boolean isValueEmpty() {
		return (value == null) || (value.getValue().length() == 0);
	}

	public Property getProperty() {
		return property;
	}

	private void setProperty(Property property) {
		this.property = property;
	}

	public String getSubject() {
		return subject;
	}

	private void setSubject(String subject) {
		this.subject = subject;
	}

	public Value getValue() {
		return value;
	}

	private void setValue(Value value) {
		this.value = value;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public String toString() {
		return "<" + subject + "," + property
				+ (ValueHelper.isLiteral(value) ? "" : ".")
				+ ValueHelper.lang(value, "@", "") + "," + value + ">";
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}

}
