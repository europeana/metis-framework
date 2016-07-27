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
package eu.europeana.enrichment.common;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.naming.ConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.inferencer.fc.DirectTypeHierarchyInferencer;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.memory.model.MemStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to work with <a href="http://openrdf.org">Sesame</a> .
 * 
 * @author Borys Omelayenko
 * 
 */
public class Helper {

	/**
	 * Creating local repository, the standard way to get a Sesame-RDF
	 * repository.
	 */
	public static Repository createLocalRepository() throws RepositoryException {
		// initializing RDF repository
		Repository repository = new SailRepository(
				new DirectTypeHierarchyInferencer(
						new ForwardChainingRDFSInferencer(new MemoryStore())));
		// NB! MemoryStore(dir) will restore its contents!
		repository.initialize();
		return repository;
	}

	public static Repository createRemoteRepository(String sparqlEndpointUrl)
			throws RepositoryException {
		HTTPRepository repository = new HTTPRepository(sparqlEndpointUrl);
		repository.initialize();
		return repository;
	}

	/**
	 * Creating local repository, the standard way to get a Sesame-RDF
	 * repository.
	 * 
	 * @param tempDir
	 *            Warning! if not <code>null</code> then it will reuse the data
	 *            left in this directory from the previous session.
	 * @return
	 * @throws ConfigurationException
	 */
	public static Repository createLocalRepository(File tempDir)
			throws RepositoryException {
		// initializing RDF repository
		Repository myRepository = new SailRepository(
				new ForwardChainingRDFSInferencer(new MemoryStore(tempDir)));// ,
		// NB! MemoryStore(dir) will restore its contents!

		myRepository.initialize();
		return myRepository;
	}

	/**
	 * Handling transitive properties. It is done by assuming that transitivity
	 * never exceeds a certain level and generating a union-query of the form
	 * <code>X parent {} parent {} ... {} parent Y</code> for the chains of one,
	 * two, etc. parents.
	 * 
	 * @param basicRepeating
	 * @param appendixRepeating
	 * @param property
	 * @param root
	 * @param level
	 * @return
	 */
	public static String generateTracingQuery(String basicRepeating,
			String appendixRepeating, String property, String root, int level) {
		String query = "";
		for (int i = 0; i < level; i++) {
			String union = basicRepeating;
			for (int j = 0; j < i - 1; j++) {
				union += "<" + property + "> {} ";
			}
			union += "<" + property + "> {<" + root + ">} ";
			union += appendixRepeating;
			if (i == 0)
				query += union;
			else
				query += "\nUNION " + union;
		}
		return query;
	}

	public static Set<String> computeDifference(Set<String> a, Set<String> b,
			String prefix, boolean exactMatch) {
		Set<String> overlap = new HashSet<String>();
		for (Iterator<String> it = a.iterator(); it.hasNext();) {
			String code = it.next();
			if (!exactMatch && code.startsWith(prefix))
				if (!b.contains(code))
					overlap.add(code);
			if (exactMatch)
				if (!b.contains(code))
					overlap.add(code);
		}
		return overlap;
	}

	public static Set<String> computeIntersection(Set<String> a, Set<String> b) {
		return computeIntersection(a, b, "", true);
	}

	public static Set<String> computeIntersection(Set<String> a, Set<String> b,
			String prefix, boolean exactMatch) {
		Set<String> overlap = new HashSet<String>();
		for (Iterator<String> it = a.iterator(); it.hasNext();) {
			String code = it.next().toString();
			if (!exactMatch && code.startsWith(prefix))
				if (b.contains(code))
					overlap.add(code);
			if (exactMatch && code.equals(prefix))
				if (b.contains(code))
					overlap.add(code);
		}
		return overlap;
	}

	public static class LoadStatementsResult {
		Set<String> statements = new HashSet<String>();
		Set<String> subjects = new HashSet<String>();
		Set<String> properties = new HashSet<String>();
		Set<String> values = new HashSet<String>();

		public Set<String> getStatements() {
			return statements;
		}

		public Set<String> getSubjects() {
			return subjects;
		}

		public Set<String> getProperties() {
			return properties;
		}

		public Set<String> getValues() {
			return values;
		}

	}

	public static LoadStatementsResult loadStatements(String file,
			String tempDir, PrintStream... out) throws Exception {
		LoadStatementsResult result = new LoadStatementsResult();
		// loading
		Repository rdf = Helper.createLocalRepository();
		RepositoryConnection con = rdf.getConnection();
		try {

			importRDFXMLFile(rdf, "http://localhost/namespace", new File(file));

			RepositoryResult<Statement> statements = con.getStatements(null,
					null, null, false);
			try {
				while (statements.hasNext()) {
					Statement st = statements.next();
					if (st instanceof MemStatement) {
						if (((MemStatement) (st)).isExplicit()) {
							String property = st.getPredicate().toString();
							result.subjects.add(st.getSubject().toString());
							result.properties.add(st.getPredicate().toString());

							String[] subject = st.getSubject().toString()
									.split("#");

							boolean isResource = (st.getObject() instanceof Resource);
							if (isResource) {
								result.values.add("R:"
										+ st.getObject().stringValue());
							} else {
								result.values.add("L"
										+ ((Literal) st.getObject())
												.getLanguage() + ":"
										+ st.getObject().stringValue());
							}

							if (subject.length == 2) {
								result.statements.add(formatLine(subject[0]
										+ "#" /* ":" */+ subject[1], property,
										isResource, st.getObject().toString()));
							} else {
								if (subject.length > 2)
									throw new Exception(
											"Multiple # in resource "
													+ st.getSubject());
								result.statements.add(formatLine(subject[0],
										property, isResource, st.getObject()
												.toString()));
							}
						}
					} else
						throw new Exception("ERROR");
				}
			} finally {
				statements.close();
			}
		} finally {
			con.close();
		}
		return result;
	}

