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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.enrichment.common.Utils;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.xconverter.api.Graph;

/**
 * The ontological concepts (resources and properties) used by converters.
 * 
 * When you want to use a new property in your conversion results, it makes
 * sense first to check if the desired property is already in this file. If not
 * then you need to create another class, a subclass of this class, and define
 * your property there.
 * 
 * Do not modify this class, but create your own project-specific subclass.
 * 
 * The order in this file does matter, and the parents should be defined earlier
 * than the children.
 * 
 * @author Borys Omelayenko
 * 
 */
public class Concepts {
	Logger log = LoggerFactory.getLogger(getClass().getName());

	public interface Concept {
		String getUri();

		public void defineInRdf(Graph definitions, Graph alignment)
				throws Exception;
	}

	public static Concepts getInstance() {
		return new Concepts();
	}

	/**
	 * RDF and OWL.
	 */
	public static class RDF extends StandardClass {
		public static final Property TYPE = new Property(Namespaces.RDF
				+ "type");
		public static final Property SAMEAS = new Property(Namespaces.OWL
				+ "sameAs");
		public static final Property SUBCLASS = new Property(Namespaces.RDFS
				+ "subClassOf");
		public static final Property SUBPROPERTY = new Property(Namespaces.RDFS
				+ "subPropertyOf");
		public static final Property LABEL = new Property(Namespaces.RDFS
				+ "label");
		public static final Property COMMENT = new Property(Namespaces.RDFS
				+ "comment");
		public static final Property RESOURCE = new Property(Namespaces.RDF
				+ "resource");
		public static final Property PROPERTY = new Property(Namespaces.RDF
				+ "Property");
		public static final Property VALUE = new Property(Namespaces.RDF
				+ "value");
	}

	/**
	 * SKOS.
	 * 
	 */
	public static class SKOS extends StandardClass {
		public static final String CONCEPT = Namespaces.SKOSCORE + "Concept";
		public static final String COLLECTION = Namespaces.SKOSCORE
				+ "Collection";

		public static final Property LABEL_PREFERRED = new Property(
				Namespaces.SKOSCORE + "prefLabel");
		public static final Property LABEL_ALT = new Property(
				Namespaces.SKOSCORE + "altLabel");
		public static final Property BROADER = new Property(Namespaces.SKOSCORE
				+ "broader");
		public static final Property NARROWER = new Property(
				Namespaces.SKOSCORE + "narrower");
		public static final Property RELATED = new Property(Namespaces.SKOSCORE
				+ "related");
		public static final Property DEFINITION = new Property(
				Namespaces.SKOSCORE + "definition");
		public static final Property IN_SCHEME = new Property(
				Namespaces.SKOSCORE + "inScheme");
		public static final Property SCOPE_NOTE = new Property(
				Namespaces.SKOSCORE + "scopeNote");
		public static final Property MEMBER = new Property(Namespaces.SKOSCORE
				+ "member");
		public static final Property NOTE = new Property(Namespaces.SKOSCORE
				+ "note");
		public static final Property NOTATION = new Property(
				Namespaces.SKOSCORE + "notation");

		public static final Property EXACT_MATCH = new Property(
				Namespaces.SKOSCORE + "exactMatch");
	}

	/**
	 * SKOS extension
	 * 
	 */
	public static class SKOS_EXT extends StandardClass {
		public static final Property LABEL_PREFERRED_SINGULAR = new Property(
				Namespaces.SKOSCORE + "prefLabel.Singular");
		public static final Property LABEL_PREFERRED_PLURAL = new Property(
				Namespaces.SKOSCORE + "prefLabel.Plural");
		public static final Property LABEL_ALT_SINGULAR = new Property(
				Namespaces.SKOSCORE + "altLabel.Plural");
		public static final Property LABEL_ALT_PLURAL = new Property(
				Namespaces.SKOSCORE + "altLabel.Plural");
	}

	/**
	 * FOAF.
	 * 
	 */
	public static class FOAF extends StandardClass {
		public static final String AGENT_CLASS = Namespaces.FOAF + "Agent";
		public static final String PERSON_CLASS = Namespaces.FOAF + "Person";
		public static final String ORGAN_CLASS = Namespaces.FOAF
				+ "Organization";
		public static final String GROUP_CLASS = Namespaces.FOAF + "Group";
		public static final Property NAME = new Property(Namespaces.FOAF
				+ "name");
	}

