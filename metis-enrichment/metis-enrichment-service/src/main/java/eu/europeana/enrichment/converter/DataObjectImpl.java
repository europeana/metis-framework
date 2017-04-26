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
package eu.europeana.enrichment.converter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.enrichment.api.ObjectRule;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.path.PathMap;
import eu.europeana.enrichment.path.PathMap.MatchResult;
import eu.europeana.enrichment.triple.XmlValue;
import eu.europeana.enrichment.xconverter.api.DataObject;

/**
 * A record consists of one or more parts with arbitrary nesting that are
 * converted one-by-one. I think we first convert the whole and then convert the
 * parts, as the parts typically refer to the whole.
 * 
 * @author Borys Omelayenko
 * 
 */
class DataObjectImpl implements DataObject {
	Logger log = LoggerFactory.getLogger(getClass().getName());

	// values of this object
	private PathMap<ListOfValues> mapPathToValue = new PathMap<ListOfValues>();

	// associated rule
	private ObjectRule dataObjectRule;

	// object separating path
	private Path separatingPath;

	private DataObject parent;

	private List<DataObject> children = new ArrayList<DataObject>();

	public List<DataObject> getChildren() {
		return children;
	}

	public DataObjectImpl(Path path, ObjectRule map, DataObject parent) {
		this.dataObjectRule = map;
		this.separatingPath = path;
		this.parent = parent;
		if (parent != null) {
			parent.getChildren().add(this);
		}
	}

	public List<DataObject> findAllChildren() {
		List<DataObject> allChildren = new ArrayList<DataObject>();
		for (DataObject directChild : children) {
			allChildren.add(directChild);
			allChildren.addAll(directChild.findAllChildren());
		}
		return allChildren;
	}

	public DataObject getParent() {
		return parent;
	}

	public long size() {
		return mapPathToValue.keySet().size();
	}

	public ListOfValues getValues(Path query) throws Exception {

		ListOfValues result = new ListOfValues();
		for (MatchResult<ListOfValues> match : mapPathToValue.ask(query)) {
			if (match.getAttributeValue() == null) {
				for (XmlValue xmlValue : match.getStoredObject()) {
					result.add(xmlValue);
				}
			} else {
				result.add(new XmlValue(match.getAttributeValue()));
			}
		}
		return result;
	}

	public XmlValue getFirstValue(Path propertyName) throws Exception {
		if (propertyName == null)
			return null;
		ListOfValues allValues = getValues(propertyName);
		if (allValues.isEmpty())
			return null;
		if (!allValues.isSingleValued())
			log.warn("Possibly missing other values of " + propertyName
					+ ", used value '" + allValues.get(0) + "', all values: "
					+ allValues);
		return allValues.get(0);
	}

	public void addValue(Path path, XmlValue value) throws Exception {

		if (path.isAttributeQuery())
			throw new Exception(
					"Query for a specific attribute is not allowed as data path");

		if (value != null) {
			try {
				ListOfValues allValues = mapPathToValue.get(path);
				if (allValues == null) {
					allValues = new ListOfValues();
					mapPathToValue.put(path, allValues);
				}
				allValues.add(new XmlValue(value.getValue(), chooseLang(path,
						value)));
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Data object add value failed: " + path
						+ " " + value, e);
			}
		}
	}

	String chooseLang(Path path, XmlValue value) {
		String langFromPath = path.getLang();
		if (!StringUtils.isBlank(langFromPath)) {
			return langFromPath;
		}
		String langFromValue = value.getLang();
		if (!StringUtils.isBlank(langFromValue)) {
			return langFromValue;
		}
		return null;
	}

	@Override
	public Iterator<Path> iterator() {
		return mapPathToValue.keySet().iterator();
	}

	public ListOfValues getValueByExactMatch(Path pathToMatch) {
		return mapPathToValue.get(pathToMatch);
	}

	public ObjectRule getDataObjectRule() {
		return dataObjectRule;
	}

	public Path getIdPath() {
		return dataObjectRule.getPrimaryRecordIdPath();
	}

	public Path getSeparatingPath() {
		return separatingPath;
	}

	@Override
	public String toString() {
		String result = "";
		for (Path p : mapPathToValue.keySet()) {
			result += p.getPath() + "=" + mapPathToValue.get(p) + "\n";
		}
		return result;
	}

}