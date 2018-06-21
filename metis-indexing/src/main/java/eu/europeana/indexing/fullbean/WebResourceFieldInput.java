package eu.europeana.indexing.fullbean;

import java.util.List;
import java.util.Map;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.corelib.solr.entity.WebResourceImpl;

/**
 * Converts a {@link WebResourceType} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to a
 * {@link WebResourceImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
class WebResourceFieldInput {

  WebResourceImpl createWebResources(WebResourceType wResourceType) {

    WebResourceImpl webResource = new WebResourceImpl();
    webResource.setAbout(wResourceType.getAbout());

    Map<String, List<String>> desMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getDescriptionList());

    webResource.setDcDescription(desMap);

    Map<String, List<String>> forMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getFormatList());

    webResource.setDcFormat(forMap);

    Map<String, List<String>> sourceMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getSourceList());

    webResource.setDcSource(sourceMap);

    Map<String, List<String>> conformsToMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getConformsToList());

    webResource.setDctermsConformsTo(conformsToMap);

    Map<String, List<String>> createdMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getCreatedList());

    webResource.setDctermsCreated(createdMap);

    Map<String, List<String>> extentMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getExtentList());

    webResource.setDctermsExtent(extentMap);

    Map<String, List<String>> hasPartMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getHasPartList());

    webResource.setDctermsHasPart(hasPartMap);

    Map<String, List<String>> isFormatOfMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getIsFormatOfList());

    webResource.setDctermsIsFormatOf(isFormatOfMap);

    Map<String, List<String>> issuedMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getIssuedList());

    webResource.setDctermsIssued(issuedMap);
    Map<String, List<String>> rightMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getRightList());

    webResource.setWebResourceDcRights(rightMap);

    Map<String, List<String>> typeMap =
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getTypeList());
    webResource.setDcType(typeMap);

    Map<String, List<String>> edmRightsMap =
        FieldInputUtils.createResourceMapFromString(wResourceType.getRights());

    webResource.setWebResourceEdmRights(edmRightsMap);

    if (wResourceType.getIsNextInSequence() != null) {
      webResource.setIsNextInSequence(wResourceType.getIsNextInSequence().getResource());
    }
    webResource.setOwlSameAs(FieldInputUtils.resourceListToArray(wResourceType.getSameAList()));

    webResource.setDcCreator(
        FieldInputUtils.createResourceOrLiteralMapFromList(wResourceType.getCreatorList()));

    if (wResourceType.getHasServiceList() != null) {
      webResource.setSvcsHasService(
          FieldInputUtils.resourceListToArray(wResourceType.getHasServiceList()));
    }
    if (wResourceType.getIsReferencedByList() != null) {
      webResource.setDctermsIsReferencedBy(
          FieldInputUtils.resourceOrLiteralListToArray(wResourceType.getIsReferencedByList()));
    }
    if (wResourceType.getPreview() != null) {
      webResource.setEdmPreview(wResourceType.getPreview().getResource());
    }
    return webResource;
  }
}
