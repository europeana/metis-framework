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

import java.util.List;

import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.PropertyRule;

/**
 * Conversion rule able to write to named graphs given a source triple and a
 * source data object.
 * 
 * @author Borys Omelayenko
 * 
 */
public abstract class Rule {

	public Rule() {
		super();
	}

	/**
	 * Generic method for writing triples as a response to a (source) triple.
	 * 
	 * @param triple
	 *            the source <code>Triple</code> to be read and converted
	 * @param dataObject
	 *            current object consisting of XML tags and their values
	 * 
	 */
	public abstract void fire(Triple triple, DataObject dataObject)
			throws Exception;

	protected boolean initialized = false;

	protected void init() throws Exception {
		// should be overridden in final rules... and checked there, in the xml
		// api generated code
	}

	/**
	 * For analyzing the rule base the rules are grouped into groups:
	 * rename-property, domain-specific, etc. May be ignored if you are not
	 * going to develop any science on top of your rules.
	 * 
	 */
	public abstract String getAnalyticalRuleClass();

	private Task task;

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	@Override
	public String toString() {
		return "Rule " + this.getClass().getCanonicalName();
	}

	public abstract Path getSourcePath();

	public abstract List<PropertyRule> getChildRules();

	/**
	 * Is invoked before a call to {@link Rule#fire(Triple, DataObject)}.
	 * 
	 * @param sourceTriple
	 * @param sourceDataObject
	 * @throws Exception
	 */
	public Triple onInvocation(Triple sourceTriple, DataObject sourceDataObject)
			throws Exception {
		return sourceTriple;
	}

	/**
	 * In invoked before a call to
	 * {@linkplain Rule#onInvocation(Triple, DataObject)} and allows to prevent
	 * rule from firing.
	 * 
	 * @param sourceTriple
	 * @param sourceDataObject
	 * @return
	 * @throws Exception
	 */
	public boolean onPreCondition(Triple sourceTriple,
			DataObject sourceDataObject) throws Exception {
		return true;
	}
}
