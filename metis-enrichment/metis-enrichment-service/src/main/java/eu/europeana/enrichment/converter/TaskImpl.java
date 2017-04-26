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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.europeana.enrichment.api.DataSource;
import eu.europeana.enrichment.api.ObjectRule;
import eu.europeana.enrichment.api.Task;
import eu.europeana.enrichment.context.Namespace;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.path.PathMap;
import eu.europeana.enrichment.path.PathMap.MatchResult;
import eu.europeana.enrichment.xconverter.api.Graph;

/**
 * Conversion task: inputs, outputs, and mappings.
 * 
 * @author Borys Omelayenko
 * 
 */
class TaskImpl implements Task {

	private String datasetId;

	private String datasetDescription;

	private Set<Graph> graphs;

	private PathMap<ObjectRule> mapPathToObjectRule = new PathMap<ObjectRule>();

	private Namespace targetNamespace;

	/**
	 * Access via <code>CoreFactory</code>.
	 * 
	 * @param datasetId
	 * @param subsignature
	 * @param description
	 * @param targetNamespace
	 * @throws Exception
	 */
	TaskImpl(String datasetId, String subsignature, String description,
			Namespace targetNamespace)
			throws Exception {

		this.datasetId = datasetId;
		if (subsignature != null && subsignature.length() > 0)
			this.datasetId += "_" + subsignature;
		if (datasetId.contains("."))
			throw new RuntimeException("Dot is not allowed in task signature "
					+ datasetId);
		this.datasetDescription = description;
		this.targetNamespace = targetNamespace;
		graphs = new HashSet<Graph>();

	}

	public void addGraph(Graph graph) {
		// some graphs are not a result of a conversion task
		if (datasetId != null) {
			if (graphs.contains(this))
				throw new RuntimeException("Coding error: duplicating targets "
						+ graph.getId());
		}
		graphs.add(graph);
	}

	DataSource dataSource;

	@Override
	public void setDataSource(DataSource dataSource) throws IOException {
		this.dataSource = dataSource;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void addPartListener(ObjectRule map) {
		mapPathToObjectRule.put(map.getRecordSeparatingPath(), map);
	}

	public List<ObjectRule> getObjectRules() {
		List<ObjectRule> result = new ArrayList<ObjectRule>();
		for (Path path : mapPathToObjectRule.keySet()) {
			result.add(mapPathToObjectRule.get(path));
		}
		return result;
	}

	public List<ObjectRule> getRuleForSourcePath(Path data) throws Exception {
		List<ObjectRule> result = new ArrayList<ObjectRule>();
		for (MatchResult<ObjectRule> match : mapPathToObjectRule.answer(data)) {
			if (match.getAttributeValue() != null)
				throw new RuntimeException(
						"Unacceptable Path for storing rules: " + data);
			result.add(match.getStoredObject());
		}
		;

		return result;
	}

	public Set<Graph> getGraphs() {
		return graphs;
	}

	public String getDatasetId() {
		return datasetId;
	}

	public String getDatasetURI() {
		return targetNamespace + datasetId;
	}

	public Namespace getTargetNamespace() {
		return targetNamespace;
	}

	public String getDatasetDescription() {
		return datasetDescription;
	}


}
