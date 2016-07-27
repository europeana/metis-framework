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
package eu.europeana.enrichment.tagger.vocabularies;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import eu.europeana.enrichment.api.Common;
import eu.europeana.enrichment.api.internal.CodeURI;
import eu.europeana.enrichment.api.internal.Language.Lang;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.Value;
import eu.europeana.enrichment.xconverter.api.DataObject;
import eu.europeana.enrichment.xconverter.api.DataObject.ListOfValues;

/**
 * Disambiguation context for vocabulary lookup.
 * 
 * @author Borys Omelayenko
 * 
 */
public class DisambiguationContext {

	public static DisambiguationContext NO_DISAMBIGUATION = null;

	private Collection<CodeURI> parents;

	private Lang lang;

	private Lang defaultLang;

	List<Path> pathToParent = new ArrayList<Path>();

	public DisambiguationContext(Lang lang, Lang defaultLang,
			Collection<CodeURI> parents) {
		this.parents = parents;
		this.lang = lang;
		this.defaultLang = lang;
	}

	public DisambiguationContext(String... parents)
			throws MalformedURLException {
		this(null, null, CodeURI.stringToCodeURI(parents));
	}

	/**
	 * Set context source path, context value will depend on the current record.
	 * Multiple values are treated as union.
	 * 
	 * @param pathToParent
	 *            source path
	 */
	public DisambiguationContext(Path... pathToParent) {
		this.pathToParent = Arrays.asList(pathToParent);
	}

	public List<Path> getPathToParent() {
		return pathToParent;
	}

	public static List<CodeURI> getContextValue(DisambiguationContext context,
			DataObject dataObject) throws Exception {
		List<CodeURI> result = new ArrayList<CodeURI>();
		if (context != null) {
			// constants
			result.addAll(context.getParents());
			// variables
			for (Path contextPath : context.getPathToParent()) {
				ListOfValues values = dataObject.getValues(contextPath);
				for (Value value : values) {
					result.add(new CodeURI(value.getValue()));
				}
			}
		}
		return result;
	}

	public Collection<CodeURI> getParents() {
		return parents;
	}

	public Lang getLang() {
		return lang;
	}

	public Lang getDefaultLang() {
		return defaultLang;
	}

	@Override
	public String toString() {
		return "DisambiguationContext [defaultLang=" + defaultLang + ", lang="
				+ lang + ", parents=" + parents + ", pathToParent="
				+ pathToParent + "]";
	}

}
