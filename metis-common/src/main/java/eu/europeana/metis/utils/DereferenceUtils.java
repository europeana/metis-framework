package eu.europeana.metis.utils;

import eu.europeana.corelib.definitions.jibx.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by gmamakis on 9-3-17.
 */
public class DereferenceUtils {
    private static IBindingFactory factory;

    private DereferenceUtils() throws JiBXException {
        factory = BindingDirectory.getFactory(RDF.class);
    }

    public static Set<String> extractValuesForDereferencing(String xml) throws JiBXException {
        Set<String> values = new HashSet<>();
        IUnmarshallingContext context = factory.createUnmarshallingContext();
        RDF rdf = (RDF) context.unmarshalDocument(IOUtils.toInputStream(xml), "UTF-8");
        List<AgentType> agentList = rdf.getAgentList();
        Set<String> agentUris = dereferenceAgentList(agentList);
        if (agentUris.size() > 0) {
            values.addAll(agentUris);
        }
        List<Concept> conceptList = rdf.getConceptList();
        Set<String> conceptUris = dereferenceConceptList(conceptList);
        if (conceptUris.size() > 0) {
            values.addAll(conceptUris);
        }
        List<PlaceType> placeList = rdf.getPlaceList();
        Set<String> placeUris = dereferencePlaceList(placeList);
        if (placeUris.size() > 0) {
            values.addAll(placeUris);
        }
        List<TimeSpanType> tsList = rdf.getTimeSpanList();
        Set<String> tsUris = dereferenceTimespanList(tsList);
        if (tsUris.size() > 0) {
            values.addAll(tsUris);
        }
        List<WebResourceType> wrList = rdf.getWebResourceList();
        Set<String> wrUris = dereferenceWrList(wrList);
        if (wrUris.size() > 0) {
            values.addAll(wrUris);
        }
        List<ProxyType> proxyList = rdf.getProxyList();
        Set<String> prUris = dereferenceProxyList(proxyList);
        if (prUris.size() > 0) {
            values.addAll(prUris);
        }
        return values;
    }

