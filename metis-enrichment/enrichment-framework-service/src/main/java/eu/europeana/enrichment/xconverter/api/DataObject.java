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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import eu.europeana.enrichment.api.ObjectRule;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.Value;
import eu.europeana.enrichment.triple.XmlValue;

/**
 * A set of pairs (property name, value), used to represent a part of XML
 * document that corresponds to a domain object.
 * 
 * @author Borys Omelayenko
 * 
 */
public interface DataObject extends Iterable<Path> {
	public static class ListOfValues extends ArrayList<XmlValue> {
		private static final long serialVersionUID = 1L;

		public boolean isSingleValued() {
			Value value = null;
			for (Iterator<XmlValue> it = iterator(); it.hasNext();) {
				if (value == null) {
					value = it.next();
				} else {
					if (!value.equals(it.next())) {
						return false;
					}
				}
			}
			return true;
		}

		public ListOfValues(Collection<? extends XmlValue> c) {
			super(c);
		}

		public ListOfValues() {
			super();
		}
	}

	/**
	 * Gets all values of a property where {@link #query} may be a query, see
	 * {@link Path} for more.
	 */
	public ListOfValues getValues(Path query) throws Exception;

	/**
	 * Parent {@link DataObject} for part-of nested.
	 */
	public DataObject getParent();

	/**
	 * Direct children {@link DataObject} for part-of nested.
	 */
	public List<DataObject> getChildren();

	/**
	 * All, direct and indirect children {@link DataObject} for part-of nested.
	 */
	public List<DataObject> findAllChildren();

	/**
	 * Gets first value of a property. See {@link #getValues(Path)} to get them
	 * all.
	 * 
	 * @param propertyName
	 * @return
	 */
	public XmlValue getFirstValue(Path propertyName) throws Exception;

	/**
	 * Adds a new element value.
	 * 
	 * @param elementName
	 * @param newValue
	 */
	public void addValue(Path elementName, XmlValue newValue) throws Exception;

	/**
	 * Gets the {@link ObjectRule}, the rule to convert instances of this
	 * {@linkplain DataObject}.
	 * 
	 * @return
	 */
	public ObjectRule getDataObjectRule();

	public Path getIdPath() throws Exception;

	public long size();

	public Path getSeparatingPath();

}
