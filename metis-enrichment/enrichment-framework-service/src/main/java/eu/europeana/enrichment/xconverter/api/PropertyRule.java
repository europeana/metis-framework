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
package eu.europeana.enrichment.xconverter.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.europeana.enrichment.api.ObjectRule;
import eu.europeana.enrichment.api.Rule;
import eu.europeana.enrichment.api.Task;
import eu.europeana.enrichment.path.Path;

/**
 * Conversion rule able to write to named graphs given a source triple and a
 * source data object.
 * 
 * @author Borys Omelayenko
 * 
 */
public abstract class PropertyRule extends Rule {

	public static final String NULL = "NULL";

	public PropertyRule(PropertyRule... childRules) {
		addChildRule(childRules);
	}

	private List<PropertyRule> childRules = new ArrayList<PropertyRule>();

	protected void addChildRule(PropertyRule... rules) {
		for (PropertyRule aRule : rules) {
			if (aRule != null) {
				childRules.add(aRule);
			}
		}
	}

	/**
	 * List of child rules.
	 * 
	 */
	@Override
	public List<PropertyRule> getChildRules() {
		return childRules;
	}

	private ObjectRule objectRule;

	public ObjectRule getObjectRule() {
		return objectRule;
	}

	public void setObjectRule(ObjectRule objectRule) {
		this.objectRule = objectRule;
		for (Rule rule : getChildRules()) {
			if (rule instanceof PropertyRule) {
				((PropertyRule) rule).setObjectRule(objectRule);
			}
		}
	}

	public List<PropertyRule> getExpandedRules() {
		return Collections.singletonList(this);
	}

	private Path sourcePath;

	/**
	 * The source (XML) path this rule is assigned to.
	 */
	@Override
	public Path getSourcePath() {
		return sourcePath;
	}

	/**
	 * Statistics: Sets the source (XML) path.
	 */
	public void setSourcePath(Path path) {
		this.sourcePath = path;
		for (PropertyRule child : childRules) {
			child.setSourcePath(path);
		}
	}

	@Override
	public void setTask(Task task) {
		super.setTask(task);
		for (Rule rule : getChildRules()) {
			rule.setTask(task);
		}
	}

	@Override
	public String toString() {
		return "Rule " + this.getClass().getCanonicalName()
				+ " applied to tag " + sourcePath + " with "
				+ getChildRules().size() + " child rules.";
	}

}
