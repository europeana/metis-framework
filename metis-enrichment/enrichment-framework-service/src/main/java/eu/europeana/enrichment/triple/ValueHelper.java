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



public class ValueHelper {
	public static boolean isLiteral(Value value)
	{
		if (value == null) 
			return false;
		return (value instanceof LiteralValue);
	}
	public static boolean isResource(Value value)
	{
		if (value == null) 
			return false;
		return (value instanceof ResourceValue);
	}
	public static String lang(Value value, String prefix, String defaultValue) {
	    String lang = null;
	    if (isLiteral(value)) {
	        lang = ((LiteralValue)value).getLang();
	    }
        return StringUtils.isBlank(lang) ? defaultValue : (prefix + lang); 
	}
}

