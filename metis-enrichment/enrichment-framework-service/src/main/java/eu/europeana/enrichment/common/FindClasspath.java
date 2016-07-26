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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FindClasspath {
	public static void main(String[] args) throws Exception {
		FindClasspath fc = new FindClasspath();

		System.out.println(fc.getClassesForPackage("java.math"));
		System.out.println(fc.getClassesForPackage("classpath"));
	}

	static FilenameFilter classFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			if (name.endsWith(".class")) {
				return true;
			}
			return false;
		}
	};

	public Set<String> getClassesForPackage(String packagename)
			throws IOException {
		Set<String> classesForPackage = new TreeSet<String>();
		List<String> resourcesToCheck = getClasspathEntries();
		String[] pathEntries = packagename.split("\\.");
		String dir = buildDirectoryName(pathEntries);
		// Windows uses \, UNIX uses /, jar uses /.
		// We need to make sure dir is in the right format for jars.
		String jarDir = buildJarDirectoryName(pathEntries);

		for (String resource : resourcesToCheck) {
			File f = new File(resource);
			if (f.exists() && f.isDirectory()) {
				addClassesFromDirectory(classesForPackage, dir, resource);

			} else {
				if (resource.endsWith(".jar")) {
					addClassesFromJar(classesForPackage, jarDir, resource);
				}
			}
		}
		return classesForPackage;
	}

	private void addClassesFromJar(Set<String> classesForPackage,
			String jarDir, String resource) throws IOException {
		// okay, a file should be a ZIP file of some kind; we're looking for zip
		// entries.
		try {
			JarFile jarfile = new JarFile(resource);
			// jarfile.entries() returns an enumeration,
			// not an iterator or a Collection, so we get to walk it.
			//
			// darn that dragon!
			Enumeration<JarEntry> jarEntries = jarfile.entries();
			while (jarEntries.hasMoreElements()) {
				JarEntry je = jarEntries.nextElement();
				if (je.getName().startsWith(jarDir)) {
					if (classFilter.accept(null, je.getName())) {
						// we need to parse off the dir when we add the
						// classname.
						classesForPackage.add(stripClassExtension(je.getName()
								.substring(jarDir.length() + 1)));
					}
				}
			}
		} catch (IOException e) {
			throw new IOException("File: " + resource + " jarDir: " + jarDir, e);
		}
	}

	private void addClassesFromDirectory(Set<String> classesForPackage,
			String dir, String resource) {
		// if it's a directory, we need to descend the directory tree to the
		// right
		// package location, and scan.
		File descentDir = new File(resource + dir);
		if (descentDir.exists() && descentDir.isDirectory()) {
			// we have a valid hit, we need to try to explore it.
			// for directories, all we have to do is get a list() of classes.
			// I wanted to do this with one statement; I didn't for clarity's
			// sake.
			String[] dirListing = descentDir.list(classFilter);
			for (String c : dirListing) {
				classesForPackage.add(stripClassExtension(c));
			}
		}
	}

	private String stripClassExtension(String className) {
		return className.replaceAll("\\.class", "");
	}

	private String buildJarDirectoryName(String[] pathEntries) {
		return internalJoin(pathEntries, "/").substring(1);
	}

	private String buildDirectoryName(String[] pathEntries) {
		return internalJoin(pathEntries, System.getProperty("file.separator"));
	}

	/**
	 * I swear sometimes I wish String had this.
	 * 
	 * @param array
	 *            the array to concatenate into a single string
	 * @param separator
	 *            the separator for each element
	 * @return the concatenated array, with entries separated by the separator
	 */
	private String internalJoin(String[] array, String separator) {
		StringBuilder sb = new StringBuilder();

		for (String path : array) {
			sb.append(separator);
			sb.append(path);
		}
		return sb.toString();
	}

	/**
	 * This returns the classpath to examine. This is meant to be overridden if
	 * you've installed your own classloader with its own classpath.
	 * 
	 * @return The classpath, as a system-dependent String
	 */
	public String getClasspath() {
		return System.getProperty("java.class.path");
	}

	/**
	 * This parses the current classpath as returned by getClasspath() as a list
	 * of Strings.
	 * 
	 * @return A list of strings representing resources in the classpath
	 */
	public final List<String> getClasspathEntries() {
		String classpath = getClasspath();
		String[] entries = classpath
				.split(System.getProperty("path.separator"));
		return Arrays.asList(entries);
	}
}
