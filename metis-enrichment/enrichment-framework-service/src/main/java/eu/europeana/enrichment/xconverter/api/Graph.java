package eu.europeana.enrichment.xconverter.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import eu.europeana.enrichment.triple.Property;
import eu.europeana.enrichment.triple.Triple;

/**
 * A destination named graph: a graph of RDF statements that are written to a
 * separate file.
 * 
 * @author Borys Omelayenko
 * 
 */
public interface Graph {

	public interface NamedGraphAddListener {
		public abstract void add(Triple triple);
	}

	/**
	 * Adds a triple to this graph. Triple will be added to the real graph
	 * (defaults to this graph).
	 * 
	 * @param triple
	 * @throws Exception
	 */
	public abstract void add(Triple triple) throws Exception;

	/**
	 * Adds a listener that would be invoked on each triple added to the named
	 * graph.
	 * 
	 * @param listener
	 */
	public abstract void addNamedGraphAddListener(
			NamedGraphAddListener addListener);

	/**
	 * User comment describing this graph.
	 * 
	 */
	public abstract List<String> getComments();

	/**
	 * File where this graph should be copied after the conversion.
	 * 
	 * @param volume
	 *            TODO
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract File getFinalFile(int volume) throws IOException;

	/**
	 * Graph id that also makes the base for the file name. Depends on the
	 * dataset name and both graph ids.
	 * 
	 * @return
	 */
	public abstract String getId();

	/**
	 * Hash code of graph id.
	 */
	public abstract int hashCode();

	/**
	 * Equals on graph id.
	 */
	public abstract boolean equals(Object obj);

	/**
	 * Properties used in the graph.
	 */
	public abstract Set<Property> getProperties();

	/**
	 * Size in triples.
	 * 
	 * @return
	 */
	public abstract long size();

	/**
	 * Last added triple, for debugging purpose
	 * 
	 * @param offset
	 *            offset from end, <code>0</code> means the last triple,
	 *            <code>1</code> one but the last, etc.
	 * @return
	 */
	public Triple getLastAddedTriple(int offset);

	/**
	 * Dynamic graphs: allow changing the graph depending on the data.
	 */
	public abstract Graph getRealGraph();

	/**
	 * Dynamic graphs: allow changing the graph depending on the data.
	 */
	public abstract void setRealGraph(Graph realGraph);

	/**
	 * Finish writing this graph into RDF and close the RDF file.
	 * 
	 * @throws Exception
	 */
	public void endRdf() throws Exception;

	public boolean writingHappened() throws Exception;

	public void startRdf() throws Exception;

	public int getVolume();

}