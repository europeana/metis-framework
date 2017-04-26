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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.europeana.enrichment.api.DataObjectPreprocessor;
import eu.europeana.enrichment.api.ObjectRule;
import eu.europeana.enrichment.api.Rule;
import eu.europeana.enrichment.api.Task;
import eu.europeana.enrichment.context.Concepts;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.path.PathMap;
import eu.europeana.enrichment.path.PathMap.MatchResult;
import eu.europeana.enrichment.path.PathMap.MatchResultIterable;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.ResourceValue;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.triple.XmlValue;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.DataObject.ListOfValues;
import eu.europeana.enrichment.xconverter.api.PropertyRule;

/**
 * A rule that separates objects and contains property mappings executed within
 * this object.
 * 
 * @author Borys Omelayenko
 * 
 */
public class ObjectRuleImpl extends ObjectRule {

	private Task task;

	// property maps
	private PathMap<Rule> mapPathToRule = new PathMap<Rule>();

	private boolean qualifiedRecordIdentifier = true;

	private Path secondaryRecordIdTag;

	private Path primaryRecordIdTag;

	private Path recordSeparatingPath;

	private List<DataObjectPreprocessor> preprocessor = new ArrayList<DataObjectPreprocessor>();

	private ObjectRule parent;

	@Override
	public String getAnalyticalRuleClass() {
		return "ConvertObject";
	}

	/**
	 * Converts an object represented with XML tag. An XML record may have
	 * (repeating) tags that indicate separate (repeating) objects, parts of the
	 * whole record. These objects need to be processed separately, and the
	 * value of each object property should be treated in the context of this
	 * specific part.
	 * 
	 * A typical example - multiple tags <code>title</code> where each may
	 * contain two subtags: <code>title</code> with the actual text, and
	 * <code>titleType</code> that indicates the kind of title.
	 * 
	 * The root element of an XML file may contain the parts, where each part
	 * corresponds to a record (where record properties, in turn, are processed
	 * within the context of the record). Thus, there is no way to escape
	 * creating a part.
	 * 
	 * @param primaryRecordIdTag
	 *            is a Path to a tag that, after preprosessing is completed,
	 *            should hold object id (that implies that you can safely alter
	 *            it in the preprocessor).
	 * 
	 */

	public static ObjectRule makeObjectRule(Task task,
			Path objectSeparatingTag, Path primaryRecordIdTag,
			Path secondaryRecordIdTag, ObjectRule parent,
			boolean registerAsPartListener) {
		return new ObjectRuleImpl(task, objectSeparatingTag,
				primaryRecordIdTag.isAbsolute() ? primaryRecordIdTag
						: new Path(objectSeparatingTag, primaryRecordIdTag),
				secondaryRecordIdTag.isAbsolute() ? secondaryRecordIdTag
						: new Path(objectSeparatingTag, secondaryRecordIdTag),
				parent, registerAsPartListener);
	}


	private ObjectRuleImpl(Task task, Path recordSeparatingTag,
			Path primaryRecordIdTag, Path secondaryRecordIdTag,
			ObjectRule parent, boolean registerAsPartListener) {
		super();
		this.task = task;
		this.recordSeparatingPath = recordSeparatingTag;
		this.primaryRecordIdTag = primaryRecordIdTag;
		this.secondaryRecordIdTag = secondaryRecordIdTag;
		this.parent = parent;
		if (registerAsPartListener) {
			this.task.addPartListener(this);
		}
	}

	@Override
	public final boolean selectorPostCondition(DataObject dataObject)
			throws Exception {

		try {
			return shouldBeConverted(dataObject, false);
		} catch (Exception e) {
			throw new Exception("Exception in postcondition", e);
		}
	}

	@Override
	public final boolean selectorPreCondition(DataObject dataObject)
			throws Exception {
		try {
			return shouldBeConverted(dataObject, true);
		} catch (Exception e) {
			throw new Exception("Exception in precondition", e);
		}
	}

	@Override
	public final void preprocess(DataObject dataObject) throws Exception {
		try {
			for (DataObjectPreprocessor p : preprocessor) {
				p.process(dataObject);
			}
		} catch (Exception e) {
			throw new Exception("Exception in preprocessor", e);
		}
	}

