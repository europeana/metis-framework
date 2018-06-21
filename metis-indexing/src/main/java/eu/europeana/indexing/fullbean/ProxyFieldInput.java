package eu.europeana.indexing.fullbean;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import eu.europeana.corelib.definitions.jibx.IsNextInSequence;
import eu.europeana.corelib.definitions.jibx.ProxyFor;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.solr.entity.ProxyImpl;

/**
 * Converts a {@link ProxyType} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to a
 * {@link ProxyImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class ProxyFieldInput {

  ProxyImpl createProxyMongoFields(ProxyImpl mongoProxy, ProxyType proxy) {

    mongoProxy.setAbout(proxy.getAbout());
    if (proxy.getEuropeanaProxy() != null) {
      mongoProxy.setEuropeanaProxy(proxy.getEuropeanaProxy().isEuropeanaProxy());
    }
    mongoProxy.setEdmCurrentLocation(
        FieldInputUtils.createResourceOrLiteralMapFromString(proxy.getCurrentLocation()));

    List<IsNextInSequence> seqList = proxy.getIsNextInSequenceList();

    if (seqList != null) {

      String[] seqarray = new String[seqList.size()];
      for (int i = 0; i < seqarray.length; i++) {
        seqarray[i] = seqList.get(i).getResource();
      }
      mongoProxy.setEdmIsNextInSequence(seqarray);
    }
    String docType = FieldInputUtils.exists(String::new, (proxy.getType().getType().xmlValue()));

    mongoProxy.setEdmType(DocType.safeValueOf(docType));

    mongoProxy
        .setProxyFor(FieldInputUtils.exists(ProxyFor::new, proxy.getProxyFor()).getResource());
    mongoProxy.setProxyIn(FieldInputUtils.resourceListToArray(proxy.getProxyInList()));
    mongoProxy.setEdmHasMet(FieldInputUtils.createResourceMapFromList(proxy.getHasMetList()));
    mongoProxy.setYear(FieldInputUtils.createLiteralMapFromList(proxy.getYearList()));
    mongoProxy
        .setEdmHasType(FieldInputUtils.createResourceOrLiteralMapFromList(proxy.getHasTypeList()));
    mongoProxy
        .setEdmHasType(FieldInputUtils.createResourceOrLiteralMapFromList(proxy.getHasTypeList()));
    mongoProxy.setEdmIncorporates(FieldInputUtils.resourceListToArray(proxy.getIncorporateList()));
    mongoProxy
        .setEdmIsDerivativeOf(FieldInputUtils.resourceListToArray(proxy.getIsDerivativeOfList()));
    mongoProxy.setEdmIsRelatedTo(
        FieldInputUtils.createResourceOrLiteralMapFromList(proxy.getIsRelatedToList()));
    if (proxy.getIsRepresentationOf() != null) {
      mongoProxy.setEdmIsRepresentationOf(proxy.getIsRepresentationOf().getResource());
    }
    mongoProxy.setEdmIsSimilarTo(FieldInputUtils.resourceListToArray(proxy.getIsSimilarToList()));
    mongoProxy.setEdmRealizes(FieldInputUtils.resourceListToArray(proxy.getRealizeList()));
    mongoProxy
        .setEdmIsSuccessorOf(FieldInputUtils.resourceListToArray(proxy.getIsSuccessorOfList()));
    List<eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice> europeanaTypeList =
        proxy.getChoiceList();
    if (europeanaTypeList != null) {
      for (eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice europeanaType : europeanaTypeList) {
        if (europeanaType.ifAlternative()) {
          if (mongoProxy.getDctermsAlternative() == null) {
            mongoProxy.setDctermsAlternative(
                FieldInputUtils.createLiteralMapFromString(europeanaType.getAlternative()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsAlternative();
            Map<String, List<String>> retMap =
                FieldInputUtils.createLiteralMapFromString(europeanaType.getAlternative());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsAlternative(tempMap);
          }
        }
        if (europeanaType.ifConformsTo()) {
          if (mongoProxy.getDctermsConformsTo() == null) {
            mongoProxy.setDctermsConformsTo(FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getConformsTo()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsConformsTo();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getConformsTo());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsConformsTo(tempMap);
          }
        }
        if (europeanaType.ifCreated()) {
          if (mongoProxy.getDctermsCreated() == null) {
            mongoProxy.setDctermsCreated(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getCreated()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsCreated();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getCreated());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsCreated(tempMap);
          }
        }
        if (europeanaType.ifExtent()) {
          if (mongoProxy.getDctermsExtent() == null) {
            mongoProxy.setDctermsExtent(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getExtent()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsExtent();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getExtent());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsExtent(tempMap);
          }
        }
        if (europeanaType.ifHasFormat()) {
          if (mongoProxy.getDctermsHasFormat() == null) {
            mongoProxy.setDctermsHasFormat(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getHasFormat()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsHasFormat();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getHasFormat());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsHasFormat(tempMap);
          }
        }
        if (europeanaType.ifHasPart()) {
          if (mongoProxy.getDctermsHasPart() == null) {
            mongoProxy.setDctermsHasPart(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getHasPart()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsHasPart();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getHasPart());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsHasPart(tempMap);
          }
        }
        if (europeanaType.ifHasVersion()) {
          if (mongoProxy.getDctermsHasVersion() == null) {
            mongoProxy.setDctermsHasVersion(FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getHasVersion()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsHasVersion();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getHasVersion());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsHasVersion(tempMap);
          }
        }

        if (europeanaType.ifIsFormatOf()) {
          if (mongoProxy.getDctermsIsFormatOf() == null) {
            mongoProxy.setDctermsIsFormatOf(FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getIsFormatOf()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsIsFormatOf();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getIsFormatOf());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsIsFormatOf(tempMap);
          }
        }

        if (europeanaType.ifIsPartOf()) {
          if (mongoProxy.getDctermsIsPartOf() == null) {
            mongoProxy.setDctermsIsPartOf(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getIsPartOf()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsIsPartOf();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getIsPartOf());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsIsPartOf(tempMap);
          }
        }
        if (europeanaType.ifIsReferencedBy()) {
          if (mongoProxy.getDctermsIsReferencedBy() == null) {
            mongoProxy.setDctermsIsReferencedBy(FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getIsReferencedBy()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsIsReferencedBy();
            Map<String, List<String>> retMap = FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getIsReferencedBy());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsIsReferencedBy(tempMap);
          }
        }
        if (europeanaType.ifIsReplacedBy()) {
          if (mongoProxy.getDctermsIsReplacedBy() == null) {
            mongoProxy.setDctermsIsReplacedBy(FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getIsReplacedBy()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsIsReplacedBy();
            Map<String, List<String>> retMap = FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getIsReplacedBy());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsIsReplacedBy(tempMap);
          }
        }

        if (europeanaType.ifIsRequiredBy()) {
          if (mongoProxy.getDctermsIsRequiredBy() == null) {
            mongoProxy.setDctermsIsRequiredBy(FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getIsRequiredBy()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsIsRequiredBy();
            Map<String, List<String>> retMap = FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getIsRequiredBy());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsIsRequiredBy(tempMap);
          }
        }
        if (europeanaType.ifIssued()) {
          if (mongoProxy.getDctermsIssued() == null) {
            mongoProxy.setDctermsIssued(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getIssued()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsIssued();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getIssued());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsIssued(tempMap);
          }
        }

        if (europeanaType.ifIsVersionOf()) {
          if (mongoProxy.getDctermsIsVersionOf() == null) {
            mongoProxy.setDctermsIsVersionOf(FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getIsVersionOf()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsIsVersionOf();
            Map<String, List<String>> retMap = FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getIsVersionOf());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsIsVersionOf(tempMap);
          }
        }

        if (europeanaType.ifMedium()) {
          if (mongoProxy.getDctermsMedium() == null) {
            mongoProxy.setDctermsMedium(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getMedium()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsMedium();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getMedium());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsMedium(tempMap);
          }
        }

        if (europeanaType.ifProvenance()) {
          if (mongoProxy.getDctermsProvenance() == null) {
            mongoProxy.setDctermsProvenance(FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getProvenance()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsProvenance();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getProvenance());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsProvenance(tempMap);
          }
        }
        if (europeanaType.ifReferences()) {
          if (mongoProxy.getDctermsReferences() == null) {
            mongoProxy.setDctermsReferences(FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getReferences()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsReferences();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getReferences());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsReferences(tempMap);
          }
        }

        if (europeanaType.ifReplaces()) {
          if (mongoProxy.getDctermsReplaces() == null) {
            mongoProxy.setDctermsReplaces(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getReplaces()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsReplaces();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getReplaces());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsReplaces(tempMap);
          }
        }

        if (europeanaType.ifRequires()) {
          if (mongoProxy.getDctermsRequires() == null) {
            mongoProxy.setDctermsRequires(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getRequires()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsRequires();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getRequires());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsRequires(tempMap);
          }
        }
        if (europeanaType.ifSpatial()) {
          if (mongoProxy.getDctermsSpatial() == null) {
            mongoProxy.setDctermsSpatial(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getSpatial()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsSpatial();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getSpatial());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsSpatial(tempMap);
          }
        }
        if (europeanaType.ifTableOfContents()) {
          if (mongoProxy.getDctermsTOC() == null) {
            mongoProxy.setDctermsTOC(FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getTableOfContents()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsTOC();
            Map<String, List<String>> retMap = FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getTableOfContents());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsTOC(tempMap);
          }
        }
        if (europeanaType.ifTemporal()) {
          if (mongoProxy.getDctermsTemporal() == null) {
            mongoProxy.setDctermsTemporal(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getTemporal()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDctermsTemporal();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getTemporal());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDctermsTemporal(tempMap);
          }
        }
        if (europeanaType.ifContributor()) {
          if (mongoProxy.getDcContributor() == null) {
            mongoProxy.setDcContributor(FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getContributor()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcContributor();
            Map<String, List<String>> retMap = FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getContributor());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcContributor(tempMap);
          }
        }
        if (europeanaType.ifCoverage()) {
          if (mongoProxy.getDcCoverage() == null) {
            mongoProxy.setDcCoverage(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getCoverage()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcCoverage();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getCoverage());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcCoverage(tempMap);
          }
        }

        if (europeanaType.ifCreator()) {
          if (mongoProxy.getDcCreator() == null) {
            mongoProxy.setDcCreator(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getCreator()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcCreator();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getCreator());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcCreator(tempMap);
          }
        }
        if (europeanaType.ifDate()) {
          if (mongoProxy.getDcDate() == null) {
            mongoProxy.setDcDate(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getDate()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcDate();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getDate());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcDate(tempMap);
          }
        }

        if (europeanaType.ifDescription()) {
          if (mongoProxy.getDcDescription() == null) {
            mongoProxy.setDcDescription(FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getDescription()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcDescription();
            Map<String, List<String>> retMap = FieldInputUtils
                .createResourceOrLiteralMapFromString(europeanaType.getDescription());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcDescription(tempMap);
          }
        }
        if (europeanaType.ifFormat()) {
          if (mongoProxy.getDcFormat() == null) {
            mongoProxy.setDcFormat(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getFormat()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcFormat();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getFormat());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcFormat(tempMap);
          }
        }

        if (europeanaType.ifIdentifier()) {
          if (mongoProxy.getDcIdentifier() == null) {
            mongoProxy.setDcIdentifier(
                FieldInputUtils.createLiteralMapFromString(europeanaType.getIdentifier()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcIdentifier();
            Map<String, List<String>> retMap =
                FieldInputUtils.createLiteralMapFromString(europeanaType.getIdentifier());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcIdentifier(tempMap);
          }
        }
        if (europeanaType.ifLanguage()) {
          if (mongoProxy.getDcLanguage() == null) {
            mongoProxy.setDcLanguage(
                FieldInputUtils.createLiteralMapFromString(europeanaType.getLanguage()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcLanguage();
            Map<String, List<String>> retMap =
                FieldInputUtils.createLiteralMapFromString(europeanaType.getLanguage());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcLanguage(tempMap);
          }
        }
        if (europeanaType.ifPublisher()) {
          if (mongoProxy.getDcPublisher() == null) {
            mongoProxy.setDcPublisher(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getPublisher()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcPublisher();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getPublisher());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcPublisher(tempMap);
          }
        }

        if (europeanaType.ifRelation()) {
          if (mongoProxy.getDcRelation() == null) {
            mongoProxy.setDcRelation(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getRelation()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcRelation();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getRelation());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcRelation(tempMap);
          }
        }
        if (europeanaType.ifRights()) {
          if (mongoProxy.getDcRights() == null) {
            mongoProxy.setDcRights(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getRights()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcRights();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getRights());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcRights(tempMap);
          }
        }

        if (europeanaType.ifSource()) {
          if (mongoProxy.getDcSource() == null) {
            mongoProxy.setDcSource(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getSource()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcSource();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getSource());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcSource(tempMap);
          }
        }

        if (europeanaType.ifSubject()) {
          if (mongoProxy.getDcSubject() == null) {
            mongoProxy.setDcSubject(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getSubject()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcSubject();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getSubject());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcSubject(tempMap);
          }
        }

        if (europeanaType.ifTitle()) {
          if (mongoProxy.getDcTitle() == null) {
            mongoProxy
                .setDcTitle(FieldInputUtils.createLiteralMapFromString(europeanaType.getTitle()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcTitle();
            Map<String, List<String>> retMap =
                FieldInputUtils.createLiteralMapFromString(europeanaType.getTitle());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcTitle(tempMap);
          }
        }
        if (europeanaType.ifType()) {
          if (mongoProxy.getDcType() == null) {
            mongoProxy.setDcType(
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getType()));
          } else {
            Map<String, List<String>> tempMap = mongoProxy.getDcType();
            Map<String, List<String>> retMap =
                FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getType());
            addValuesFromMapToMap(tempMap, retMap);
            mongoProxy.setDcType(tempMap);
          }
        }
      }
    }
    return mongoProxy;
  }

  private static void addValuesFromMapToMap(Map<String, List<String>> tempMap,
      Map<String, List<String>> retMap) {
    if (retMap != null && tempMap != null) {
      for (Entry<String, List<String>> entry : retMap.entrySet()) {
        if (tempMap.containsKey(entry.getKey())) {
          List<String> values = tempMap.get(entry.getKey());
          values.addAll(retMap.get(entry.getKey()));
        } else {
          tempMap.put(entry.getKey(), entry.getValue());
        }
      }
    }
  }
}
