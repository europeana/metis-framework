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
package eu.europeana.enrichment.api;

import eu.europeana.enrichment.api.internal.Language.Lang;

/**
 * Label with a language.
 */
public class Label {
	private String label;
	private Lang lang;

	public Label(String label, Lang lang) {
		super();
		this.label = label;
		this.lang = lang;
	}

	public String getLabel() {
		return label;
	}

	public Lang getLang() {
		return lang;
	}

}