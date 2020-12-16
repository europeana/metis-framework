package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.schema.jibx.RDF;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;

/**
 * This class maintains an instance of {@link IBindingFactory} which it can make available upon
 * request.
 *
 * TODO use {@link eu.europeana.metis.schema.convert.RdfConversionUtils} - no org.jibx.runtime.*
 * import should remain.
 */
final class RdfBindingFactoryProvider {

  private static IBindingFactory rdfBindingFactory;

  /**
   * Constructor - this class should not be initialized.
   */
  private RdfBindingFactoryProvider() {
  }

  /**
   * @return A binding factory.
   * @throws JiBXException In case the binding factory could not be created.
   */
  static IBindingFactory getBindingFactory() throws JiBXException {
    synchronized (RdfBindingFactoryProvider.class) {
      if (rdfBindingFactory == null) {
        rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
      }
      return rdfBindingFactory;
    }
  }
}
