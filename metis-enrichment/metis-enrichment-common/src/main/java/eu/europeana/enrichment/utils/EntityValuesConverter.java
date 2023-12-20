package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.VcardAddress;
import eu.europeana.enrichment.api.external.model.VcardAddresses;
import eu.europeana.entitymanagement.definitions.model.Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class EntityValuesConverter {

  private EntityValuesConverter() {
  }

  public static List<Label> convertMapToLabels(Map<String, String> map) {
    List<Label> labels = new ArrayList<>();
    if (map == null) {
      return labels;
    }
    map.forEach((key, value) -> labels.add(new Label(key, value)));
    return labels;
  }

  public static List<Label> convertMultilingualMapToLabel(Map<String, List<String>> map) {
    List<Label> labels = new ArrayList<>();
    if (map == null) {
      return labels;
    }
    map.forEach(
        (key, entry) -> entry.stream().map(value -> new Label(key, value)).forEach(labels::add));
    return labels;
  }

  public static List<Label> convertListToLabel(List<String> values) {
    List<Label> labels = new ArrayList<>();
    if (values == null) {
      return labels;
    }
    values.forEach(value -> labels.add(new Label(value)));
    return labels;
  }

  public static List<Resource> convertListToResource(List<String> values) {
    List<Resource> resources = new ArrayList<>();
    if (values == null) {
      return resources;
    }
    values.forEach(value -> resources.add(new Resource(value)));
    return resources;
  }

  public static List<LabelResource> convertResourceOrLiteral(Map<String, List<String>> map) {
    List<LabelResource> parts = new ArrayList<>();
    if (map == null) {
      return parts;
    }
    map.forEach((key, entry) -> entry.stream()
                                     .map(value -> (isUri(key) ? new LabelResource(key) : new LabelResource(key, value)))
                                     .forEach(parts::add));
    return parts;
  }

  public static List<LabelResource> convertListToLabelResource(List<String> values) {
    List<LabelResource> labelResources = new ArrayList<>();
    if (values == null) {
      return labelResources;
    }
    values.forEach(value -> labelResources.add(new LabelResource(value)));
    return labelResources;
  }

  public static List<Part> convertListToPart(List<String> resources) {
    if (resources == null) {
      return new ArrayList<>();
    }
    return resources.stream().map(Part::new).toList();
  }

  public static List<Resource> convertToResourceList(String[] resources) {
    if (resources == null) {
      return new ArrayList<>();
    }
    return Arrays.stream(resources).map(Resource::new).toList();
  }

  public static VcardAddresses getVcardAddresses(Address address) {
    if (address != null && address.hasMetadataProperties()) {
      VcardAddresses vcardAddresses = new VcardAddresses();
      vcardAddresses.setVcardAddressesList(Collections.singletonList(getAddress(address)));
      return vcardAddresses;
    }
    return null;

  }

  private static VcardAddress getAddress(Address address) {
    VcardAddress vcardAddress = new VcardAddress();
    vcardAddress.setCountryName(address.getVcardCountryName());
    vcardAddress.setLocality(address.getVcardLocality());
    vcardAddress.setStreetAddress(address.getVcardStreetAddress());
    vcardAddress.setPostalCode(address.getVcardPostalCode());
    vcardAddress.setPostOfficeBox(address.getVcardPostOfficeBox());
    vcardAddress.setHasGeo(new Resource(address.getVcardHasGeo()));
    return vcardAddress;
  }

  private static boolean isUri(String str) {
    return str.startsWith("http://");
  }

}
