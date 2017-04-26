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
package eu.europeana.enrichment.utils;

import info.aduna.xml.XMLUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

import eu.europeana.enrichment.context.Namespaces;
import eu.europeana.enrichment.triple.LiteralValue;
import eu.europeana.enrichment.triple.Triple;

/**
 * Utilities to work with <a href="http://openrdf.org">Sesame</a> .
 * 
 * @author Borys Omelayenko
 * 
 */
public class SesameWriter extends RDFXMLWriter {

	public static void checkPredicateUri(String predString) throws Exception {
		int predSplitIdx = XMLUtil.findURISplitIndex(predString);
		if (predSplitIdx == -1) {
			throw new RDFHandlerException(
					"Unable to create XML namespace-qualified name for predicate: "
							+ predString);
		}
	}

	public static SesameWriter createRDFXMLWriter(File fn,
			Namespaces namespaces, String id, String description,
			int bufferInKB, int bufferInTriples, String... comment)
			throws Exception {
		fn.delete();
		OutputStream out;
		try {
			out = new BufferedOutputStream(new FileOutputStream(fn, true),
					1024 * bufferInKB);
		} catch (Exception e) {
			throw new Exception("file " + fn.getCanonicalPath(), e);
		}

		SesameWriter writer = new SesameWriter(out, bufferInTriples);

		// sort ns
		List<String> sortedNs = new LinkedList<String>();

		for (String uri : namespaces.listAllUris()) {
			sortedNs.add(namespaces.getNick(uri) + "=" + uri);
		}
		Collections.sort(sortedNs);
		for (String ns : sortedNs) {
			String[] nss = ns.split("=");
			writer.handleNamespace(nss[0], nss[1]);
		}

		List<String> comments = new ArrayList<String>();
		comments.add("");
		comments.add("**********************************************************");
		comments.add("This file is a result of conversion to RDF performed using");
		comments.add("AnnoCultor software, available from   http://AnnoCultor.eu");
		comments.add("**********************************************************");
		comments.add("Named graph: " + id);
		comments.add("Description: " + description);
		comments.add("");
		comments.addAll(Arrays.asList(comment));

		String wholeComment = "";
		for (String c : comments) {
			wholeComment += c + "\n";
		}
		writer.handleComment(wholeComment);
		return writer;
	}

	int bufferInTriples;

	public SesameWriter(OutputStream out, int bufferInTriples) {
		super(out);
		this.bufferInTriples = bufferInTriples;
	}

	List<Triple> pendingStatements = new LinkedList<Triple>();
	int resources = 0;

	@Override
	public void handleComment(String arg0) throws RDFHandlerException {
		flushStatements();
		super.handleComment(arg0);
	}

	public void handleTriple(Triple triple) throws RDFHandlerException {
		if (pendingStatements.isEmpty()) {
			pendingStatements.add(triple);
		} else {
			if (!pendingStatements.get(0).getSubject()
					.equals(triple.getSubject()))
				resources++;

			if (pendingStatements.size() > bufferInTriples
					|| !pendingStatements.get(0).getSubject()
							.equals(triple.getSubject())) {
				flushStatements();
			}
			pendingStatements.add(triple);
		}
	}

	private void flushStatements() throws RDFHandlerException {
		// sort statements by subject-predicate-object
		Collections.sort(pendingStatements, new Comparator<Triple>() {

			public int compare(Triple left, Triple right) {
				// sort by subject
				int result = left.getSubject().compareTo(right.getSubject());
				if (result == 0) {
					// sort by property within a subject, based on namespace
					// nicks
					result = left.getProperty().toString()
							.compareTo(right.getProperty().toString());
					if (result == 0) {
						// sort by object within the same subject and property
						result = left.getValue().getValue()
								.compareTo(right.getValue().getValue());
					}
				}
				return result;
			}

		});

		for (Triple triple : pendingStatements) {
			// determine triple type: literal or resource
			Value value = null;
			if (triple.getValue() instanceof LiteralValue) {
				LiteralValue literalValue = (LiteralValue) triple.getValue();
				value = new LiteralImpl(literalValue.getValue(),
						literalValue.getLang());
			} else {
				value = new URIImpl(triple.getValue().getValue());
			}
			// write triple
			handleStatement(new StatementImpl(new URIImpl(triple.getSubject()),
					new URIImpl(triple.getProperty().getUri()), value));
			// optionally write comment
			if (triple.getComment() != null && !triple.getComment().isEmpty()) {
				super.handleComment(triple.getComment());
			}
		}

		pendingStatements.clear();

	}

	@Override
	public void endRDF() throws RDFHandlerException {
		flushStatements();
		this.handleComment("Resources: "
				+ resources
				+ " \n(if a resource is described in two XML elements rdf:Descriptions then it will be counted twice)");
		super.endRDF();
		try {
			this.writer.close();
		} catch (Exception e) {
			throw new RDFHandlerException(e);
		}
	}

}
