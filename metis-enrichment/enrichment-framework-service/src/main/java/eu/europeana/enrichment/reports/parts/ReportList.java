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
package eu.europeana.enrichment.reports.parts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * Lists.
 * 
 * @author Borys Omelayenko
 * 
 */
public class ReportList<T> extends AbstractReportPart implements Iterable<T> {

	Logger log = LoggerFactory.getLogger(getClass().getName());

	private List<T> list = new ArrayList<T>();

	public ReportList(File dir, String file, int maxSize) throws Exception {
		super(new File(dir, file), maxSize);
	}

	@Override
	public void flush() throws IOException {
		FileWriter writer = new FileWriter(getFile(), true);
		try {
			XStream xStream = new XStream();
			xStream.toXML(list, writer);
			empty();
		} finally {
			writer.close();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load() throws IOException {
		InputStream is = new FileInputStream(getFile());
		try {
			XStream xStream = new XStream();
			list = (List<T>) xStream.fromXML(is);
		} finally {
			is.close();
		}
	}

	public void add(T item) throws IOException {
		list.add(item);
		if (list.size() > getMaxSize()) {
			flush();
			log.info("Autoflush report list file " + getFile().getName());
		}
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

}
