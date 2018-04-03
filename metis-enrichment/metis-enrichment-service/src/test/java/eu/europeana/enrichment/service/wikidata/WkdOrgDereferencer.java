/**
 * 
 */
package eu.europeana.enrichment.service.wikidata;
// package eu.europeana.ec.ds.wkd;

import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.input.CharSequenceReader;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * @author Hugo Manguinhas <hugo.manguinhas@europeana.eu>
 * @since 22 Mar 2018
 */
public class WkdOrgDereferencer {
  private static String SPARQL = "https://query.wikidata.org/sparql";
  private static int SIZE = 1024 * 1024;

  private Transformer _transformer;
  private StringBuilder _sb = new StringBuilder(SIZE);


  public WkdOrgDereferencer(Transformer transformer) {
    _transformer = transformer;
  }

  private Model loadModelFromSPARQL(String uri) {
    String sDescribe = "DESCRIBE <" + uri + ">";

    Model m = ModelFactory.createDefaultModel();
    QueryEngineHTTP endpoint = new QueryEngineHTTP(SPARQL, sDescribe);
    try {
      return endpoint.execDescribe(m);
    } catch (RiotException e) {
      System.out.println("Error: " + e.getMessage());
    } finally {
      endpoint.close();
    }

    return m;
  }

  private synchronized void transform(Model m, Transformer t, StreamResult res) {
    StringBuilderWriter sbw = new StringBuilderWriter(_sb);
    try {
      RDFWriter writer = m.getWriter("RDF/XML");
      writer.setProperty("tab", "0");
      writer.setProperty("allowBadURIs", "true");
      writer.setProperty("relativeURIs", "");
      writer.write(m, sbw, "RDF/XML");
      t.transform(new StreamSource(new CharSequenceReader(_sb)), res);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      _sb.setLength(0);
    }
  }

  public void translate(String uri, StreamResult res) {
    _transformer.setParameter("rdf_about", uri);    
    transform(loadModelFromSPARQL(uri), _transformer, res);
  }
}
