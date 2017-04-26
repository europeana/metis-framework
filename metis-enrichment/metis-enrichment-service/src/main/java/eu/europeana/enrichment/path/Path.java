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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;

import eu.europeana.enrichment.context.Namespace;
import eu.europeana.enrichment.context.Namespaces;

/**
 * 
 * Represents a (simplified) XPath expression, represented as a
 * slash-concatenation of path elements, of the form
 * 
 * <code>element-name[@attribute='value' and @attribute='value']</code>
 * 
 * @author Borys Omelayenko
 */
public class Path extends LinkedList<PathElement> implements Comparable<Path> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * Constructors.
	 */
	private Path() {
	}

	/**
	 * Creates a new path by appending it to the parent.
	 * 
	 * @param parent
	 * @param childToClone
	 */
	public Path(Path parent, Path childToClone) {
		appendParent(parent);

		for (PathElement pathElement : childToClone) {
			add(new PathElement(pathElement));
		}

		if (childToClone.value != null) {
			getValueBuffer().append(childToClone.value);
		}
		updateCachedRepresentations();
	}

	private Path(Path parent, PathElement peToClone) {
		appendParent(parent);
		add(new PathElement(peToClone));
		updateCachedRepresentations();
	}

	public Path(String xpath) throws Exception {
		this(xpath, new Namespaces());
	}

	public Path(String xpath, Namespaces namespaces) throws Exception {
		XPScanner scanner = new XPScanner(xpath);

		// is absolute path (starting with /)
		this.isAbsolute = scanner.skipString("/") ? ABSOLUTE.absolute
				: ABSOLUTE.relative;

		// iterate element[attr] defs
		while (!scanner.eof()) {
			int scannerPositionAtElement = scanner.pos();

			add(PathElement.makePathElement(scanner, namespaces));

			if (!scanner.eof()) {
				if (!scanner.skipString("/")) {
					throw new Exception("Error, missing / in " + xpath);
				}
			}
			if (scanner.pos() == scannerPositionAtElement) {
				throw new Exception("Error in " + xpath);
			}
		}

		updateCachedRepresentations();
	}

	/**
	 * Creates a path from an XML tag, used in XML parser startElement.
	 */
	public Path(Path parent, String namespace, String tag, Attributes attributes)
			throws Exception {
		appendParent(parent);
		if (!StringUtils.isEmpty(namespace) || !StringUtils.isEmpty(tag)) {
			add(new PathElement(tag, namespace, attributes));
		}
		updateCachedRepresentations();
	}

	private void appendParent(Path parent) {
		// copy parent
		if (parent != null) {
			for (PathElement pathElement : parent) {
				add(pathElement);
			}
		}
	}

	public static final String PE_SEPARATOR = "*";

	// lang is computed at updateCacheRepresentations(), it should never be
	// directly assigned to
	private String lang;

	public String getLang() {
		return lang;
	}

	/**
	 * Enforces a new namespace.
	 * 
	 * @param pathOne
	 * @param pathTwo
	 * @return
	 */
	public static Path changeNamespace(Namespace ns, Path path)
			throws Exception {
		Path newPath = new Path();
		for (PathElement pe : path) {
			String expanded = pe.getExpanded();
			String nsPrefix = StringUtils.substringAfterLast(expanded, "/");
			if (expanded.contains("#")) {
				nsPrefix = StringUtils.substringAfterLast(expanded, "#");
			}
			if (StringUtils.isBlank(nsPrefix)) {
				nsPrefix = expanded;
			}
			newPath.add(new PathElement(nsPrefix, ns.getUri(), null));
		}
		newPath.updateCachedRepresentations();
		return newPath;
	}

	public int compareTo(Path o) {
		return getPath().compareTo(o.getPath());
	}

	private String pathElementsAttributesNormalized;

	private String pathElementsOnlyNormalized;

	private void updateCachedRepresentations() {
		// path as string
		pathElementsAttributesNormalized = "";
		pathElementsOnlyNormalized = "";

		boolean separatorNeeded = false;
		for (PathElement pe : this) {
			// string representations
			// TODO: escape and remove *
			if (separatorNeeded) {
				pathElementsAttributesNormalized += PE_SEPARATOR;
				pathElementsOnlyNormalized += PE_SEPARATOR;
			}
			separatorNeeded = true;
			pathElementsAttributesNormalized += pe.getPath();
			pathElementsOnlyNormalized += pe.getExpanded();

			// cascading xml:lang
			if (pe.getLang() != null) {
				lang = pe.getLang();
			}

			// deep has attributes
			hasAttributes |= pe.hasAttributes();
		}
	}

	public String getPathElementsOnly() {
		return pathElementsOnlyNormalized;
	}

	private boolean hasAttributes = false;

	/**
	 * Deep query for attributes.
	 * 
	 * @return
	 */
	public boolean hasAttributes() {
		return hasAttributes;
	}

	/*
	 * Value of this element
	 */
	private StringBuilder value = null;

	private StringBuilder getValueBuffer() {
		// most Paths have no values and do not need it
		if (value == null)
			value = new StringBuilder();
		return value;
	}

	public void appendValue(char[] characters, int begin, int end) {
		getValueBuffer().append(characters, begin, end);
	}

	public void appendValue(String string) {
		getValueBuffer().append(string);
	}

	public String getValue() {
		return value == null ? null : value.toString();
	}

	private enum ABSOLUTE {
		absolute, relative, undefined
	}

	private ABSOLUTE isAbsolute = ABSOLUTE.undefined;

	public boolean isAbsolute() {
		if (isAbsolute == ABSOLUTE.undefined)
			throw new RuntimeException(
					"Internal error: isAbsolute was not set for path "
							+ pathElementsAttributesNormalized);
		return isAbsolute == ABSOLUTE.absolute;
	}

	public boolean isAttributeQuery() {
		if (isEmpty()) {
			return false;
		}
		return getLast().isAttributeQuery();
	}

	public String getLastTagExpanded() {
		if (isEmpty()) {
			return "";
		}
		return getLast().getExpanded();
	}

	/**
	 * Generates all possible branches from the path, incl attrbiutes.
	 * 
	 * @return
	 */
	public List<Path> explicate() throws Exception {
		List<Path> paths = new ArrayList<Path>();
		explicate(paths, new Path());
		return paths;
	}

	private void explicate(List<Path> paths, Path prefix) throws Exception {
		if (prefix.size() == size()) {
			paths.add(new Path(null, prefix));
		} else {
			// next pe
			PathElement pe = get(prefix.size());

			// make a child path with elements only
			Path childElementsOnly = new Path(prefix, new PathElement(
					pe.getName(), pe.getNamespace(), null));

			// try to append this element, no atts
			explicate(paths, childElementsOnly);

			// branch via atts
			for (NamespacedName attr : pe.getAttributesAsSet()) {
				Path childWithAttribute = new Path(prefix, new PathElement(pe,
						attr));
				childWithAttribute.appendValue(pe.getAttributeValue(attr));
				paths.add(childWithAttribute);
			}
		}
	}

	@Override
	public int hashCode() {
		return getPath().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Path) {
			return pathElementsAttributesNormalized
					.equals(((Path) obj).pathElementsAttributesNormalized);
		}
		return false;
	}

	@Override
	public String toString() {
		// throw new RuntimeException("Calling toString is a sin");
		return getPath();
	}

	/**
	 * Returns string representation of the path.
	 * 
	 * @return
	 */
	public String getPath() {
		return pathElementsAttributesNormalized;
	}

	/*
	 * Presentation.
	 */
	public static String formatPath(Path path, Namespaces namespaces) {
		String pre = "<abbr class='namespaceAbbr' title=\"";
		String mid = "\">";
		String pos = ":</abbr> ";

		// path as string
		String result = "";
		boolean separatorNeeded = false;
		if (path != null) {
			for (PathElement pe : path) {
				// string representations
				if (separatorNeeded) {
					result += "/";// PE_SEPARATOR;
				}
				separatorNeeded = true;

				String element = pe.getPath();

				int nsEnd = findNamespaceEnd(element, namespaces);
				String x = "";
				if (nsEnd >= 0) {
					String nsUri = element.substring(0, nsEnd + 1);
					x = pre + nsUri + mid;

					String nick = namespaces.getNick(nsUri);
					if (nick == null) {
						x += namespaces.addNamespace(nsUri);
					} else {
						x += nick;
					}
					x += pos;
				}
				x += element.substring(nsEnd + 1);
				result += x;
			}
		} else {
			result += "NULL";
		}

		return result;
	}

	private static int findNamespaceEnd(String pathStr, Namespaces namespaces) {
		String path = pathStr;
		if (path.indexOf('[') >= 0) {
			path = path.substring(0, pathStr.indexOf('['));
		}
		int nsEnd = -1;
		// try to find it in the known namespaces
		for (String uri : namespaces.listAllUris()) {
			if (path.startsWith(uri)) {
				if (path.substring(uri.length()).matches("(\\w)+")) {
					return uri.length() - 1;
				}
			}
		}
		if (path.lastIndexOf("/") > nsEnd) {
			nsEnd = path.lastIndexOf("/");
		}
		if (path.lastIndexOf("#") > nsEnd) {
			nsEnd = path.lastIndexOf("#");
		}

		return nsEnd;
	}

}