    private static Set<String> dereferenceProxyList(List<ProxyType> proxyList) {
        Set<String> values = new HashSet<>();
        if (proxyList != null && proxyList.size() > 0) {
            for (ProxyType proxyType : proxyList) {
                if (proxyType.getHasMetList() != null && proxyType.getHasMetList().size() > 0) {
                    List<HasMet> hasMetList = proxyType.getHasMetList();
                    for (HasMet hasMet : hasMetList) {
                        values.add(extractValueFromResource(hasMet));
                    }
                }
                if (proxyType.getHasTypeList() != null && proxyType.getHasTypeList().size() > 0) {
                    List<HasType> hasTypeList = proxyType.getHasTypeList();
                    for (HasType hasType : hasTypeList) {
                        values.add(extractValueFromResourceOrLiteral(hasType));
                    }
                }
                if (proxyType.getIncorporateList() != null && proxyType.getIncorporateList().size() > 0) {
                    List<Incorporates> incorporateList = proxyType.getIncorporateList();
                    for (Incorporates incorporates : incorporateList) {
                        values.add(extractValueFromResource(incorporates));
                    }
                }
                if (proxyType.getIsDerivativeOfList() != null && proxyType.getIsDerivativeOfList().size() > 0) {
                    List<IsDerivativeOf> isDerivativeOfList = proxyType.getIsDerivativeOfList();
                    for (IsDerivativeOf isDerivativeOf : isDerivativeOfList) {
                        values.add(extractValueFromResource(isDerivativeOf));
                    }
                }
                if (proxyType.getIsRelatedToList() != null && proxyType.getIsRelatedToList().size() > 0) {
                    List<IsRelatedTo> isRelatedToList = proxyType.getIsRelatedToList();
                    for (IsRelatedTo isRelatedTo : isRelatedToList) {
                        values.add(extractValueFromResourceOrLiteral(isRelatedTo));
                    }
                }
                if (proxyType.getIsSimilarToList() != null && proxyType.getIsSimilarToList().size() > 0) {
                    List<IsSimilarTo> isSimilarToList = proxyType.getIsSimilarToList();
                    for (IsSimilarTo isSimilarTo : isSimilarToList) {
                        values.add(extractValueFromResource(isSimilarTo));
                    }
                }
                if (proxyType.getIsSuccessorOfList() != null && proxyType.getIsSuccessorOfList().size() > 0) {
                    List<IsSuccessorOf> isSuccessorOfList = proxyType.getIsSuccessorOfList();
                    for (IsSuccessorOf isSuccessorOf : isSuccessorOfList) {
                        values.add(extractValueFromResource(isSuccessorOf));
                    }
                }
                if (proxyType.getRealizeList() != null && proxyType.getRealizeList().size() > 0) {
                    List<Realizes> realizesList = proxyType.getRealizeList();
                    for (Realizes realizes : realizesList) {
                        values.add(extractValueFromResource(realizes));
                    }
                }
                if (proxyType.getCurrentLocation() != null) {
                    values.add(extractValueFromResource(proxyType.getCurrentLocation()));
                }
                if(proxyType.getChoiceList()!=null && proxyType.getChoiceList().size()>0){
                    List<ProxyType.Choice> choices = proxyType.getChoiceList();
                    for(ProxyType.Choice choice:choices){
                        if(choice.ifSubject()){
                            values.add(extractValueFromResourceOrLiteral(choice.getSubject()));
                        }
                        if(choice.ifPublisher()){
                            values.add(extractValueFromResourceOrLiteral(choice.getPublisher()));
                        }
                        if(choice.ifReferences()){
                            values.add(extractValueFromResourceOrLiteral(choice.getReferences()));
                        }
                        if(choice.ifRelation()){
                            values.add(extractValueFromResourceOrLiteral(choice.getRelation()));
                        }
                        if(choice.ifReplaces()){
                            values.add(extractValueFromResourceOrLiteral(choice.getReplaces()));
                        }
                        if(choice.ifContributor()){
                            values.add(extractValueFromResourceOrLiteral(choice.getContributor()));
                        }
                        if(choice.ifCoverage()){
                            values.add(extractValueFromResourceOrLiteral(choice.getCoverage()));
                        }
                        if(choice.ifCreator()){
                            values.add(extractValueFromResourceOrLiteral(choice.getCreator()));
                        }
                        if(choice.ifCreated()){
                            values.add(extractValueFromResourceOrLiteral(choice.getCreated()));
                        }
                        if(choice.ifDate()){
                            values.add(extractValueFromResourceOrLiteral(choice.getDate()));
                        }
                        if(choice.ifExtent()){
                            values.add(extractValueFromResourceOrLiteral(choice.getExtent()));
                        }
                        if(choice.ifFormat()){
                            values.add(extractValueFromResourceOrLiteral(choice.getFormat()));
                        }
                        if(choice.ifHasFormat()){
                            values.add(extractValueFromResourceOrLiteral(choice.getHasFormat()));
                        }
                        if(choice.ifHasPart()){
                            values.add(extractValueFromResourceOrLiteral(choice.getHasPart()));
                        }
                        if(choice.ifHasVersion()){
                            values.add(extractValueFromResourceOrLiteral(choice.getHasVersion()));
                        }
                        if(choice.ifSource()){
                            values.add(extractValueFromResourceOrLiteral(choice.getSource()));
                        }
                        if(choice.ifSpatial()){
                            values.add(extractValueFromResourceOrLiteral(choice.getSpatial()));
                        }
                        if(choice.ifTemporal()){
                            values.add(extractValueFromResourceOrLiteral(choice.getTemporal()));
                        }
                        if(choice.ifIsFormatOf()){
                            values.add(extractValueFromResourceOrLiteral(choice.getIsFormatOf()));
                        }
                        if(choice.ifIsPartOf()){
                            values.add(extractValueFromResourceOrLiteral(choice.getIsPartOf()));
                        }
                        if(choice.ifIsReferencedBy()){
                            values.add(extractValueFromResourceOrLiteral(choice.getIsReferencedBy()));
                        }
                        if(choice.ifIsRequiredBy()){
                            values.add(extractValueFromResourceOrLiteral(choice.getIsRequiredBy()));
                        }
                        if(choice.ifIssued()){
                            values.add(extractValueFromResourceOrLiteral(choice.getIssued()));
                        }
                        if(choice.ifIsVersionOf()){
                            values.add(extractValueFromResourceOrLiteral(choice.getIsVersionOf()));
                        }
                        if(choice.ifMedium()){
                            values.add(extractValueFromResourceOrLiteral(choice.getMedium()));
                        }
                        if(choice.ifType()){
                            values.add(extractValueFromResourceOrLiteral(choice.getType()));
                        }
                    }

                }
            }
        }
        return values;
    }

