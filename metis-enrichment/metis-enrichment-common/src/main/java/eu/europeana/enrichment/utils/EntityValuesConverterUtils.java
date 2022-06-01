package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.VcardAddress;
import eu.europeana.enrichment.api.external.model.VcardAddresses;
import eu.europeana.entitymanagement.definitions.model.Address;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EntityValuesConverterUtils {

    private EntityValuesConverterUtils() {
    }

    public static List<Label> convertMapToLabel(Map<String, String> values) {
        if (values == null) {
            return null;
        }
        List<Label> res = new ArrayList<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            res.add(new Label(entry.getKey(), entry.getValue()));
        }
        return res;
    }

    public static List<Label> convertMultilingualMapToLabel(Map<String, List<String>> values) {
        if (values == null) {
            return null;
        }
        List<Label> res = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : values.entrySet()) {
            List<String> entryValues = entry.getValue();
            for (String entryValue : entryValues) {
                res.add(new Label(entry.getKey(), entryValue));
            }
        }
        return res;
    }

    public static List<Label> convertListToLabel(List<String> values) {
        if (values == null) {
            return null;
        }
        List<Label> res = new ArrayList<>();
        values.forEach(value -> res.add(new Label(value)));
        return res;
    }

    public static List<Resource> convertListToResource(List<String> values) {
        if (values == null) {
            return null;
        }
        List<Resource> res = new ArrayList<>();
        values.forEach(value -> res.add(new Resource(value)));
        return res;
    }

    public static List<LabelResource> convertMultilingualMapToLabelResource(Map<String, List<String>> values) {
        if (values == null) {
            return null;
        }
        List<LabelResource> res = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : values.entrySet()) {
            List<String> entryValues = entry.getValue();
            for (String entryValue : entryValues) {
                res.add(new LabelResource(entry.getKey(), entryValue));
            }
        }
        return res;
    }

    public static List<LabelResource> convertListToLabelResource(List<String> values) {
        if (values == null) {
            return null;
        }
        List<LabelResource> res = new ArrayList<>();
        values.forEach(value -> res.add(new LabelResource(value)));
        return res;
    }

    public static List<Part> convertListToPart(List<String> values) {
        if (values == null) {
            return null;
        }
        List<Part> res = new ArrayList<>();
        values.forEach(value -> res.add(new Part(value)));
        return res;
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

}
