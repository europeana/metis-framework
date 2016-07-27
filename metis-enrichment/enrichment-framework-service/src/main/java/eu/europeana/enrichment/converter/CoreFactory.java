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
package eu.europeana.enrichment.converter;

import eu.europeana.enrichment.api.Task;
import eu.europeana.enrichment.context.Namespace;

/**
 * Core factory.
 * 
 * @author Borys Omelayenko
 * 
 */
public class CoreFactory {

	/**
	 * Makes a new conversion task.
	 * 
	 * @param signature
	 *            signature of the dataset, e.g. a museum name.
	 * @param subsignature
	 *            subsignature of the dataset, e.g. <code>works</code>,
	 *            <code>terms</code>, <code>artists</code>.
	 * @param description
	 *            description of the dataset that will appear in the output RDF
	 *            files
	 * @param targetNamespace
	 *            namespace of the dataset objects to appear in the output RDF
	 *            files
	 * @throws Exception
	 */

	public static Task makeTask(String signature, String subsignature,
			String description, Namespace targetNamespace) throws Exception {
		return new TaskImpl(signature, subsignature, description,
				targetNamespace);
	}

}