    private static Set<String> dereferenceTimespanList(List<TimeSpanType> tsList) {
        Set<String> values = new HashSet<>();
        if (tsList != null && tsList.size() > 0) {
            for (TimeSpanType ts : tsList) {
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
            }
        }
        return values;
    }

    private static Set<String> dereferenceAgentList(List<AgentType> agentList) {
        Set<String> values = new HashSet<>();
        if (agentList != null && agentList.size() > 0) {
            for (AgentType agent : agentList) {
                values.add(agent.getAbout());
                if (agent.getHasMetList() != null) {
                    List<HasMet> hasMets = agent.getHasMetList();
                    for (HasMet hasMet : hasMets) {
                        String hasMetResource = extractValueFromResource(hasMet);
                        if (hasMetResource != null) {
                            values.add(hasMetResource);
                        }
                    }
                }
                if (agent.getIsRelatedToList() != null) {
                    List<IsRelatedTo> hasMets = agent.getIsRelatedToList();
                    for (IsRelatedTo hasMet : hasMets) {
                        String hasMetResource = extractValueFromResourceOrLiteral(hasMet);
                        if (hasMetResource != null) {
                            values.add(hasMetResource);
                        }
                    }
                }
            }
        }
        return values;
    }

    private static Set<String> dereferenceConceptList(List<Concept> conceptList) {

        Set<String> values = new HashSet<>();
        if (conceptList != null && conceptList.size() > 0) {
            for (Concept concept : conceptList) {
                values.add(concept.getAbout());
                if (concept.getChoiceList() != null) {
                    List<Concept.Choice> choiceList = concept.getChoiceList();
                    for (Concept.Choice choice : choiceList) {
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
                }

            }
        }
        return values;
    }

    private static Set<String> dereferencePlaceList(List<PlaceType> placeList) {

        Set<String> values = new HashSet<>();
        if (placeList != null && placeList.size() > 0) {
            for (PlaceType place : placeList) {
                values.add(place.getAbout());
                if (place.getIsPartOfList() != null) {
                    List<IsPartOf> isPartOfList = place.getIsPartOfList();
                    for (IsPartOf isPartOf : isPartOfList) {
                        values.add(extractValueFromResourceOrLiteral(isPartOf));
                    }
                }
                if (place.getSameAList() != null) {
                    List<SameAs> sameAsList = place.getSameAList();
                    for (SameAs sameAs : sameAsList) {
                        values.add(extractValueFromResource(sameAs));
                    }
                }
                if (place.getHasPartList() != null) {
                    List<HasPart> hasParts = place.getHasPartList();
                    for (HasPart hasPart : hasParts) {
                        values.add(extractValueFromResourceOrLiteral(hasPart));
                    }
                }
            }
        }
        return values;
    }

    private static Set<String> dereferenceWrList(List<WebResourceType> wrList) {

        Set<String> values = new HashSet<>();
        if (wrList != null && wrList.size() > 0) {
            for (WebResourceType wr : wrList) {
                if (wr.getCreatedList() != null) {
                    List<Created> createdList = wr.getCreatedList();
                    for (Created created : createdList) {
                        values.add(extractValueFromResourceOrLiteral(created));
                    }
                }
                if (wr.getExtentList() != null) {
                    List<Extent> extentList = wr.getExtentList();
                    for (Extent extent : extentList) {
                        values.add(extractValueFromResourceOrLiteral(extent));
                    }
                }
                if (wr.getFormatList() != null) {
                    List<Format> formatList = wr.getFormatList();
                    for (Format format : formatList) {
                        values.add(extractValueFromResourceOrLiteral(format));
                    }
                }
                if (wr.getHasPartList() != null) {
                    List<HasPart> hasPartList = wr.getHasPartList();
                    for (HasPart hasPart : hasPartList) {
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
            }
        }
        return values;
    }

    private static <T extends ResourceOrLiteralType> String extractValueFromResourceOrLiteral(T type) {
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