	/**
	 * Dublin Core.
	 * 
	 */
	public static class DC extends StandardClass {
		public static final Property DESCRIPTION = new Property(Namespaces.DC
				+ "description");
		public static final Property LOCATION = new Property(Namespaces.DC
				+ "location");
		public static final Property TITLE = new Property(Namespaces.DC
				+ "title");
		public static final Property DATE = new Property(Namespaces.DC + "date");
		public static final Property CREATOR = new Property(Namespaces.DC
				+ "creator");
		public static final Property PUBLISHER = new Property(Namespaces.DC
				+ "publisher");
		public static final Property SUBJECT = new Property(Namespaces.DC
				+ "subject");
		public static final Property CONTRIBUTOR = new Property(Namespaces.DC
				+ "contributor");
		public static final Property TYPE = new Property(Namespaces.DC + "type");
		public static final Property FORMAT = new Property(Namespaces.DC
				+ "format");
		public static final Property SOURCE = new Property(Namespaces.DC
				+ "source");
		public static final Property LANGUAGE = new Property(Namespaces.DC
				+ "language");
		public static final Property RIGHTS = new Property(Namespaces.DC
				+ "rights");
		public static final Property IDENTIFIER = new Property(Namespaces.DC
				+ "identifier");

	}

	/**
	 * Dublin Core terms.
	 * 
	 */
	public static class DCTEMRS extends StandardClass {
		public static final Property IS_PART_OF = new Property(
				Namespaces.DCTERMS + "isPartOf");
	}

	/**
	 * VRA
	 * 
	 */
	public static class VRA extends StandardClass {
		public static final Property CREATOR = new Property(Namespaces.VRA
				+ "creator");
		public static final Property CREATOR_ATTRIBUTED = new Property(
				Namespaces.VRA + "creator.attributed", CREATOR,
				"attributed creator", "toegeschreven kunstenaar");
		public static final Property TECHNIQUE = new Property(Namespaces.VRA
				+ "technique");
		public static final Property TECHNIQUE_NOTE = new Property(
				Namespaces.VRA + "technique.note", TECHNIQUE, null, null);
		public static final Property MATERIAL = new Property(Namespaces.VRA
				+ "material");
		public static final Property MATERIAL_NOTE = new Property(
				Namespaces.VRA + "material.note", MATERIAL, null, null);
		public static final Property MATERIAL_EN = new Property(Namespaces.VRA
				+ "material@@en");
		public static final Property DESCRIPTION = new Property(Namespaces.VRA
				+ "description");
		public static final Property DATE = new Property(Namespaces.VRA
				+ "date");
		public static final Property DATE_NOTE = new Property(Namespaces.VRA
				+ "date.note", DATE, null, null);
		public static final Property DATE_EARLIEST = new Property(
				Namespaces.VRA + "date.earliestDate", DATE, "earliest date",
				"vroegste datum", null, Property.NO_AAT);
		public static final Property DATE_LATEST = new Property(Namespaces.VRA
				+ "date.latestDate", DATE, "latest date", "laatste datum",
				null, Property.NO_AAT);
		public static final Property DATE_MODIFICATION = new Property(
				Namespaces.VRA + "date.modificationDate", DATE,
				"latest modification date", "laatste modificatie datum", null,
				Property.NO_AAT);

		public static final Property TITLE = new Property(Namespaces.VRA
				+ "title");
		public static final Property TITLE_NOTE = new Property(Namespaces.VRA
				+ "title.note", TITLE, null, null);
		public static final Property TITLE_EN = new Property(Namespaces.VRA
				+ "title@@en");
		public static final Property TITLE_NL = new Property(Namespaces.VRA
				+ "title@@nl");
		public static final Property TITLE_ALT = new Property(Namespaces.VRA
				+ "title.alternative", TITLE, "alternative title",
				"alternatieve titel");

		public static final Property TITLE_SERIES = new Property(Namespaces.VRA
				+ "title.series");
		public static final Property TITLE_BOOK = new Property(Namespaces.VRA
				+ "title.book");

		public static final Property TYPE = new Property(Namespaces.VRA
				+ "type");
		public static final Property TYPE_NOTE = new Property(Namespaces.VRA
				+ "type.note", TYPE, null, null);

		public static final Property SUBJECT = new Property(Namespaces.VRA
				+ "subject");
		public static final Property SUBJECT_NOTE = new Property(Namespaces.VRA
				+ "subject.note", SUBJECT, null, null);
		public static final Property SUBJECT_GEO = new Property(Namespaces.VRA
				+ "subject.geographicPlace", SUBJECT,
				"geographic place of subject",
				"geografische plaats van ontwerp", null, Property.NO_AAT);
		public static final Property SUBJECT_PERSONALNAME = new Property(
				Namespaces.VRA + "subject.personalName", SUBJECT,
				"person name of subject", "persoon naam va ontwerp", null,
				Property.NO_AAT);
		public static final Property SUBJECT_ICONCLASS = new Property(
				Namespaces.VRA + "subject.iconclass", VRA.SUBJECT,
				"subject iconclass", "onderwerp iconclass");

		public static final Property SOURCE = new Property(Namespaces.VRA
				+ "source");
		public static final Property SOURCE_COLL = new Property(Namespaces.VRA
				+ "source.collection", SOURCE, null, null);
		public static final Property WORK = new Property(Namespaces.VRA
				+ "Work");

		public static final Property LOCATION = new Property(Namespaces.VRA
				+ "location");
		public static final Property LOCATION_CREATION = new Property(
				Namespaces.VRA + "location.creationSite", LOCATION, null, null,
				null, Property.NO_AAT);
		public static final Property LOCATION_CURRENT = new Property(
				Namespaces.VRA + "location.currentRepository", LOCATION, null,
				null, null, Property.NO_AAT);

