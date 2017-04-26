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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * Various utilities.
 * 
 * @author Borys Omelayenko
 * 
 */
public class Utils {

	


	public static List<File> expandFileTemplateFrom(File dir, String... pattern)
			throws IOException {
		List<File> files = new ArrayList<File>();

		for (String p : pattern) {

			File fdir = new File(
					new File(dir, FilenameUtils.getFullPathNoEndSeparator(p))
							.getCanonicalPath());
			if (!fdir.isDirectory())
				throw new IOException("Error: " + fdir.getCanonicalPath()
						+ ", expanded from directory " + dir.getCanonicalPath()
						+ " and pattern " + p + " does not denote a directory");
			if (!fdir.canRead())
				throw new IOException("Error: " + fdir.getCanonicalPath()
						+ " is not readable");
			FileFilter fileFilter = new WildcardFileFilter(
					FilenameUtils.getName(p));
			File[] list = fdir.listFiles(fileFilter);
			if (list == null)
				throw new IOException(
						"Error: "
								+ fdir.getCanonicalPath()
								+ " does not denote a directory or something else is wrong");
			if (list.length == 0)
				throw new IOException("Error: no files found, template " + p
						+ " from dir " + dir.getCanonicalPath()
						+ " where we recognised " + fdir.getCanonicalPath()
						+ " as path and " + fileFilter + " as file mask");
			for (File file : list) {
				if (!file.exists()) {
					throw new FileNotFoundException("File not found: " + file
							+ " resolved to " + file.getCanonicalPath());
				}
			}
			files.addAll(Arrays.asList(list));
		}

		return files;
	}

	public static String show(Collection list, String separator) {
		String result = "";
		for (Object object : list) {
			if (result.length() > 0)
				result += separator;
			result += object.toString();
		}
		return result;
	}

	/**
	 * Returned by compareFiles.
	 * 
	 */
	public static class DiffInFiles {
		@Override
		public String toString() {
			return "Line" + line + ", " + strOne + " * " + strTwo;
		}

		public int line;
		public String strOne;
		public String strTwo;

		public DiffInFiles(int line, String strOne, String strTwo) {
			super();
			this.line = line;
			this.strOne = strOne;
			this.strTwo = strTwo;
		}

	}

	/**
	 * Compares two text files.
	 * 
	 * @param file1
	 * @param file2
	 * @return lines where they differ
	 */
	public static List<DiffInFiles> compareFiles(File file1, File file2,
			int maxDiffLinesToReport) throws IOException {
		List<DiffInFiles> result = new ArrayList<DiffInFiles>();
		BufferedReader b1 = new BufferedReader(new FileReader(file1), 64000);
		BufferedReader b2 = new BufferedReader(new FileReader(file2), 64000);
		int lineNumber = 0;
		while (maxDiffLinesToReport == -1
				|| result.size() < maxDiffLinesToReport) {
			String s1 = b1.readLine();
			String s2 = b2.readLine();
			// end of both files
			if (s1 == null && s2 == null)
				break;

			// one is shorter
			if (s1 == null) {
				result.add(new DiffInFiles(lineNumber, s1, s2));
			} else {
				if (s2 == null) {
					result.add(new DiffInFiles(lineNumber, s1, s2));
				} else {
					if (!s1.equals(s2))
						result.add(new DiffInFiles(lineNumber, s1, s2));
				}
			}
			lineNumber++;
		}
		b1.close();
		b2.close();
		return result;
	}

