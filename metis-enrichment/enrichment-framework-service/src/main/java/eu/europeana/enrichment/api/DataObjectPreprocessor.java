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

import eu.europeana.enrichment.xconverter.api.DataObject;

/**
 * Selects and preprocesses values of a data object prior to conversion. This is
 * the proper place to modify object id, set default values, append or replace
 * property values, etc.
 */
public abstract class DataObjectPreprocessor {
	/**
	 * Preprocessor runs between {@linkplain #preCondition(DataObject)} and
	 * {@linkplain #postCondition(DataObject)} right before property rules are
	 * applied to this data object; it is used to set new values into the
	 * <code>dataObject</code> such as generated object id's or other fields
	 * that are easier to compute here and then use by property conversion rules
	 * (override it!).
	 * 
	 * @param dataObject
	 * @return
	 * @throws Exception
	 */
	public void process(DataObject dataObject) throws Exception {
		// default is nothing
	}

	/**
	 * Selector precondition runs before anything is done to this
	 * <code>dataObject</code> and is typically used to filter some records out
	 * based on the values of the <code>dataObject</code> (override it!).
	 * 
	 * @see #process(DataObject)
	 * @see #postCondition(DataObject)
	 * 
	 * @param dataObject
	 * @return
	 * @throws Exception
	 * 
	 */
	public boolean preCondition(DataObject dataObject) throws Exception {
		return true;
	}

	/**
	 * Selector postcondition runs after {@linkplain #preCondition(DataObject)}
	 * and {@linkplain #process(DataObject)} but before property rules are
	 * applied to this data object; it is typically used to filter some records
	 * out based on the values of the <code>dataObject</code> that were changed
	 * by the {@linkplain #process(DataObject)} method (override it!).
	 * 
	 * @see #preCondition(DataObject)
	 * @see #process(DataObject)
	 * 
	 * @param dataObject
	 * @return
	 * @throws Exception
	 */
	public boolean postCondition(DataObject dataObject) throws Exception {
		return true;
	}

}