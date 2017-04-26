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
package eu.europeana.enrichment.rules;

import java.util.regex.Pattern;

import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.triple.Value;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.PropertyRule;

/**
 * Used to invoke a writer depending on the result of a regular expression.
 * 
 * @author Borys Omelayenko
 * 
 */
public class BranchOnPatternRule extends AbstractBranchRule {
	private String patternString;

	private Pattern pattern;

	private Path propertyName;

	private boolean fullLinePattern;



	/**
	 * If RE matches on the value of <code>propertyName</code> of this object.
	 * Note that there may be multiple occurrences of this
	 * <code>propertyName</code> and their values would be merged.
	 * 
	 * @param pattern
	 * @param propertyName
	 *            property which value should be evaluated. Use
	 *            <code>null</code> if you want to use the current triple
	 *            instead of naming it to avoid errors with multiple occurrences
	 *            of this property in the same record.
	 * @param success
	 * @param failure
	 */
	public BranchOnPatternRule(String pattern, Path propertyName,
			PropertyRule success, PropertyRule failure) {
		super(success, failure);
		this.fullLinePattern = pattern.startsWith("^") && pattern.endsWith("$");
		this.pattern = Pattern.compile(pattern);
		this.propertyName = propertyName;
		this.patternString = pattern;
	}

	@Override
	public void fire(Triple triple, DataObject dataObject) throws Exception {

		Value value = null;
		if (!fullLinePattern) {
			throw new Exception(BranchOnPatternRule.class.getName()
					+ " receives pattern (" + patternString
					+ ") that is not wrapped up with ^ and $");
			// used to be a warning
			// warned, don't care
			// fullLinePattern = true;
		}
		// current property is treated separately to avoid issues with multiple
		// occurrences of property
		if (propertyName == null)
			value = triple.getValue();
		else
			value = dataObject.getFirstValue(propertyName);

		if (value != null && pattern.matcher(value.getValue()).find()) {
			if (success != null) {
				success.fire(triple, dataObject);
				return;
			}
		} else {
			if (failure != null) {
				failure.fire(triple, dataObject);
				return;
			}
		}
	}

}
