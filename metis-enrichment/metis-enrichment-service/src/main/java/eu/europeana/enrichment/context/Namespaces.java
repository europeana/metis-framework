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
package eu.europeana.enrichment.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Standard namespaces.
 * 
 * Do not modify this class, but create your own project-specific subclass.
 * 
 * @author Borys Omelayenko
 * 
 */
public class Namespaces {

	private static Set<Namespace> staticNamespaces = new HashSet<Namespace>();

	// standard Namespaces

	public static final Namespace XML = Namespaces.addNamespace(
			"http://www.w3.org/XML/1998/namespace", "xml", true);
	public static final Namespace RDFS = Namespaces.addNamespace(
			"http://www.w3.org/2000/01/rdf-schema#", "rdfs", true);
	public static final Namespace RDF = Namespaces.addNamespace(
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf", true);
	public static final Namespace OWL = Namespaces.addNamespace(
			"http://www.w3.org/2002/07/owl#", "owl", true);
	public static final Namespace DC = Namespaces.addNamespace(
			"http://purl.org/dc/elements/1.1/", "dc", true);
	public static final Namespace DCTERMS = Namespaces.addNamespace(
			"http://purl.org/dc/terms/", "dcterms", true);
	public static final Namespace SKOSCORE = Namespaces.addNamespace(
			"http://www.w3.org/2004/02/skos/core#", "skos", true);
	public static final Namespace FOAF = Namespaces.addNamespace(
			"http://xmlns.com/foaf/0.1/", "foaf", true);
	public static final Namespace LIB = Namespaces.addNamespace(
			"http://www.swi-prolog.org/rdf/library/", "lib", true);
	public static final Namespace ANNOCULTOR_REPORT = Namespaces.addNamespace(
			"http://annocultor.eu/report/", "ac_report", true);
	public static final Namespace ANNOCULTOR_TIME = Namespaces.addNamespace(
			"http://annocultor.eu/time/", "ac_time", true);
	public static final Namespace ANNOCULTOR_PEOPLE = Namespaces.addNamespace(
			"http://annocultor.eu/people/", "ac_people", true);

	public static final Namespace ANNOCULTOR_CONVERTER = Namespaces
			.addNamespace("http://annocultor.eu/converter/", "ac", true);

	public static final Namespace VRA = Namespaces.addNamespace(
			"http://www.vraweb.org/vracore/vracore3#", "vra", true);
	public static final Namespace OAI = Namespaces.addNamespace(
			"http://www.openarchives.org/OAI/2.0/", "oai", true);
	public static final Namespace OAI_DC = Namespaces.addNamespace(
			"http://www.openarchives.org/OAI/2.0/oai_dc/", "oaidc", true);

	public static final Namespace NS = Namespaces.addNamespace(
			"http://localhost/namespace", "lh", false);
	public static final Namespace EMPTY_NS = Namespaces.addNamespace("",
			"empty", false);

	// real storage: <url, nick>
	private HashMap<String, String> namespaces = new HashMap<String, String>();

	public Set<String> listAllUris() {
		return namespaces.keySet();
	}

	public HashMap<String, String> getNamespaces() {
		return namespaces;
	}

	public String getNick(String uri) {
		return namespaces.get(uri);
	}

	public String getUri(String nick) {
		for (String uri : listAllUris()) {
			if (getNick(uri).equals(nick)) {
				return uri;
			}
		}
		return null;
	}

	/**
	 * A namespace with its uri and a nick.
	 * 
	 * @param url
	 *            full url
	 * @param nick
	 *            short name
	 */
	public void addNamespace(String uri, String nick) {
		String existingNick = getNick(uri);
		if (existingNick == null) {
			// new namespace
			namespaces.put(uri, nick);
		} else {
			// existing
			if (existingNick.equals(nick)) {
				return;
			} else {
				throw new RuntimeException("Duplicating namespace " + nick
						+ " (" + uri
						+ "). Namespace with the same URI but nick "
						+ existingNick + " already exists.");
			}
		}
	}

	private static Namespace addNamespace(String uri, String nick,
			boolean printable) {
		Namespace ns = new Namespace(uri, nick, printable);
		if (staticNamespaces.contains(ns))
			throw new RuntimeException("Duplicating namespace " + uri);
		if (printable) {
			staticNamespaces.add(ns);
		}
		return ns;
	}

	public String addNamespace(String uri) {
		if (namespaces.containsKey(uri))
			throw new RuntimeException("Duplicating namespace " + uri);

		String nick = generateNick();
		addNamespace(uri, nick);
		return nick;
	}

	private int nickCounter = 1;

	private String generateNick() {
		return "ns" + nickCounter++;
	}

	public Namespaces() {
		for (Namespace ns : staticNamespaces) {
			this.addNamespace(ns.getUri(), ns.getNick());
		}
	}

	public String makeQualifiedName(String unqualifiedUri) {
		String nick = getNick(unqualifiedUri);
		String prefixBehindNick = getUri(nick);
		if (StringUtils.isBlank(prefixBehindNick)) {
			return null;
		}
		String qualifiedName = StringUtils.substring(unqualifiedUri,
				prefixBehindNick.length());
		return qualifiedName;
	}
}
