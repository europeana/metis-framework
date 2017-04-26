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
package eu.europeana.enrichment.converters.time;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.europeana.enrichment.api.internal.Language.Lang;
import eu.europeana.enrichment.context.Concepts;
import eu.europeana.enrichment.context.Namespaces;
import eu.europeana.enrichment.context.Concepts.ANNOCULTOR;
import eu.europeana.enrichment.context.Concepts.RDF;
import eu.europeana.enrichment.context.Concepts.SKOS;
import eu.europeana.enrichment.reports.parts.ReportList;
import eu.europeana.enrichment.triple.LiteralValue;
import eu.europeana.enrichment.triple.ResourceValue;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.utils.SesameWriter;

/**
 * Trunk of the time ontology.
 * 
 * @author Borys Omelayenko
 * 
 */
public class GenerateTimeOntologyTrunk {

	private static final String NS_TIME = "http://annocultor.eu/time/";
	private static final String NS_ROOT = "ChronologicalPeriod";

	public static ReportList<LabelTemplate> bc;
	public static ReportList<LabelTemplate> ad;
	public static ReportList<LabelTemplate> centuryTemplates;
	public static ReportList<LabelTemplate> halfsTemplates;
	public static ReportList<LabelTemplate> thirdsTemplates;
	public static ReportList<LabelTemplate> quartersTemplates;
	public static ReportList<LabelTemplate> decadesTemplates;

	public static void main(String[] args) throws Exception {
		bc = new ReportList<LabelTemplate>(new File("."), "milleniums-bc.xml",
				10000);
		bc.load();
		ad = new ReportList<LabelTemplate>(new File("."), "milleniums-ad.xml",
				10000);
		ad.load();
		centuryTemplates = new ReportList<LabelTemplate>(new File("."),
				"centuries.xml", 10000);
		centuryTemplates.load();
		halfsTemplates = new ReportList<LabelTemplate>(new File("."),
				"halfs.xml", 10000);
		halfsTemplates.load();
		thirdsTemplates = new ReportList<LabelTemplate>(new File("."),
				"thirds.xml", 10000);
		thirdsTemplates.load();
		quartersTemplates = new ReportList<LabelTemplate>(new File("."),
				"quarters.xml", 10000);
		quartersTemplates.load();
		decadesTemplates = new ReportList<LabelTemplate>(new File("."),
				"decades.xml", 10000);
		decadesTemplates.load();

		Namespaces namespaces = new Namespaces();
		SesameWriter centuries = SesameWriter
				.createRDFXMLWriter(
						new File("time.periods.rdf"),
						namespaces,
						"time",
						"AnnoCultor time ontology, generated trunk: milleniums, centuries, decades",
						1024, 1024);
		centuries.startRDF();
		SesameWriter years = SesameWriter.createRDFXMLWriter(new File(
				"time.years.rdf"), namespaces, "time",
				"AnnoCultor time ontology, generated trunk: years", 1024, 1024);
		years.startRDF();

		List<LiteralValue> altLabels = new ArrayList<LiteralValue>();
		// milleniums
		for (int mil = 4; mil > 0; mil--) {
			altLabels.clear();
			for (LabelTemplate template : bc) {
				altLabels.add(applyTemplate(mil, -1, template));
			}
			write(centuries, "BC" + mil + "xxx", list(NS_ROOT), altLabels);
		}
		for (int mil = 1; mil < 4; mil++) {
			altLabels.clear();
			for (LabelTemplate template : ad) {
				altLabels.add(applyTemplate(mil, -1, template));
			}
			write(centuries, "AD" + mil + "xxx", list(NS_ROOT), altLabels);
		}
		// centuries
		for (int century = 1; century < 22; century++) {
			boolean first = century < 10;
			String centStub = (first ? "0" : "") + (century - 1);
			String cent = centStub + "xx";
			// instantiate patterns
			altLabels.clear();
			for (LabelTemplate template : centuryTemplates) {
				if (century > 10 || template.getPattern_N().length() > 5) {
					altLabels.add(applyTemplate(century, -1, template));
				}
			}
			if (century > 10) {
				altLabels.add(new LiteralValue("" + century + "??"));
				altLabels.add(new LiteralValue("" + century + ".."));
			} else {
				// altLabels.add(new LiteralValue("" + centStub + "00"));
			}
			write(centuries, cent, list("AD" + (first ? "1" : "2") + "xxx"),
					altLabels);
			int centuryStartYear = (century - 1) * 100 + 1;
			writeBeginEndDates(centuries, cent, centuryStartYear, century * 100);

			// years
			for (int yearThisCentury = 0; yearThisCentury < 100; yearThisCentury++) {

				// build period numbers
				int year = (century - 1) * 100 + yearThisCentury;
				int half = yearThisCentury < 50 ? 1 : 2;
				int third = yearThisCentury < 33 ? 1
						: (yearThisCentury < 66 ? 2 : 3);
				int quart = yearThisCentury < 25 ? 1
						: (yearThisCentury < 50 ? 2 : (yearThisCentury < 75 ? 3
								: 4));
				int decade = yearThisCentury / 10;

				boolean showYear = year != 0;
				boolean showHalf = yearThisCentury == 0
						|| yearThisCentury == 50;
				boolean showThird = yearThisCentury == 0
						|| yearThisCentury == 33 || yearThisCentury == 66;
				boolean showQuart = yearThisCentury == 0
						|| yearThisCentury == 25 || yearThisCentury == 50
						|| yearThisCentury == 75;
				boolean showDecade = yearThisCentury % 10 == 0;

				// build identifiers
				String halfId = cent + "_" + half + "_half";
				String thirdId = cent + "_" + third + "_third";
				String quartId = cent + "_" + quart + "_quarter";
				String decadeId = centStub + decade + "x";

				// build labels

				// half-centuries
				if (showHalf) {
					altLabels.clear();
					for (LabelTemplate template : halfsTemplates) {
						altLabels.add(applyTemplate(half, century, template));
					}
					write(centuries, halfId, list(cent), altLabels);
					writeBeginEndDates(centuries, halfId, centuryStartYear
							+ (half - 1) * 50, centuryStartYear + half * 50);
				}

				// thirds
				if (showThird) {
					altLabels.clear();
					for (LabelTemplate template : thirdsTemplates) {
						altLabels.add(applyTemplate(third, century, template));
					}
					write(centuries, thirdId, list(cent), altLabels);
					writeBeginEndDates(centuries, thirdId, centuryStartYear
							+ (third - 1) * 33, centuryStartYear + third * 33
							- 1 + (third / 3));
				}

				// quarts
				if (showQuart) {
					altLabels.clear();
					for (LabelTemplate template : quartersTemplates) {
						altLabels.add(applyTemplate(quart, century, template));
					}
					write(centuries, quartId, list(cent, halfId), altLabels);
					writeBeginEndDates(centuries, quartId, centuryStartYear
							+ (quart - 1) * 25, centuryStartYear + quart * 25
							- 1);
				}

				// decades
				if (showDecade) {
					altLabels.clear();
					for (LabelTemplate template : decadesTemplates) {
						altLabels.add(applyTemplate(decade + 1, century,
								template));
					}
					if (century > 10) {
						altLabels.add(new LiteralValue(
								((century - 1) * 10 + decade) + "x"));
						altLabels.add(new LiteralValue(
								((century - 1) * 10 + decade) + "?"));
						altLabels.add(new LiteralValue(
								((century - 1) * 10 + decade) + "0s"));
					}
					write(centuries, decadeId, list(cent, halfId), altLabels);
					writeBeginEndDates(centuries, decadeId, centuryStartYear
							+ decade * 10, centuryStartYear + (decade + 1) * 10
							- 1);
				}

				// years
				if (showYear) {
					String zeroed = (yearThisCentury < 10 ? "0" : "")
							+ yearThisCentury;
					altLabels.clear();
					altLabels.add(new LiteralValue("" + year));
					String yearId = centStub + zeroed;
					List<String> parents = list(cent, halfId, thirdId, quartId,
							decadeId);
					write(years, yearId, parents, altLabels);
					writeBeginEndDates(years, yearId, year, year);
				}
			}
		}
		centuries.endRDF();
		years.endRDF();
	}

