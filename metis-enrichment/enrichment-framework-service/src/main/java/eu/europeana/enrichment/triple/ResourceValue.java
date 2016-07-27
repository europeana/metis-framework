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
package eu.europeana.enrichment.triple;

import eu.europeana.enrichment.context.Namespace;

public class ResourceValue extends Value {
	private String value;
	private String namespace;

	@Override
	public String getValue() {
		return namespace + value;
	}

	public String getNamespace() {
		return namespace;
	}

	public ResourceValue(String namespace, String value) {
		this.value = value;
		this.namespace = namespace;
	}

	public ResourceValue(Namespace namespace, String value) {
		this.value = value;
		this.namespace = namespace.getUri();
	}

	public ResourceValue(String value) {
		this.value = value;
		this.namespace = "";
	}

	@Override
	public boolean equals(Object obj) {
		return getValue().equals(obj);
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}

	@Override
	public String toString() {
		return getValue();
	}

}
