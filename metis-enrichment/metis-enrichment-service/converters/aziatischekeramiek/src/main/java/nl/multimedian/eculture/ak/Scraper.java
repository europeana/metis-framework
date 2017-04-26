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
package nl.multimedian.eculture.ak;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import nl.multimedian.eculture.annocultor.xconverter.api.Environment;
import nl.multimedian.eculture.annocultor.xconverter.api.Environment.PARAMETERS;
import nl.multimedian.eculture.annocultor.xconverter.impl.XmlElementForVelocity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Borys Omelayenko
 *
 */
public class Scraper
{
	
	/*
	 * Downloading aziatischekeramiek data from their web site and saving it as a single XML document.
	 * 
	 */
	public static void main(String ... params)
	throws Exception
	{
		Environment environment = new Environment();

		File scrapedXmlFile = new File(environment.getParameter(PARAMETERS.inputDir), "ak.xml");
		if (!scrapedXmlFile.exists())
		{
			// target doc
			Document trgDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element trgElement = trgDoc.createElement("records");
			trgDoc.appendChild(trgElement);

			// src list
			DOMParser p = new DOMParser();
			p.parse(new InputSource(new URL("http://www.aziatischekeramiek.nl/get?site=ak&id=i000077&types=object").openStream()));
			Map<String, String> namespaces = new HashMap<String, String>();
			namespaces.put("ak", "http://www.sitemaps.org/schemas/sitemap/0.9");
			XmlElementForVelocity root = new XmlElementForVelocity(p.getDocument().getDocumentElement(), namespaces);
			XmlElementForVelocity[] links = root.getChildren("url");
			for (int i = 0; i < links.length; i++)
			{					
				String url = links[i].getFirstChild("loc").getValue();
				String id = url.substring(url.lastIndexOf("&") + "&id=".length());
				url += "&version=xml";
				System.out.println(url);

				// append a work to target
				DOMParser work = new DOMParser();
				work.parse(new InputSource(new URL(url).openStream()));
				Node workNode = trgDoc.importNode(work.getDocument().getDocumentElement(), true);
				Element workId = trgDoc.createElement("id");
				workId.appendChild(trgDoc.createTextNode(id));
				workNode.appendChild(workId);
				trgElement.appendChild(workNode);
			}

			// save target
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			StreamResult result = new StreamResult(scrapedXmlFile);
			DOMSource source = new DOMSource(trgDoc);
			transformer.transform(source, result);
		}
	}
}