	private static String formatLine(String s, String p, boolean isResource,
			String v) {
		return s + ", " + p + (isResource ? "." : "") + ", " + v;
	}

	private static class FileLoadingThread extends Thread {
		private String ns;
		private File file;
		private Repository rdf;

		public FileLoadingThread(Repository rdf, File file, String ns) {
			super();
			this.rdf = rdf;
			this.file = file;
			this.ns = ns;
		}

		@Override
		public void run() {
			try {
				RepositoryConnection c = rdf.getConnection();
				try {
					if (file.getCanonicalPath().endsWith(".ntriples"))
						c.add(file, ns, RDFFormat.NTRIPLES);
					else {
						System.out.println("trying to add a file");

						c.add(file, ns, RDFFormat.RDFXML);
						System.out.println("added a file");
					}

				} catch (Throwable e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				} finally {
					c.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

	};

	public static void importRDFXMLFile(Repository rdf, String ns,
			File... files) throws Exception {
		Stack<Thread> threads = new Stack<Thread>();

		Logger log = LoggerFactory.getLogger("RDF/XML Importer");

		for (File file : files) {
			try {
				// multi-threaded load to enjoy multi-core CPUs
				Thread thread = new FileLoadingThread(rdf, file, ns);
				log.info("Loading  file "
						+ file
						+ " of "
						+ new DecimalFormat("###,###,###.###").format(file
								.length()) + " bytes");// , thread " +
														// threads.size());
				System.setProperty("entityExpansionLimit", "1000000");
				thread.run();
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Failed to load file "
						+ file.getCanonicalPath(), e);
			}
		}

		while (!threads.isEmpty()) {
			if (!threads.peek().isAlive()) {
				threads.pop();
			}
			Thread.sleep(100);
		}

	}

	/**
	 * Generates list of files from a regular expression pattern. Useful for
	 * batch file processing. Beware that a point in the regular expression
	 * means 'any symbol'. The directory should be separated from the file names
	 * with forward slash <code>/</code>.
	 * 
	 * @param pattern
	 *            regular expression
	 * @return list of file names that match the <code>pattern</code>
	 */
	static public String[] makeFileNames(final String pattern) {
		// Getting the list of source files
		File dir = new File(pattern.substring(0, pattern.lastIndexOf('/') + 1));

		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isFile()
						&& file.getName()
								.matches(
										pattern.substring(pattern
												.lastIndexOf('/') + 1));
			}
		};

		File[] files = dir.listFiles(fileFilter);
		if (files == null)
			throw new RuntimeException("No file is found with pattern "
					+ pattern);
		String[] productionFileNames = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			productionFileNames[i] = files[i].getAbsolutePath();
		}
		if (productionFileNames.length == 0) {
			Logger log = LoggerFactory.getLogger("CLI Parser");
			log.warn("No files to convert");
		}
		return productionFileNames;
	}

	// taken from
	// http://www.exampledepot.com/egs/javax.xml.transform/BasicXsl.html
	// This method applies the xslFilename to inFilename and writes
	// the output to outFilename.
	public static void xsl(File inFile, File outFile, InputStream xslStream)
			throws Exception {
		try {
			// Create transformer factory
			TransformerFactory factory = TransformerFactory.newInstance();

			// Use the factory to create a template containing the xsl file
			Templates template = factory.newTemplates(new StreamSource(
					xslStream));

			// Use the template to create a transformer
			Transformer xformer = template.newTransformer();

			// Prepare the input and output files
			Source source = new StreamSource(new FileInputStream(inFile));
			Result result = new StreamResult(new FileOutputStream(outFile));

			// Apply the xsl file to the source file and write the result to the
			// output file
			xformer.transform(source, result);
		} catch (TransformerException e) {
			// An error occurred while applying the XSL file
			// Get location of error in input file
			SourceLocator locator = e.getLocator();
			int col = locator.getColumnNumber();
			int line = locator.getLineNumber();
			throw new Exception(String.format(
					"XSL exception line %d col %d message: %s", line, col,
					e.getMessage()));
		}
	}

	public static List<Class> findAllClasses(Class superclass,
			String... packages) throws IOException, ClassNotFoundException {
		List<Class> result = new ArrayList<Class>();
		for (String packge : packages) {
			List<Class> l = Utils.getClassesForPackage(packge);
			for (Class c : l) {
				String className = c.getName();
				Type type = Class.forName(className).getGenericSuperclass();
				if (type != null
						&& (type.equals(superclass) || result.contains(type))) {
					Class classClass = Class.forName(className).asSubclass(
							superclass);
					result.add(classClass);
				}
			}
		}
		return result;
	}

}
