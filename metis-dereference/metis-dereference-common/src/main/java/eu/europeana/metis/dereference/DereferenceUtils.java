package eu.europeana.metis.dereference;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.utils.EntityMergeUtils;

/**
 * Created by gmamakis on 9-3-17.
 */
public class DereferenceUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DereferenceUtils.class);

  private static final String UTF8 = StandardCharsets.UTF_8.name();
  
  private static final Function<ResourceType, String> RESOURCE_EXTRACTOR = DereferenceUtils::extractFromResource; 
  private static final Function<ResourceOrLiteralType, String> RESOURCE_OR_LITERAL_EXTRACTOR = DereferenceUtils::extractFromResourceOrLiteral; 
  
  private static IBindingFactory rdfBindingFactory = null;
  
  static {
    try {
      rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
    } catch (JiBXException e) {
      LOGGER.error("Unable to create binding factory", e);
    }
  }

  private DereferenceUtils() { }

  private static IBindingFactory getRdfBindingFactory() {
    if (rdfBindingFactory != null) {
      return rdfBindingFactory;
    }
    throw new IllegalStateException("No binding factory available.");
  }

  /**
   * Merge entities in a record after dereferencing
   * @param rdf The RDF to enrich
   * @param enrichmentBaseList The information to append
   * @param fieldName The name of the field so that it can be connected to Europeana Proxy
   * @return An RDF object with the merged entities
   */
  public static RDF mergeEntity(RDF rdf, List<EnrichmentBase> enrichmentBaseList) {
  	return EntityMergeUtils.mergeEntity(rdf, enrichmentBaseList, "");
  }

  /**
   * Extract values from RDF document
   * @param RDF input document
   * @return set of values for dereferencing
   * @throws JiBXException
   */
  public static Set<String> extractValuesForDereferencing(RDF rdf) {
    final Set<String> result = new HashSet<>();
    extractValues(rdf.getPlaceList(), item -> dereferencePlace(item, result));
    extractValues(rdf.getAgentList(), item -> dereferenceAgent(item, result));
    extractValues(rdf.getConceptList(), concept -> {
      result.add(concept.getAbout());
      extractValues(concept.getChoiceList(), item -> dereferenceConceptChoice(item, result));
    });
    extractValues(rdf.getTimeSpanList(), item -> dereferenceTimespan(item, result));
    extractValues(rdf.getWebResourceList(), item -> dereferenceWebResource(item, result));
    extractValues(rdf.getProxyList(), item -> dereferenceProxy(item, result));
    return result;
  }

  private static <S> void extractValues(List<S> source, Consumer<S> extractor) {
    if (source != null) {
      for (S sourceItem : source) {
        extractor.accept(sourceItem);
      }
    }
  }

  private static <T> void convertValues(List<? extends T> source,
      Function<T, String> conversion, Set<String> result) {
    if (source != null) {
      for (T sourceItem : source) {
        convertValue(sourceItem, conversion, result);
      }
    }
  }

  private static <T> void convertValue(T source, Function<T, String> conversion,
      Set<String> result) {
    if (source != null) {
      final String target = conversion.apply(source);
      if (StringUtils.isNotEmpty(target)) {
        result.add(target);
      }
    }
  }

  public static RDF toRDF(String xml) throws JiBXException {
	    IUnmarshallingContext context = getRdfBindingFactory().createUnmarshallingContext();
	    return (RDF) context.unmarshalDocument(IOUtils.toInputStream(xml), UTF8);
  }
  
  private static void dereferenceProxy(ProxyType proxyType, Set<String> values) {
    convertValues(proxyType.getHasMetList(), RESOURCE_EXTRACTOR, values);
    convertValues(proxyType.getHasTypeList(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValues(proxyType.getIncorporateList(), RESOURCE_EXTRACTOR, values);
    convertValues(proxyType.getIsDerivativeOfList(), RESOURCE_EXTRACTOR, values);
    convertValues(proxyType.getIsRelatedToList(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValues(proxyType.getIsSimilarToList(), RESOURCE_EXTRACTOR, values);
    convertValues(proxyType.getIsSuccessorOfList(), RESOURCE_EXTRACTOR, values);
    convertValues(proxyType.getRealizeList(), RESOURCE_EXTRACTOR, values);
    convertValue(proxyType.getCurrentLocation(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    extractValues(proxyType.getChoiceList(), item -> dereferenceChoice(item, values));
  }

  private static void dereferenceChoice(Choice choice, Set<String> values) {
    convertValue(choice.getSubject(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getPublisher(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getReferences(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getRelation(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getReplaces(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getContributor(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getCoverage(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getCreator(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getCreated(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getDate(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getExtent(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getFormat(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getHasFormat(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getHasPart(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getHasVersion(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getSource(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getSpatial(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getTemporal(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getIsFormatOf(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getIsPartOf(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getIsReferencedBy(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getIsRequiredBy(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getIssued(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getIsVersionOf(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getMedium(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.getType(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
  }

  private static void dereferenceTimespan(TimeSpanType timespan, final Set<String> result) {
    result.add(timespan.getAbout());
    convertValues(timespan.getHasPartList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
    convertValues(timespan.getSameAList(), RESOURCE_EXTRACTOR, result);
    convertValues(timespan.getIsPartOfList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
  }

  private static void dereferenceAgent(AgentType agent, final Set<String> result) {
    result.add(agent.getAbout());
    convertValues(agent.getHasMetList(), RESOURCE_EXTRACTOR, result);
    convertValues(agent.getIsRelatedToList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
  }

  private static void dereferenceConceptChoice(Concept.Choice choice, final Set<String> result) {
    convertValue(choice.getBroadMatch(), RESOURCE_EXTRACTOR, result);
    convertValue(choice.getCloseMatch(), RESOURCE_EXTRACTOR, result);
    convertValue(choice.getExactMatch(), RESOURCE_EXTRACTOR, result);
    convertValue(choice.getNarrowMatch(), RESOURCE_EXTRACTOR, result);
    convertValue(choice.getRelatedMatch(), RESOURCE_EXTRACTOR, result);
    convertValue(choice.getRelated(), RESOURCE_EXTRACTOR, result);
  }

  private static void dereferencePlace(PlaceType place, Set<String> result) {
    result.add(place.getAbout());
    convertValues(place.getIsPartOfList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);    
    convertValues(place.getSameAList(), RESOURCE_EXTRACTOR, result);    
    convertValues(place.getHasPartList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);    
  }

  private static void dereferenceWebResource(WebResourceType wr, final Set<String> result) {
    convertValues(wr.getCreatedList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
    convertValues(wr.getExtentList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
    convertValues(wr.getFormatList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
    convertValues(wr.getHasPartList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
    convertValues(wr.getIsFormatOfList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
    convertValues(wr.getIssuedList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
  }
  
  private static String extractFromResourceOrLiteral(ResourceOrLiteralType type) {
    if (type.getResource() != null && StringUtils.isNotEmpty(type.getResource().getResource())) {
      return type.getResource().getResource();
    }
    return null;
  }

  private static String extractFromResource(ResourceType type) {
    if (StringUtils.isNotEmpty(type.getResource())) {
      return type.getResource();
    }
    return null;
  }
}
