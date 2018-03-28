package eu.europeana.indexing;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.utils.MongoConstructor;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;

public class FullBeanCreator {

  private static final Logger LOGGER = LoggerFactory.getLogger(FullBeanCreator.class);

  private static IBindingFactory rdfBindingFactory;
  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  public FullBeanImpl convertStringToFullBean(String record) throws IndexingException {

    // Convert string to RDF
    final RDF rdf;
    try {
      rdf = convertStringToRDF(record);
    } catch (JiBXException e) {
      throw new IndexingException("Could not convert record to RDF.", e);
    }

    // Sanity check - shouldn't happen
    if (rdf == null) {
      throw new IndexingException("Could not convert record to RDF: null was returned.", null);
    }

    // Convert RDF to FullBean
    final FullBeanImpl fBean;
    try {
      fBean = convertRdfToFullBean(rdf);
    } catch (InstantiationException | IllegalAccessException | IOException e) {
      throw new IndexingException("Could not construct FullBean using MongoConstructor.", e);
    }

    // Sanity Check - shouldn't happen
    if (fBean == null) {
      throw new IndexingException("Could not construct FullBean: null was returned.", null);
    }

    // TODO JOCHEN
    // Hack to prevent potential null pointer exceptions
    fBean.setEuropeanaCollectionName(new String[100]);

    // Done.
    return fBean;
  }

  FullBeanImpl convertRdfToFullBean(RDF rdf)
      throws InstantiationException, IllegalAccessException, IOException {
    return new MongoConstructor().constructFullBean(rdf);
  }

  RDF convertStringToRDF(String xml) throws IndexingException, JiBXException {
    IUnmarshallingContext context = getRdfBindingFactory().createUnmarshallingContext();
    return (RDF) context.unmarshalDocument(IOUtils.toInputStream(xml, DEFAULT_CHARSET),
        DEFAULT_CHARSET.name());
  }

  private static synchronized IBindingFactory getRdfBindingFactory() throws IndexingException {
    if (rdfBindingFactory == null) {
      try {
        rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
      } catch (JiBXException e) {
        LOGGER.warn("Error creating the JibX factory.", e);
        throw new IndexingException("Error creating the JibX factory.", e);
      }
    }
    return rdfBindingFactory;
  }
}