	/**
	 * Loads a file into a string.
	 * 
	 * @param fileName
	 *            file name
	 * @param EOL
	 *            End-of-line string to put into the resulting string
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String loadFileToString(String fileName, String EOL)
			throws FileNotFoundException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		String result = "";
		String str;
		while ((str = in.readLine()) != null) {
			result += str + EOL;
		}
		in.close();
		return result;
	}

	public static void saveStringToFile(String string, String fileName)
			throws FileNotFoundException, IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
		out.append(string);
		out.close();
	}

	/**
	 * Loads a web page from an URL to a string.
	 * 
	 * @param url
	 *            address of the page
	 * @param EOL
	 *            End-of-line string to put into the resulting string
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String loadURLToString(String url, String EOL)
			throws FileNotFoundException, IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader((new URL(
				url)).openStream()));
		String result = "";
		String str;
		while ((str = in.readLine()) != null) {
			result += str + EOL;
		}
		in.close();
		return result;
	}

	// public static String[] filesToString(List<File> files) throws Exception
	// {
	// String[] result = new String[files.size()];
	// int i = 0;
	// for (File file : files)
	// {
	// result[i++] = file.getCanonicalPath();
	// }
	// return result;
	// }
	//
	// public static long hashCode(String[] list)
	// {
	// return merge(list, ";").hashCode();
	// }
	//
	// public static String merge(String[] list, String separator)
	// {
	// String merged = "";
	// for (int i = 0; i < list.length; i++)
	// {
	// merged += ((i == 0) ? "" : separator) + list[i];
	// }
	// return merged;
	// }

	/*
	 * From
	 * http://www.java-tips.org/java-se-tips/java.io/reading-a-file-into-a-byte
	 * -array.html and adapted
	 */
	private static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		if (length > Integer.MAX_VALUE) {
			// File is too large
			throw new IOException("File is too large");
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	static public byte[] readResourceFile(File src) throws IOException {
		if (src == null)
			throw new NullPointerException("Null src");
		String srcPath;
		try {
			srcPath = src.getCanonicalPath();
		} catch (Exception e) {
			throw new IOException(e.getMessage() + " on " + src.getName());
		}
		if (srcPath.contains("!")) {
			// JARs
			// remove dire prefix
			if (srcPath.indexOf("file:") != srcPath.lastIndexOf("file:"))
				throw new IOException("Source " + srcPath
						+ " should contain only one substring 'file:'");

			srcPath = srcPath.substring(srcPath.indexOf("file:")
					+ "file:".length());
			String[] srcPaths = srcPath.split("!");
			if (srcPaths.length != 2)
				throw new IOException(
						"Source "
								+ srcPath
								+ " should contain no '!' for plain files or just '!' for jar files.");

			if (srcPaths[1].startsWith("/") || srcPaths[1].startsWith("\\"))
				srcPaths[1] = srcPaths[1].substring(1);

			JarResources jr = new JarResources(srcPaths[0]);
			byte[] buf = jr.getResource(srcPaths[1]);

			if (buf == null)
				throw new NullPointerException("Null resource: " + srcPaths[1]
						+ " from " + srcPaths[0]);
			return buf;
		} else {
			// plain files
			return getBytesFromFile(src);
		}
	}

	static public String readResourceFileAsString(String resource)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(readResourceFile(new File(resource)))));

		String line;
		String result = "";
		while ((line = br.readLine()) != null) {
			result += line + "\n";
		}
		br.close();
		return result;
	}

	private static InputStream readResourceFromPackage(Class theClass,
			String resource) throws IOException {
		if (theClass.getResource(resource) == null)
			throw new NullPointerException("Failed to find resource for class "
					+ theClass.getName() + " resource " + resource);

		String fileName = theClass.getResource(resource).getFile();
		if (fileName == null)
			throw new NullPointerException(
					"Failed to generate file for resource for class "
							+ theClass.getName() + " resource " + resource);
		byte[] file = readResourceFile(new File(fileName));
		return new ByteArrayInputStream(file);
	}

	static public String readResourceFileFromSamePackageAsString(
			Class theClass, String resource) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				readResourceFromPackage(theClass, resource)));
		String line;
		String result = "";
		while ((line = br.readLine()) != null) {
			result += line + "\n";
		}
		br.close();
		return result;
	}

	static public List<String> readResourceFileFromSamePackageAsList(
			Class theClass, String resource) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				readResourceFromPackage(theClass, resource)));
		String line;
		List<String> result = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			result.add(line);
		}
		br.close();
		return result;
	}

	/**
	 * A dirty Dutch plural -> singular converter.
	 */
	public static String getSingular(String plural) {
		if (plural == null)
			return null;
		String singular = plural;
		// if word ends with -en : cut off -en
		if (plural.endsWith("en"))
			singular = plural.substring(0, plural.length() - 2);
		else
		// if word ends with 's : cut off 's
		if (plural.endsWith("'s"))
			singular = plural.substring(0, plural.length() - 2);
		else
		// if word ends with -s : cut off -s
		if (plural.endsWith("s"))
			singular = plural.substring(0, plural.length() - 1);

		// then
		// if word ends with two similar consonant : remove one
		if (singular
				.matches("(.*)(ss|tt|qq|ww|rr|pp|dd|ff|gg|kk|ll|zz|xx|cc|vv|bb|nn|mm)$"))
			singular = singular.substring(0, singular.length() - 1);
		// if word ends with -v : remove -v and add -f
		if (singular.endsWith("v"))
			singular = singular.substring(0, singular.length() - 1) + "f";
		// if word ends with -z : remove -z and add -s
		if (singular.endsWith("z"))
			singular = singular.substring(0, singular.length() - 1) + "s";
		return singular;
	}

	/**
	 * Attempts to list all the classes in the specified package as determined
	 * by the context class loader
	 * 
	 * @param pckgname
	 *            the package name to search
	 * @return a list of classes that exist within that package
	 * @throws ClassNotFoundException
	 *             if something went wrong
	 */
	public static List<Class> getClassesForPackage(String pckgname)
			throws ClassNotFoundException, IOException {
		// Set<String> list = getClassNamesPackage(pckgname);

		Set<String> list = new FindClasspath().getClassesForPackage(pckgname);

		List<Class> classes = new ArrayList<Class>();
		for (String fileName : list) {
			classes.add(Class.forName(pckgname.length() == 0 ? fileName
					: (pckgname + '.' + fileName)));
		}
		return classes;
	}

	private static Set<String> getClassNamesPackage(String pckgname)
			throws ClassNotFoundException, IOException {
		// This will hold a list of directories matching the pckgname. There may
		// be
		// more than one if a package is split over multiple jars/paths
		Queue<File> directories = new LinkedList<File>();
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			if (cld == null) {
				throw new ClassNotFoundException("Can't get class loader.");
			}
			String path = pckgname.replace('.', '/');
			// Ask for all resources for the path
			Enumeration<URL> resources = cld.getResources(path);
			while (resources.hasMoreElements()) {
				directories.add(new File(URLDecoder.decode(resources
						.nextElement().getPath(), "UTF-8")));
			}
		} catch (NullPointerException x) {
			throw new ClassNotFoundException(
					pckgname
							+ " does not appear to be a valid package (Null pointer exception)");
		} catch (UnsupportedEncodingException encex) {
			throw new ClassNotFoundException(
					pckgname
							+ " does not appear to be a valid package (Unsupported encoding)");
		} catch (IOException ioex) {
			throw new ClassNotFoundException(
					"IOException was thrown when trying to get all resources for "
							+ pckgname);
		}

		Set<String> classes = new HashSet<String>();
		// For every directory identified capture all the .class files
		while (!directories.isEmpty()) {
			File directory = directories.poll();
			if (directory.exists()) {
				// Get the list of the files contained in the package
				File[] files = directory.listFiles();
				for (File file : files) {
					// we are only interested in .class files
					if (file.getCanonicalPath().endsWith(".class")) {
						String fileName = file.getPath().substring(
								directory.getPath().length() + 1);
						pckgname = file.getPath()
								.substring(
										file.getPath().indexOf(
												File.separator + "nl"
														+ File.separator) + 1);
						pckgname = pckgname.substring(0,
								pckgname.lastIndexOf(File.separator))
								.replaceAll("\\" + File.separator, ".");
						// if (!fileName.matches("(.+)\\$\\d\\.class"))
						// removes the .class extension
						classes.add(fileName.substring(0, fileName.length() - 6));
					}
					// Add subdirs
					if (file.isDirectory()) {
						directories.add(file);
					}
				}
			} else {
				throw new ClassNotFoundException(pckgname + " ("
						+ directory.getPath()
						+ ") does not appear to be a valid package");
			}
		}
		return classes;
	}

	public static void copy(InputStream src, File dst) throws IOException {
		if (src == null)
			throw new NullPointerException("Source should not be NULL.");

		if (dst == null)
			throw new NullPointerException("Dest should not be NULL.");

		OutputStream out = new FileOutputStream(dst);
		while (src.available() != 0) {
			out.write(src.read());
		}
		out.close();
	}

}
