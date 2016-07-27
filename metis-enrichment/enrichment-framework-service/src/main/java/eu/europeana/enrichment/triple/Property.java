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

import java.util.ArrayList;
import java.util.List;

import eu.europeana.enrichment.api.internal.Language.Lang;
import eu.europeana.enrichment.context.Concepts.Concept;
import eu.europeana.enrichment.context.Concepts.RDF;
import eu.europeana.enrichment.context.Concepts.SKOS;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.xconverter.api.Graph;

/**
 * (RDF) Property.
 * 
 * @author Borys Omelayenko
 * 
 */
public class Property implements Concept {
	private static List<Property> properties = new ArrayList<Property>();

	public static List<Property> getProperties() {
		return properties;
	}

	public static final Property NO_PARENT = null;
	public static final String NO_AAT = null;

	private String uri;

	private String comment;

	private String enLabel;

	private String nlLabel;

	private String aatCode;

	private Property parent;

	private String tag;

	/**
	 * Property wrap-up created for XML paths when they are passed to converter.
	 * Should be never called by users.
	 */
	public Property(Path path) {
		this.uri = path.getPath();
		this.parent = NO_PARENT;
		this.comment = null;
		this.enLabel = null;
		this.nlLabel = null;
		this.aatCode = NO_AAT;
	}

	public Property(String uri, Property parent, String enLabel,
			String nlLabel, String comment, String aatCode) {
		this.uri = uri;
		this.parent = parent;
		this.comment = comment;
		this.enLabel = enLabel;
		this.nlLabel = nlLabel;
		this.aatCode = aatCode;
		properties.add(this);
	}

	@Deprecated
	public Property(String uri, Property parent, String tag) {
		this(uri, parent, null, null, null, NO_AAT);
		this.tag = tag;
	}

	public Property(String uri, Property parent) {
		this(uri, parent, null, null, null, NO_AAT);
	}

	public Property(String uri, Property parent, String enLabel, String nlLabel) {
		this(uri, parent, enLabel, nlLabel, null, NO_AAT);
	}

	public Property(String uri) {
		this(uri, NO_PARENT, null, null, null, NO_AAT);
	}

	public Property(String uri, Lang lang) {
		this(uri + lang == null ? "" : lang.getCode());
	}

	@Override
	public String toString() {
		// dependency below
		return uri;
	}

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Property) {
			return uri.equals(obj.toString());
		}
		return false;
	}

	public void defineInRdf(Graph definitions, Graph alignment)
			throws Exception {
		if (comment != null)
			definitions.add(new Triple(uri, RDF.COMMENT, new LiteralValue(
					comment), null));
		if (parent != null)
			alignment.add(new Triple(uri, RDF.SUBPROPERTY, new ResourceValue(
					parent.uri), null));
		if (enLabel != null)
			definitions.add(new Triple(uri, SKOS.LABEL_PREFERRED,
					new LiteralValue(enLabel), null));
		if (nlLabel != null)
			definitions.add(new Triple(uri, SKOS.LABEL_PREFERRED,
					new LiteralValue(nlLabel), null));
		if (aatCode != null)
			alignment.add(new Triple(uri, SKOS.RELATED, new ResourceValue(
					aatCode), null));

		definitions.add(new Triple(uri, RDF.TYPE, new ResourceValue(
				RDF.PROPERTY.getUri()), null));
	}

	public String getUri() {
		return uri;
	}

	public String getComment() {
		return comment;
	}

	public String getEnLabel() {
		return enLabel;
	}

	public String getNlLabel() {
		return nlLabel;
	}

	public String getAatCode() {
		return aatCode;
	}

	public Property getParent() {
		return parent;
	}

	public String getTag() {
		return tag;
	}

}
