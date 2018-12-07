package eu.europeana.metis.mediaprocessing;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaprocessing.exception.RdfConverterException;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;

/**
 * This is the super class of all RDF converters, providing an instance of {@link IBindingFactory}.
 */
class RdfConverter {

  private static IBindingFactory rdfBindingFactory;

  RdfConverter() {
  }

  /**
   * @return A binding factory.
   * @throws RdfConverterException In case the binding factory could not be created.
   */
  static synchronized IBindingFactory getBindingFactory() throws RdfConverterException {
    if (rdfBindingFactory == null) {
      try {
        rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
      } catch (JiBXException e) {
        throw new RdfConverterException("Unable to create binding factory", e);
      }
    }
    return rdfBindingFactory;
  }
}
