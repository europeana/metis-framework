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

import java.io.IOException;
import java.util.List;
import java.util.Set;

import eu.europeana.enrichment.context.Namespace;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.xconverter.api.Graph;

/**
 * Conversion task.
 * 
 * @author Borys Omelayenko
 * 
 */
public interface Task {

	/**
	 * Source files to process.
	 */
	public abstract void setDataSource(DataSource dataSource)
			throws IOException;

	public abstract DataSource getDataSource();

	/**
	 * Should not be called by custom code. A part is an XML tag that is
	 * repeating in a record and has subtags that need to be processed in the
	 * context of this (repeating) tag. A typical example - multiple tags
	 * <code>title</code> where each may contain two subtags: <code>title</code>
	 * with the actual text, and <code>titleType</code> that indicates the kind
	 * of title.
	 * 
	 * The root element of an XML file may contain the parts, where each part
	 * corresponds to a record (where record properties, in turn, are processed
	 * within the context of the record). Thus, there is no way to escape
	 * creating a part.
	 * 
	 * 
	 */
	public abstract void addPartListener(ObjectRule map);

	/**
	 * Named graphs expected to be populated during conversion.
	 * 
	 * @return
	 */
	public abstract Set<Graph> getGraphs();

	/**
	 * Named graph expected to be populated during conversion.
	 * 
	 * @param graph
	 */
	public abstract void addGraph(Graph graph);

	public abstract String getDatasetId();

	/**
	 * Dataset id prefixed with a namespace.
	 * 
	 * @return
	 */
	public abstract String getDatasetURI();

	public abstract Namespace getTargetNamespace();

	public abstract String getDatasetDescription();

	/**
	 * Returns a rule that converts a source XML path, should not be used in
	 * custom converters.
	 * 
	 * @param path
	 * @return
	 */
	public List<ObjectRule> getRuleForSourcePath(Path data) throws Exception;

	public List<ObjectRule> getObjectRules();


}