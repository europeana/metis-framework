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

import java.io.File;
import java.io.IOException;

/**
 * Persistable part of a report, should be flushed.
 * 
 * @author Borys Omelayenko
 * 
 */
public abstract class AbstractReportPart {

	private File file;

	public File getFile() {
		return file;
	}

	private int maxSize;

	protected int getMaxSize() {
		return maxSize;
	}

	protected void empty() {
		maxSize = 0;
	}

	public AbstractReportPart(File file, int maxSize) {
		super();
		this.file = file;
		this.maxSize = maxSize;
	}

	public abstract void flush() throws IOException;

	public abstract void load() throws Exception;

}
