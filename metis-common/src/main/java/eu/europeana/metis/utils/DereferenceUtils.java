package eu.europeana.metis.utils;

import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.Created;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.Extent;
import eu.europeana.corelib.definitions.jibx.Format;
import eu.europeana.corelib.definitions.jibx.HasMet;
import eu.europeana.corelib.definitions.jibx.HasPart;
import eu.europeana.corelib.definitions.jibx.HasType;
import eu.europeana.corelib.definitions.jibx.Incorporates;
import eu.europeana.corelib.definitions.jibx.IsDerivativeOf;
import eu.europeana.corelib.definitions.jibx.IsFormatOf;
import eu.europeana.corelib.definitions.jibx.IsPartOf;
import eu.europeana.corelib.definitions.jibx.IsRelatedTo;
import eu.europeana.corelib.definitions.jibx.IsSimilarTo;
import eu.europeana.corelib.definitions.jibx.IsSuccessorOf;
import eu.europeana.corelib.definitions.jibx.Issued;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.Realizes;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.definitions.jibx.SameAs;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by gmamakis on 9-3-17.
 */
public class DereferenceUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DereferenceUtils.class);

  private static IBindingFactory factory;

  static {
    try {
      factory = BindingDirectory.getFactory(RDF.class);
    } catch (JiBXException e) {
      LOGGER.error("Unable to create binding factory", e);
      System.exit(-1);
    }
  }

  private DereferenceUtils() { }

  /**
   * Merge entities for dereferencing (simpler rules than enrichment)
   *
   * @param record The original record
   * @param entity The xml representation of the entity
   * @return A JibX RDF object (Java object)
   */
  public static RDF mergeEntityForDereferencing(String record, String entity) throws JiBXException {
    return EntityMergeUtils.mergeEntity(record, entity);
  }
  
  /**
   * Merge entities in a record after dereferencing
   * @param rdf The RDF to enrich
   * @param enrichmentBaseList The information to append
   * @param fieldName The name of the field so that it can be connected to Europeana Proxy
   * @return An RDF object with the merged entities
   */
  public static RDF mergeEntity(RDF rdf, ArrayList<EnrichmentBase> enrichmentBaseList) {
  	return EntityMergeUtils.mergeEntity(rdf, enrichmentBaseList, "");
  }

  /**
   * Extract values from xml document
   * @param xml input document
   * @return set of values for dereferencing
   * @throws JiBXException
   */
  public static Set<String> extractValuesForDereferencing(String xml) throws JiBXException {
    IUnmarshallingContext context = factory.createUnmarshallingContext();
    RDF rdf = (RDF) context.unmarshalDocument(IOUtils.toInputStream(xml), "UTF-8");
    
    return extractValuesForDereferencing(rdf);
  }
  
  /**
   * Extract values from RDF document
   * @param RDF input document
   * @return set of values for dereferencing
   * @throws JiBXException
   */
  // TODO JOCHEN should this method be public? Where is it used?
  public static Set<String> extractValuesForDereferencing(RDF rdf) {
	    Set<String> values = new HashSet<>();

	    values.addAll(dereferencePlaceList(rdf.getPlaceList()));
	    values.addAll(dereferenceAgentList(rdf.getAgentList()));
	    values.addAll(dereferenceConceptList(rdf.getConceptList()));
	    values.addAll(dereferenceTimespanList(rdf.getTimeSpanList()));
	    values.addAll(dereferenceWebResourceList(rdf.getWebResourceList()));
	    values.addAll(dereferenceProxyList(rdf.getProxyList()));

	    return values;
  }
  
  // TODO JOCHEN is this method needed? It is definitely not functional.
  public static RDF toRDF(String xml) throws JiBXException {
	    Set<String> values = new HashSet<>();

	    IUnmarshallingContext context = factory.createUnmarshallingContext();
	    RDF rdf = (RDF) context.unmarshalDocument(IOUtils.toInputStream(xml), "UTF-8");

	    return rdf;
	  }

  private static Set<String> dereferenceProxyList(List<ProxyType> proxyList) {
    Set<String> values = new HashSet<>();
    if (proxyList != null) {
      for (ProxyType proxyType : proxyList) {
        values.addAll(dereferenceProxy(proxyType));
      }
    }
    return values;
  }

  private static Set<String> dereferenceProxy(ProxyType proxyType) {
    Set<String> values = new HashSet<>();
    if (proxyType.getHasMetList() != null) {
      List<HasMet> hasMetList = proxyType.getHasMetList();
      for (HasMet hasMet : hasMetList) {
        values.add(extractValueFromResource(hasMet));
      }
    }
    if (proxyType.getHasTypeList() != null) {
      List<HasType> hasTypeList = proxyType.getHasTypeList();
      for (HasType hasType : hasTypeList) {
        values.add(extractValueFromResourceOrLiteral(hasType));
      }
    }
    if (proxyType.getIncorporateList() != null) {
      List<Incorporates> incorporateList = proxyType.getIncorporateList();
      for (Incorporates incorporates : incorporateList) {
        values.add(extractValueFromResource(incorporates));
      }
    }
    if (proxyType.getIsDerivativeOfList() != null) {
      List<IsDerivativeOf> isDerivativeOfList = proxyType.getIsDerivativeOfList();
      for (IsDerivativeOf isDerivativeOf : isDerivativeOfList) {
        values.add(extractValueFromResource(isDerivativeOf));
      }
    }
    if (proxyType.getIsRelatedToList() != null) {
      List<IsRelatedTo> isRelatedToList = proxyType.getIsRelatedToList();
      for (IsRelatedTo isRelatedTo : isRelatedToList) {
        values.add(extractValueFromResourceOrLiteral(isRelatedTo));
      }
    }
    if (proxyType.getIsSimilarToList() != null) {
      List<IsSimilarTo> isSimilarToList = proxyType.getIsSimilarToList();
      for (IsSimilarTo isSimilarTo : isSimilarToList) {
        values.add(extractValueFromResource(isSimilarTo));
      }
    }
    if (proxyType.getIsSuccessorOfList() != null) {
      List<IsSuccessorOf> isSuccessorOfList = proxyType.getIsSuccessorOfList();
      for (IsSuccessorOf isSuccessorOf : isSuccessorOfList) {
        values.add(extractValueFromResource(isSuccessorOf));
      }
    }
    if (proxyType.getRealizeList() != null) {
      List<Realizes> realizesList = proxyType.getRealizeList();
      for (Realizes realizes : realizesList) {
        values.add(extractValueFromResource(realizes));
      }
    }
    if (proxyType.getCurrentLocation() != null) {
      values.add(extractValueFromResourceOrLiteral(proxyType.getCurrentLocation()));
    }
    if (proxyType.getChoiceList() != null) {
      List<ProxyType.Choice> choices = proxyType.getChoiceList();
      for (ProxyType.Choice choice : choices) {
        values.addAll(extractValueFromChoice(choice));
      }
    }
    return values;
  }

  private static Set<String> extractValueFromChoice(Choice choice) {
    Set<String> values = new HashSet<>();
    if (choice.ifSubject()) {
      values.add(extractValueFromResourceOrLiteral(choice.getSubject()));
    }
    if (choice.ifPublisher()) {
      values.add(extractValueFromResourceOrLiteral(choice.getPublisher()));
    }
    if (choice.ifReferences()) {
      values.add(extractValueFromResourceOrLiteral(choice.getReferences()));
    }
    if (choice.ifRelation()) {
      values.add(extractValueFromResourceOrLiteral(choice.getRelation()));
    }
    if (choice.ifReplaces()) {
      values.add(extractValueFromResourceOrLiteral(choice.getReplaces()));
    }
    if (choice.ifContributor()) {
      values.add(extractValueFromResourceOrLiteral(choice.getContributor()));
    }
    if (choice.ifCoverage()) {
      values.add(extractValueFromResourceOrLiteral(choice.getCoverage()));
    }
    if (choice.ifCreator()) {
      values.add(extractValueFromResourceOrLiteral(choice.getCreator()));
    }
    if (choice.ifCreated()) {
      values.add(extractValueFromResourceOrLiteral(choice.getCreated()));
    }
    if (choice.ifDate()) {
      values.add(extractValueFromResourceOrLiteral(choice.getDate()));
    }
    if (choice.ifExtent()) {
      values.add(extractValueFromResourceOrLiteral(choice.getExtent()));
    }
    if (choice.ifFormat()) {
      values.add(extractValueFromResourceOrLiteral(choice.getFormat()));
    }
    if (choice.ifHasFormat()) {
      values.add(extractValueFromResourceOrLiteral(choice.getHasFormat()));
    }
    if (choice.ifHasPart()) {
      values.add(extractValueFromResourceOrLiteral(choice.getHasPart()));
    }
    if (choice.ifHasVersion()) {
      values.add(extractValueFromResourceOrLiteral(choice.getHasVersion()));
    }
    if (choice.ifSource()) {
      values.add(extractValueFromResourceOrLiteral(choice.getSource()));
    }
    if (choice.ifSpatial()) {
      values.add(extractValueFromResourceOrLiteral(choice.getSpatial()));
    }
    if (choice.ifTemporal()) {
      values.add(extractValueFromResourceOrLiteral(choice.getTemporal()));
    }
    if (choice.ifIsFormatOf()) {
      values.add(extractValueFromResourceOrLiteral(choice.getIsFormatOf()));
    }
    if (choice.ifIsPartOf()) {
      values.add(extractValueFromResourceOrLiteral(choice.getIsPartOf()));
    }
    if (choice.ifIsReferencedBy()) {
      values.add(extractValueFromResourceOrLiteral(choice.getIsReferencedBy()));
    }
    if (choice.ifIsRequiredBy()) {
      values.add(extractValueFromResourceOrLiteral(choice.getIsRequiredBy()));
    }
    if (choice.ifIssued()) {
      values.add(extractValueFromResourceOrLiteral(choice.getIssued()));
    }
    if (choice.ifIsVersionOf()) {
      values.add(extractValueFromResourceOrLiteral(choice.getIsVersionOf()));
    }
    if (choice.ifMedium()) {
      values.add(extractValueFromResourceOrLiteral(choice.getMedium()));
    }
    if (choice.ifType()) {
      values.add(extractValueFromResourceOrLiteral(choice.getType()));
    }
    return values;
  }

  private static Set<String> dereferenceTimespanList(List<TimeSpanType> tsList) {
    Set<String> values = new HashSet<>();
    if (tsList != null) {
      for (TimeSpanType ts : tsList) {
        values.addAll(extractListOfValuesFromListFields(ts));
      }
    }
    return values;
  }

  private static Set<String> extractListOfValuesFromListFields(TimeSpanType ts) {
    Set<String> values = new HashSet<>();
    values.add(ts.getAbout());
    if (ts.getHasPartList() != null) {
      List<HasPart> hasPartList = ts.getHasPartList();
      for (HasPart hasPart : hasPartList) {
        values.add(extractValueFromResourceOrLiteral(hasPart));
      }
    }
    if (ts.getSameAList() != null) {
      List<SameAs> sameAsList = ts.getSameAList();
      for (SameAs sameAs : sameAsList) {
        values.add(extractValueFromResource(sameAs));
      }
    }
    if (ts.getIsPartOfList() != null) {
      List<IsPartOf> isPartOfList = ts.getIsPartOfList();
      for (IsPartOf isPartOf : isPartOfList) {
        values.add(extractValueFromResourceOrLiteral(isPartOf));
      }
    }
    return values;
  }

  private static Set<String> dereferenceAgentList(List<AgentType> agentList) {
    Set<String> values = new HashSet<>();
    if (agentList != null) {
      for (AgentType agent : agentList) {
        values.addAll(dererefenceAgent(agent));
      }
    }
    return values;
  }

  private static Set<String> dererefenceAgent(AgentType agent) {
    Set<String> values = new HashSet<>();
    values.add(agent.getAbout());
    if (agent.getHasMetList() != null) {
      for (HasMet hasMet : agent.getHasMetList()) {
        String hasMetResource = extractValueFromResource(hasMet);
        if (hasMetResource != null) {
          values.add(hasMetResource);
        }
      }
    }
    if (agent.getIsRelatedToList() != null) {
      for (IsRelatedTo hasMet : agent.getIsRelatedToList()) {
        String hasMetResource = extractValueFromResourceOrLiteral(hasMet);
        if (hasMetResource != null) {
          values.add(hasMetResource);
        }
      }
    }
    return values;
  }

  private static Set<String> dereferenceConceptList(List<Concept> conceptList) {
    Set<String> values = new HashSet<>();
    if (conceptList != null) {
      for (Concept concept : conceptList) {
        values.add(concept.getAbout());
        if (concept.getChoiceList() != null) {
          values.addAll(derefenceConceptChoiceList(concept.getChoiceList() ));
        }
      }
    }
    return values;
  }

  private static Set<String> derefenceConceptChoiceList(List<Concept.Choice> choiseList) {
    Set<String> values = new HashSet<>();
    for (Concept.Choice choice: choiseList) {
      if (choice.ifBroadMatch()) {
        String res = extractValueFromResource(choice.getBroadMatch());
        if (StringUtils.isNotEmpty(res)) {
          values.add(res);
        }
      }
      if (choice.ifCloseMatch()) {
        String res = extractValueFromResource(choice.getCloseMatch());
        if (StringUtils.isNotEmpty(res)) {
          values.add(res);
        }
      }
      if (choice.ifExactMatch()) {
        String res = extractValueFromResource(choice.getExactMatch());
        if (StringUtils.isNotEmpty(res)) {
          values.add(res);
        }
      }
      if (choice.ifNarrowMatch()) {
        String res = extractValueFromResource(choice.getNarrowMatch());
        if (StringUtils.isNotEmpty(res)) {
          values.add(res);
        }
      }
      if (choice.ifRelatedMatch()) {
        String res = extractValueFromResource(choice.getRelatedMatch());
        if (StringUtils.isNotEmpty(res)) {
          values.add(res);
        }
      }
      if (choice.ifRelated()) {
        String res = extractValueFromResource(choice.getRelated());
        if (StringUtils.isNotEmpty(res)) {
          values.add(res);
        }
      }
    }
    return values;
  }

  private static Set<String> dereferencePlaceList(List<PlaceType> placeList) {

    Set<String> values = new HashSet<>();
    if (placeList != null) {
      for (PlaceType place : placeList) {
        values.add(place.getAbout());
        if (place.getIsPartOfList() != null) {
          for (IsPartOf isPartOf : place.getIsPartOfList()) {
            values.add(extractValueFromResourceOrLiteral(isPartOf));
          }
        }
        if (place.getSameAList() != null) {
          for (SameAs sameAs : place.getSameAList()) {
            values.add(extractValueFromResource(sameAs));
          }
        }
        if (place.getHasPartList() != null) {
          for (HasPart hasPart : place.getHasPartList()) {
            values.add(extractValueFromResourceOrLiteral(hasPart));
          }
        }
      }
    }
    return values;
  }

  private static Set<String> dereferenceWebResourceList(List<WebResourceType> wrList) {
    Set<String> values = new HashSet<>();
    if (wrList != null) {
      for (WebResourceType wr : wrList) {
        values.addAll(dereferenceWebResource(wr));
      }
    }
    return values;
  }

  private static Set<String> dereferenceWebResource(WebResourceType wr) {
    Set<String> values = new HashSet<>();
    if (wr.getCreatedList() != null) {
      for (Created created : wr.getCreatedList()) {
        values.add(extractValueFromResourceOrLiteral(created));
      }
    }
    if (wr.getExtentList() != null) {
      for (Extent extent : wr.getExtentList()) {
        values.add(extractValueFromResourceOrLiteral(extent));
      }
    }
    if (wr.getFormatList() != null) {
      for (Format format : wr.getFormatList()) {
        values.add(extractValueFromResourceOrLiteral(format));
      }
    }
    if (wr.getHasPartList() != null) {
      for (HasPart hasPart : wr.getHasPartList()) {
        values.add(extractValueFromResourceOrLiteral(hasPart));
      }
    }
    if (wr.getIsFormatOfList() != null) {
      List<IsFormatOf> isFormatOfList = wr.getIsFormatOfList();
      for (IsFormatOf isFormatOf : isFormatOfList) {
        values.add(extractValueFromResourceOrLiteral(isFormatOf));
      }
    }
    if (wr.getIssuedList() != null) {
      List<Issued> issuedList = wr.getIssuedList();
      for (Issued issued : issuedList) {
        values.add(extractValueFromResourceOrLiteral(issued));
      }
    }
    return values;
  }

  private static <T extends ResourceOrLiteralType> String extractValueFromResourceOrLiteral(
      T type) {
    if (type.getResource() != null && StringUtils.isNotEmpty(type.getResource().getResource())) {
      return type.getResource().getResource();
    }
    return null;
  }

  private static <T extends ResourceType> String extractValueFromResource(T type) {
    if (StringUtils.isNotEmpty(type.getResource())) {
      return type.getResource();
    }
    return null;
  }
}
