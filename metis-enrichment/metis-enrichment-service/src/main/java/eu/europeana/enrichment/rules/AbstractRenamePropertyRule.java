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

import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.Graph;

public abstract class AbstractRenamePropertyRule extends AbstractNoBranchRule {
	private Property targetPropertyName = null;

	private Graph graph;

	@Override
	public String getAnalyticalRuleClass() {
		return "RenameProperty";
	}

	public AbstractRenamePropertyRule(Property targetPropertyName, Graph target) {
		this.targetPropertyName = targetPropertyName;
		this.graph = target;
	}

	@Override
	public void fire(Triple triple, DataObject dataObject) throws Exception {
		Triple t = triple.changeRule(this);

		if (t.getValue().getValue().length() > 0)
			graph.add(t);

	}

	public void setTargetPropertyName(Property targetPropertyName) {
		this.targetPropertyName = targetPropertyName;
	}

	public Property getTargetPropertyName() {
		return targetPropertyName;
	}

}