	private static List<String> list(String... items) {
		List<String> list = new ArrayList<String>();
		for (String item : items) {
			list.add(item);
		}
		return list;
	}

	private static LiteralValue applyTemplate(int part, int whole,
			LabelTemplate template) {
		String activeTemplate = template.getPattern_N();
		if (part == 1 || part == 21) {
			activeTemplate = template.getPattern_1();
		}
		if (part == 2 || part == 22) {
			activeTemplate = template.getPattern_2();
		}
		if (part == 3) {
			activeTemplate = template.getPattern_3();
		}
		return new LiteralValue((whole > 0 ? String.format(activeTemplate,
				part, whole) : String.format(activeTemplate, part)), template
				.getLang().isEmpty() ? null : Lang.valueOf(template.getLang()));

	}

	public static void write(SesameWriter writer, String id,
			List<String> broader, List<LiteralValue> prefLabel)
			throws Exception {

		for (String broaderUri : broader) {
			writer.handleTriple(new Triple(NS_TIME + id,
					Concepts.DCTEMRS.IS_PART_OF, new ResourceValue(NS_TIME
							+ broaderUri), null));
		}
		for (LiteralValue literalValue : prefLabel) {
			writer.handleTriple(new Triple(NS_TIME + id, SKOS.LABEL_PREFERRED,
					literalValue, null));
		}
		writer.handleTriple(new Triple(NS_TIME + id, RDF.TYPE,
				new ResourceValue(NS_TIME + "Period"), null));
	}

	public static void writeBeginEndDates(SesameWriter writer, String id,
			int begin, int end) throws Exception {

		writer.handleTriple(new Triple(NS_TIME + id, ANNOCULTOR.DATE_BEGIN,
				new LiteralValue("" + begin + "-01-01"), null));
		writer.handleTriple(new Triple(NS_TIME + id, ANNOCULTOR.DATE_END,
				new LiteralValue("" + end + "-12-31"), null));
	}
}
