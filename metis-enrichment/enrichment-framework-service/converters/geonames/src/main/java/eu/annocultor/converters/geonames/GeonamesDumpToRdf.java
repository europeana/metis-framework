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
package eu.annocultor.converters.geonames;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 * Text-level parser of the Geonames file. 
 * In this file 6.5 mln small RDF files, one per feature (this is how they call their geo-objects)
 * are merged in a single txt file, separated with the id of the feature 
 * described in each small file.
 * 
 * @author Borys Omelayenko
 *
 */
public class GeonamesDumpToRdf 
{

	public static Properties countryToContinent = new Properties();

	public static void main(String[] args) 
	throws Exception
	{		
		File root = new File("input_source");

		// load country-continent match
		countryToContinent.load((new GeonamesDumpToRdf()).getClass().getResourceAsStream("/country-to-continent.properties"));

		// creating files
		Map<String,BufferedWriter> files = new HashMap<String, BufferedWriter>();
		Map<String,Boolean> started = new HashMap<String, Boolean>();

		for (Object string : countryToContinent.keySet())
		{
			String continent = countryToContinent.getProperty(string.toString());
			File dir = new File(root, continent);
			if (!dir.exists()) {
				dir.mkdir();
			}
			files.put(string.toString(), 
					new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(
											new File (root, continent + "/" + string + ".rdf")
									),
									"UTF-8"
							)
					)
			);
			System.out.println(continent + "/" + string + ".rdf");
			started.put(string.toString(), false);
		}

		System.out.println(started);

		Pattern countryPattern = Pattern.compile("<inCountry rdf\\:resource\\=\"http\\://www\\.geonames\\.org/countries/\\#(\\w\\w)\"/>");
		long counter = 0;
		LineIterator it = FileUtils.lineIterator(new File(root, "all-geonames-rdf.txt"), "UTF-8");
		try {
			while (it.hasNext()) {
				String text = it.nextLine();
				if (text.startsWith("http://sws.geonames"))
					continue;

				// progress
				counter ++;
				if (counter % 100000 == 0) {
					System.out.print("*");
				}
				//			System.out.println(counter);
				// get country
				String country = null;
				Matcher matcher = countryPattern.matcher(text);
				if (matcher.find())
				{
					country = matcher.group(1);
				}
				//			System.out.println(country);
				if (country == null)
					country = "null";
				text = text.replace(
						"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><rdf:RDF",
						"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><rdf:RDF"
				);
				if (started.get(country) == null)
					throw new Exception("Unknow country " + country);
				if (started.get(country).booleanValue())
				{
					// remove RDF opening
					text = text.substring(text.indexOf("<rdf:RDF "));
					text = text.substring(text.indexOf(">") + 1);
				}
				// remove RDF ending
				text = text.substring(0, text.indexOf("</rdf:RDF>"));
				files.get(country).append(text + "\n");
				if (!started.get(country).booleanValue()) {
					// System.out.println("Started with country " + country);
				}
				started.put(country, true);
			}
		} finally {
			LineIterator.closeQuietly(it);
		}

		for (Object string : countryToContinent.keySet())
		{
			boolean hasStarted = started.get(string.toString()).booleanValue();
			if (hasStarted) {
				BufferedWriter bf = files.get(string.toString());
				bf.append("</rdf:RDF>");
				bf.flush();
				bf.close();
			}
		}
		return;
	}

}