		public static final Property MEASURE = new Property(Namespaces.VRA
				+ "measurements");
		public static final Property MEASUREFMT = new Property(Namespaces.VRA
				+ "measurements.format", MEASURE, null, null, null,
				Property.NO_AAT);
		public static final Property MEASUREDIM = new Property(Namespaces.VRA
				+ "measurements.dimensions", MEASURE, null, null, null,
				Property.NO_AAT);

		public static final Property ID_CURRENT = new Property(Namespaces.VRA
				+ "idNumber.currentRepository");

		public static final Property CULTURE = new Property(Namespaces.VRA
				+ "culture");

		public static final Property RELATION = new Property(Namespaces.VRA
				+ "relation");
		public static final Property RELATION_GEO = new Property(Namespaces.VRA
				+ "relation.geographical", RELATION, null, null, null,
				Property.NO_AAT);
		public static final Property RELATION_PERSON = new Property(
				Namespaces.VRA + "relation.person", RELATION, null, null, null,
				Property.NO_AAT);

		public static final Property DEPICTS = new Property(Namespaces.VRA
				+ "relation.depicts", RELATION, null, null, null,
				Property.NO_AAT);

		public static final Property RIGHT = new Property(Namespaces.VRA
				+ "rights");
		public static final Property COPYRIGHT = new Property(Namespaces.VRA
				+ "rights.copyright", RIGHT, null, null, null, Property.NO_AAT);

		public static final Property PERIOD = new Property(Namespaces.VRA
				+ "stylePeriod");
		public static final Property STYLEPERIOD = new Property(Namespaces.VRA
				+ "stylePeriod.period", PERIOD, null, null, null,
				Property.NO_AAT);

		public static final Property INSCRIPTION = new Property(Namespaces.VRA
				+ "inscription");
	}

	/**
	 * AnnoCultor internal.
	 * 
	 */
	public static class ANNOCULTOR extends StandardClass {
		// each part gets a triple <part_id, PART_SUBJECT_PARENT_ID, parent_id>
		public static final Path PART_TO_PARENT;
		// each part gets a triple <parent_id, PARENT_SUBJECT_PART_ID, part_id>
		public static final Path PARENT_TO_PART;
		static {
			try {
				PART_TO_PARENT = new Path("ac:useParentId", new Namespaces());
				PARENT_TO_PART = new Path("ac:usePartId", new Namespaces());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public static final Property PERIOD_BEGIN = new Property(
				Namespaces.ANNOCULTOR_TIME + "beginPeriod");
		public static final Property PERIOD_END = new Property(
				Namespaces.ANNOCULTOR_TIME + "endPeriod");
		public static final Property DATE_BEGIN = new Property(
				Namespaces.ANNOCULTOR_TIME + "beginDate");
		public static final Property DATE_END = new Property(
				Namespaces.ANNOCULTOR_TIME + "endDate");
	}

	/**
	 * AnnoCultor reporting facilities.
	 */
	public static class REPORTER extends StandardClass {
		public static final Property REPORT_NAME = new Property(
				Namespaces.ANNOCULTOR_REPORT + "name");
		public static final Property REPORT_LABEL = new Property(
				Namespaces.ANNOCULTOR_REPORT + "label");
		public static final Property REPORT_TOTAL_VALUES = new Property(
				Namespaces.ANNOCULTOR_REPORT + "totalValues");
		public static final Property REPORT_ALL_UNIQUE = new Property(
				Namespaces.ANNOCULTOR_REPORT + "allUnique");
		public static final Property REPORT_VALUE = new Property(
				Namespaces.ANNOCULTOR_REPORT + "value");
	}

	protected static List<Concept> conceptClasses = new ArrayList<Concept>();

	protected static class StandardClass {

	}

	/**
	 * Generates an RDF representation of the properties defined in this class.
	 * 
	 * @param concepts
	 * @param definitions
	 *            graph where property definitions will go
	 * @param alignment
	 *            graph where the links between properties and other properties
	 *            will go
	 * @throws Exception
	 */
	public void extractPropertyDefinitions(
	// Concepts concepts,
			Graph definitions, Graph alignment) throws Exception {
		// find all classes, also up in the hierarchy; when loaded their static
		// Property-es are loaded as well
		Class cls = this.getClass();// concepts.getClass();
		while (cls != null) {
			String pkg = cls.getPackage().getName();
			// skip packages that should not contain property defs.
			if (pkg.startsWith("java") || pkg.startsWith("com.sun")
					|| pkg.startsWith("sun"))
				break;
			Utils.getClassesForPackage(pkg);
			cls = cls.getSuperclass();
		}

		for (Property property : Property.getProperties()) {
			property.defineInRdf(definitions, alignment);
		}

		for (Concept concept : conceptClasses) {
			concept.defineInRdf(definitions, alignment);
		}

	}
}