	private boolean shouldBeConverted(DataObject dataObject,
			boolean isPrecondition) throws Exception {
		try {
			for (DataObjectPreprocessor aSelector : preprocessor) {
				if (!(isPrecondition ? aSelector.preCondition(dataObject)
						: aSelector.postCondition(dataObject)))
					return false;
			}
		} catch (Exception e) {
			// e.printStackTrace();
			throw new Exception("Problem in selector for dataset "
					+ task.getDatasetId() + "\n" + e.getLocalizedMessage(), e);
		}

		try {
			if (parent != null
					&& !(isPrecondition ? parent
							.selectorPreCondition(dataObject.getParent())
							: parent.selectorPostCondition(dataObject
									.getParent())))
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Problem with parent in selector for dataset "
					+ task.getDatasetId());
		}

		return true;
	}

	@Override
	public final void addPreprocessor(DataObjectPreprocessor preproc) {
		this.preprocessor.add(preproc);
	}

	// to ensure that we do not get the same rule assigned to a different
	// source path
	private Set<Rule> rulesAlreadyUsed = new HashSet<Rule>();

	@Override
	public void addRule(PropertyRule rule) throws Exception {
		// in fact, a rule may encapsulate multiple rules
		for (PropertyRule realRule : rule.getExpandedRules()) {
			// check if we have a fresh copy of this rule
			if (rulesAlreadyUsed.contains(realRule)) {
				throw new Exception("An instance of rule " + realRule
						+ " has already been used in ObjectRule " + this);
			}
			rulesAlreadyUsed.add(realRule);

			if (realRule.getSourcePath() == null) {
				throw new Exception("An instance of rule " + realRule
						+ " has no XML source path assigned");
			}

			// delegate to Abs/Rel
			if (realRule.getSourcePath().isAbsolute())
				addAbsRule(realRule.getSourcePath(), realRule);
			else
				addRelRule(realRule.getSourcePath(), realRule);
		}
	}

	@Override
	public final void addAbsRule(Path absolutePath, PropertyRule rule)
			throws Exception {
		if (mapPathToRule.containsPath(absolutePath))
			throw new Exception("Duplicate objectRule for property "
					+ absolutePath + " in the source code.");
		mapPathToRule.put(absolutePath, rule);
		rule.setTask(getTask());
		rule.setSourcePath(absolutePath);
		rule.setObjectRule(this);
	}

	@Override
	public final void addRelRule(Path relativePath, PropertyRule rule)
			throws Exception {
		addAbsRule(new Path(recordSeparatingPath, relativePath), rule);
	}

	private Map<Path, Integer> missedPaths = new HashMap<Path, Integer>();

	@Override
	public final Map<Path, Integer> getMissedPaths() {
		return missedPaths;
	}

	@Override
	public final MatchResultIterable<Rule> getRule(Path data) throws Exception {
		MatchResultIterable<Rule> rules = getRulesForPath(data);

		if (rules.isEmpty()) {
			rules = getRulesForEmptyPath();
		}

		if (!rules.iterator().hasNext()) {
			Integer count = missedPaths.get(data);
			if (count == null) {
				count = 1;
			} else {
				count++;
			}
			missedPaths.put(data, count);
		}
		return rules;
	}

	private MatchResultIterable<Rule> getRulesForPath(Path data)
			throws Exception {
		return mapPathToRule.answer(data);
	}

	private MatchResultIterable<Rule> getRulesForEmptyPath() throws Exception {
		return mapPathToRule.answer(new Path(""));
	}

	@Override
	public final void processDataObject(String subject, DataObject dataObject)
			throws Exception {

		// TODO! ugly!!! reverse? first rules then paths?
		Set<Path> uniqueDataPaths = new HashSet<Path>();
		for (Path dataPath : dataObject) {
			uniqueDataPaths.add(dataPath);
		}
		for (Path dataPath : uniqueDataPaths) {
			for (MatchResult<Rule> mr : getRule(dataPath)) {
				Rule rule = mr.getStoredObject();
				// data values: elements and attributes treated separately
				ListOfValues dataValues = null;
				try {
					if (rule.getSourcePath() != null
							&& rule.getSourcePath().isAttributeQuery()) {
						dataValues = new ListOfValues();
						String attributeValue = mr.getAttributeValue();
						if (attributeValue != null)
							dataValues.add(new XmlValue(attributeValue));
					} else {
						dataValues = dataObject.getValues(dataPath);
					}

				} catch (Exception e) {
					throw new Exception(
							"Error in ObjectRule in acquiring values for rule "
									+ rule, e);
				}
				// process each value
				for (XmlValue dataValue : dataValues) {
					Triple nTriple;
					if (dataValue.getValue().length() > 0) {
						try {
							// qualified local identifiers with namespaces
							ResourceValue subjectOfTriple = new ResourceValue(
									this.isQualifiedLocalRecordIdentifier() ? task
											.getTargetNamespace().getUri() : "",
									subject);
							XmlValue valueOfTriple = dataValue;

							if (dataPath.getPath().equals(
									Concepts.ANNOCULTOR.PARENT_TO_PART
											.toString())) {
								// special property: set parent as subject
								valueOfTriple = new XmlValue(
										subjectOfTriple.getValue());
								subjectOfTriple = new ResourceValue(
										dataValue.getValue());
							}

							nTriple = new Triple(subjectOfTriple.getValue(),
									new Property(dataPath), valueOfTriple, null);
						} catch (Exception e) {
							throw new Exception(
									"Error in ObjectRule on creating source triple for (original) subject (URL expected) '"
											+ subject
											+ "', value '"
											+ dataValue + "', path " + dataPath,
									e);
						}

						try {
							rule.fire(nTriple, dataObject);
						} catch (Exception e) {
							throw new Exception(
									"Error in ObjectRule on firing rule "
											+ rule + " on source triple "
											+ nTriple, e);
						}
					}
				}
			}
		}
	}

	@Override
	public final String getDataObjectId(DataObject dataObject) throws Exception {
		ListOfValues values = dataObject.getValues(primaryRecordIdTag);
		if (values.size() == 0) {
			throw new Exception("No object id found for primary record id tag "
					+ primaryRecordIdTag + "\nand data object\n"
					+ dataObject.toString());
		}
		if (values.isSingleValued()) {
			return values.get(0).getValue();
		}

		throw new Exception("Multiple id's found: " + values);
	}

	@Override
	public List<PropertyRule> getChildRules() {
		List<PropertyRule> r = new ArrayList<PropertyRule>();
		for (Path path : mapPathToRule.keySet()) {
			Rule rule = mapPathToRule.get(path);
			if (rule instanceof PropertyRule)
				r.add((PropertyRule) rule);
		}
		return r;
	}

	@Override
	public final Path getSecondaryRecordIdPath() {
		return secondaryRecordIdTag;
	}

	@Override
	public final void setSecondaryRecordIdTag(Path secondaryRecordIdTag) {
		this.secondaryRecordIdTag = secondaryRecordIdTag;
	}

	@Override
	public final Path getPrimaryRecordIdPath() {
		return primaryRecordIdTag;
	}

	@Override
	public final void setPrimaryRecordIdField(Path primaryRecordIdTag) {
		this.primaryRecordIdTag = primaryRecordIdTag;
	}

	@Override
	public final Path getRecordSeparatingPath() {
		return recordSeparatingPath;
	}

	@Override
	public final void setRecordSeparatingTag(Path recordSeparatingTag) {
		this.recordSeparatingPath = recordSeparatingTag;
	}

	@Override
	public final ObjectRule getParent() {
		return parent;
	}

	@Override
	public final Task getTask() {
		return task;
	}

	@Override
	public final boolean isQualifiedLocalRecordIdentifier() {
		return qualifiedRecordIdentifier;
	}

	/**
	 * Do not prefix the identifier with the target namespace.
	 */
	@Override
	public final void assumeQualifiedLocalRecordIdentifier() {
		this.qualifiedRecordIdentifier = false;
	}

	@Override
	public final Path getSourcePath() {
		return recordSeparatingPath;
	}

	@Override
	public final String toString() {
		return "ObjectRule " + this.getClass().getCanonicalName()
				+ " applied to record/tag " + recordSeparatingPath + " with "
				+ mapPathToRule.keySet().size() + " child rules.";
	}

}
