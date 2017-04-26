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

import eu.europeana.enrichment.api.Rule;
import eu.europeana.enrichment.xconverter.api.PropertyRule;

/**
 * May invoke one of the two child rules: success and failure rules, depending
 * on a condition.
 * 
 * @author Borys Omelayenko
 * 
 */
public abstract class AbstractBranchRule extends PropertyRule {

	protected Rule success;
	protected Rule failure;

	@Override
	public String getAnalyticalRuleClass() {
		return "Branch";
	}

	public AbstractBranchRule(PropertyRule success, PropertyRule failure) {
		super(success, failure);
		this.success = success;
		this.failure = failure;
	}

}
