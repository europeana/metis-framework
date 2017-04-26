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

/**
 * 
 * @author Borys Omelayenko
 */
class NamespacedName implements Comparable<NamespacedName> {
	private String name;
	private String namespaceUri;
	private String expanded;

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespaceUri;
	}

	public String getExpanded() {
		return expanded;
	}

	public NamespacedName(String name, String namespaceUri) {
		this.name = name;
		this.namespaceUri = namespaceUri;
		this.expanded = namespaceUri + name;
	}

	@Override
	public int hashCode() {
		return getExpanded().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		NamespacedName name = (NamespacedName) obj;
		return getExpanded().equals(name.getExpanded());
	}

	@Override
	public int compareTo(NamespacedName o) {
		return getExpanded().compareTo(o.getExpanded());
	}

	@Override
	public String toString() {
		return getExpanded();
	}

}
