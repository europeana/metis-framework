package eu.europeana.indexing;

import java.io.IOException;
import java.util.function.Supplier;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.utils.MongoConstructor;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexingException;

/**
 * This class converts String representations of RDF (XML) to instances of {@link FullBeanImpl}.
 * 
 * @author jochen
 *
 */
public class FullBeanConverter {

  private static final int MAX_COLLECTION_NAME_LENGTH = 100;
  
  private final Supplier<MongoConstructor> mongoConstructorSupplier;

  /**
   * Constructor.
   */
  public FullBeanConverter() {
    this(MongoConstructor::new);
  }

  /**
   * Constructor for testing purposes.
   * 
   * @param mongoConstructorSupplier Supplies an instance of {@link MongoConstructor} used to
   *        convert an instance of {@link RDF} to an instance of {@link FullBeanImpl}. Will be
   *        called once during every call to convert an RDF.
   */
  FullBeanConverter(Supplier<MongoConstructor> mongoConstructorSupplier) {
    this.mongoConstructorSupplier = mongoConstructorSupplier;
  }

  /**
   * Converts an RDF to Full Bean.
   * 
   * @param rdf The RDF.
   * @return The Full Bean.
   * @throws IndexingException In case there was a problem with the parsing or conversion.
   */
  public FullBeanImpl convertFromRdf(RDF rdf) throws IndexingException {

    // Convert RDF to FullBean
    final FullBeanImpl fBean;
    try {
      fBean = mongoConstructorSupplier.get().constructFullBean(rdf);
    } catch (InstantiationException | IllegalAccessException | IOException e) {
      throw new IndexingException("Could not construct FullBean using MongoConstructor.", e);
    }

    // Sanity Check - shouldn't happen
    if (fBean == null) {
      throw new IndexingException("Could not construct FullBean: null was returned.");
    }

    // TODO Hack to prevent potential null pointer exceptions
    fBean.setEuropeanaCollectionName(new String[MAX_COLLECTION_NAME_LENGTH]);

    // Done.
    return fBean;
  }
}
