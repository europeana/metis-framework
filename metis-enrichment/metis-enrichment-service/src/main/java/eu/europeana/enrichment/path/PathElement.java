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
package eu.europeana.enrichment.path;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;

import eu.europeana.enrichment.context.Namespaces;

/**
 * An XML element with possible attribute-value pairs, a part of a Path;
 * immutable.
 * 
 * @author Borys Omelayenko
 */
final class PathElement extends NamespacedName {

	private String pathFullNormalized;

	private Map<NamespacedName, String> attributeValues = new HashMap<NamespacedName, String>();

	/**
	 * Get value of this PE attribute.
	 * 
	 * @param attribute
	 * @return
	 */
	public String getAttributeValue(NamespacedName attribute) {
		return attributeValues.get(attribute);
	}

	/**
	 * Shallow query for attributes.
	 * 
	 * @return
	 */
	public boolean hasAttributes() {
		return !attributeValues.isEmpty();
	}

	// performance shortcut: additional shortcut to xml:lang
	private String lang = null;

	public String getLang() {
		return lang;
	}

	private NamespacedName queryAttribute = null;

	/**
	 * Private to keep PE immutable,
	 * 
	 * @param attributeName
	 * @param attributeNamespace
	 * @param value
	 */
	private void addAttribute(NamespacedName attribute, String value) {
		// keep a special link to query attribute
		queryAttribute = attributeValues.isEmpty() ? attribute : null;
		attributeValues.put(attribute, value);
		if ("lang".equals(attribute.getName())
				&& Namespaces.XML.getUri().equals(attribute.getNamespace())) {
			this.lang = value;
		}
	}

	private boolean isAttributeQuery = false;

	public boolean isAttributeQuery() {
		return isAttributeQuery;
	}

	public static PathElement makePathElement(XPScanner scanner,
			Namespaces namespaces) throws Exception {

		// element name, expanding namespace
		NamespacedName element = scanner.expandNamespaceForTag(namespaces);
		PathElement pathElement = new PathElement(element.getName(),
				element.getNamespace(), null);

		if (scanner.skipString("[")) {
			// attribute block
			boolean moreAttributesExpected = false;
			while (!scanner.skipString("]")) {
				int scannerPositionAtAttribute = scanner.pos();
				// attr + optional value or operation
				if (scanner.skipString("@")) {
					NamespacedName attribute = scanner
							.expandNamespaceForTag(namespaces);

					// attribute value
					String attributeValue = null;
					if (scanner.skipString("=")) {
						attributeValue = scanner.nextLiteral();
					}

					// attribute query?
					if (attributeValue == null) {
						pathElement.isAttributeQuery = true;
						if (pathElement == null) {
							throw new Exception("Error in " + scanner
									+ ", unexpected attribute query @"
									+ attribute);
						}
					}

					pathElement.addAttribute(
							new NamespacedName(attribute.getName(), attribute
									.getNamespace()), attributeValue);

					moreAttributesExpected = scanner.skipString(" and ");
					if (scanner.pos() == scannerPositionAtAttribute) {
						throw new Exception("Error in " + scanner);
					}
				} else {
					throw new Exception("Missing expected symbol @ at "
							+ scanner);
				}
			}

			if (moreAttributesExpected) {
				throw new Exception("More attributes expected on " + scanner);
			}
		} // and of attribute block

		pathElement.updateCachedRepresentations();
		return pathElement;
	}

	public PathElement(String name, String namespace, Attributes attributes) {
		super(name, namespace);
		if (attributes != null) {
			for (int i = 0; i < attributes.getLength(); i++) {
				addAttribute(new NamespacedName(attributes.getLocalName(i),
						attributes.getURI(i)), attributes.getValue(i));
			}
		}
		updateCachedRepresentations();
	}

	public PathElement(NamespacedName element, NamespacedName attribute) {
		super(element.getName(), element.getNamespace());
		if (attribute != null) {
			addAttribute(attribute, null);
		}
		updateCachedRepresentations();
	}

	public static PathElement appendAttribute(PathElement pe,
			NamespacedName attribute, String value) {
		PathElement npe = new PathElement(pe);
		npe.addAttribute(attribute, value);
		npe.updateCachedRepresentations();
		return npe;
	}

	/**
	 * Creates a shallow clone of an element, with a new parent.
	 * 
	 * @param peToShallowClone
	 * @param parent
	 */
	public PathElement(PathElement peToShallowClone) {
		super(peToShallowClone.getName(), peToShallowClone.getNamespace());
		// this.parent = peToShallowClone.getParent();
		for (NamespacedName attribute : peToShallowClone.attributeValues
				.keySet()) {
			addAttribute(attribute,
					peToShallowClone.attributeValues.get(attribute));
		}
		updateCachedRepresentations();
	}

	// immutable objects
	private void updateCachedRepresentations() {
		// a single attribute and no values
		isAttributeQuery = queryAttribute != null
				&& attributeValues.get(queryAttribute) == null;

		pathFullNormalized = getExpanded()
				+ toPath(isAttributeQuery(), attributeValues);

	}

	@Override
	public String toString() {
		return getPath();
	}

	public String getPath() {
		return pathFullNormalized;
	}

	public int getAttrCount() {
		return attributeValues.size();
	}

	public NamespacedName getQueryAttribute() {
		return isAttributeQuery ? queryAttribute : null;
	}

	public Set<NamespacedName> getAttributesAsSet() {
		return attributeValues.keySet();
	}

	private static String toPath(boolean isAttributeQuery,
			Map<NamespacedName, String> attributes) {
		StringBuilder result = new StringBuilder();
		if (attributes.size() > 0) {
			result.append(isAttributeQuery ? "/" : "[");

			// sorted list of attributes
			List<NamespacedName> names = new LinkedList<NamespacedName>();
			names.addAll(attributes.keySet());
			Collections.sort(names);

			boolean first = true;
			for (NamespacedName attribute : names) {
				if (attribute == null)
					throw new NullPointerException("NULL attribute name at "
							+ attributes);
				String v = attributes.get(attribute);
				if (v == null && !isAttributeQuery)
					throw new NullPointerException("NULL value of attribute "
							+ attribute + " at " + attributes);

				if (!first && isAttributeQuery)
					throw new RuntimeException(
							"Attrbiute query with multiple attributes "
									+ attributes);
				result.append((first ? "" : " and ") + "@" + attribute
						+ (isAttributeQuery ? "" : ("='" + v + "'")));
				first = false;
			}
			result.append(isAttributeQuery ? "" : "]");
		}
		return result.toString();
	}

	@Override
	public int hashCode() {
		return pathFullNormalized.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		PathElement other = (PathElement) obj;
		return pathFullNormalized.equals(other.pathFullNormalized);
	}

}
