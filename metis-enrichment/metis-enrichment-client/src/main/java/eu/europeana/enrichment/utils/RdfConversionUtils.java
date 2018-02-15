package eu.europeana.enrichment.utils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.jibx.RDF;

public final class RdfConversionUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentUtils.class);

  private static final String UTF8 = StandardCharsets.UTF_8.name();

  private static IBindingFactory rdfBindingFactory;

  static {
    try {
      rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
    } catch (JiBXException e) {
      LOGGER.error("Unable to create binding factory", e);
    }
  }

  private RdfConversionUtils() {}

  private static IBindingFactory getRdfBindingFactory() {
    if (rdfBindingFactory != null) {
      return rdfBindingFactory;
    }
    throw new IllegalStateException("No binding factory available.");
  }

  /**
   * Convert an RDF to a UTF-8 encoded XML
   * 
   * @param rdf The RDF object to convert
   * @return An XML string representation of the RDF object
   * @throws JiBXException
   * @throws UnsupportedEncodingException
   */
  public static String convertRdfToString(RDF rdf)
      throws JiBXException, UnsupportedEncodingException {
    IMarshallingContext context = getRdfBindingFactory().createMarshallingContext();
    context.setIndent(2);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    context.marshalDocument(rdf, UTF8, null, out);
    return out.toString(UTF8);
  }

  public static RDF convertStringToRdf(String xml) throws JiBXException {
    IUnmarshallingContext context = getRdfBindingFactory().createUnmarshallingContext();
    return (RDF) context.unmarshalDocument(IOUtils.toInputStream(xml), UTF8);
  }
}
