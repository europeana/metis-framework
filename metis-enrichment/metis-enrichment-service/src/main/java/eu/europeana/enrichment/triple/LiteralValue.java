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

import org.apache.commons.lang.StringUtils;

import eu.europeana.enrichment.api.internal.Language.Lang;

public class LiteralValue extends Value {
	private String value;
	private String lang;

	@Override
	public String getValue() {
		return value;
	}

	public String getLang() {
		return lang;
	}

	public LiteralValue(String value, String lang) {
		this.value = value;
		this.lang = lang;
	}

	public LiteralValue(String value, Lang lang) {
		this.value = value;
		this.lang = lang == null ? null : lang.getCode();
	}

	public LiteralValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LiteralValue other = (LiteralValue) obj;
		if (lang == null) {
			if (other.lang != null)
				return false;
		} else if (!lang.equals(other.lang))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lang == null) ? 0 : lang.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return getValue() + (StringUtils.isBlank(lang) ? "" : ("@" + lang));
	}

}
