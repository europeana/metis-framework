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
package eu.europeana.enrichment.api.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Representation of a term code as an URI.
 * 
 * @author Borys Omelayenko
 * 
 */
public class CodeURI {
	private String uri;

	/**
	 * Create a code from a Java uri. In fact, we assume it to be an URL.
	 * 
	 * @param uri
	 */
	public CodeURI(String uri) throws MalformedURLException {
		this.uri = new URL(uri).toString();
	}

	@Override
	public String toString() {
		return uri;
	}

	/**
	 * Create a Java URI (slow).
	 * 
	 * @return
	 */
	public String getUri() {
		return uri;
	}

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CodeURI other = (CodeURI) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	public static Collection<CodeURI> stringToCodeURI(String[] parents)
			throws MalformedURLException {
		Collection<CodeURI> result = new ArrayList<CodeURI>();
		for (String uri : parents) {
			result.add(new CodeURI(uri));
		}
		return result;
	}
}
