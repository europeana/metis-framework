package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.external.model.*;
import eu.europeana.entitymanagement.definitions.model.Address;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EntityXmlUtils {

    private EntityXmlUtils() {
    }

    public static List<Label> convertMapToXmlLabel(Map<String, String> values) {
        if (values == null) {
            return null;
        }
        List<Label> res = new ArrayList<>();
        for (Map.Entry<String,String> entry : values.entrySet()) {
            res.add(new Label(entry.getKey(), entry.getValue()));
        }
        return res;
    }

    public static List<Label> convertMultilingualMapToXmlLabel(Map<String, List<String>> values) {
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

    public static List<Label> convertListToXmlLabel(List<String> values) {
        if (values == null) {
            return null;
        }
        List<Label> res = new ArrayList<>();
        values.stream().forEach(value -> res.add(new Label(value)));
        return res;
    }

    public static List<Resource> convertListToXmlResource(List<String> values) {
        if (values == null) {
            return null;
        }
        List<Resource> res = new ArrayList<>();
        values.stream().forEach( value -> res.add(new Resource(value)));
        return res;
    }

    public static List<LabelResource> convertMultilingualMapToXmlLabelResource(Map<String, List<String>> values) {
        if (values == null) {
            return null;
        }
        List<LabelResource> res = new ArrayList<>();
        for (Map.Entry<String,List<String>> entry : values.entrySet()) {
            List<String> entryValues = entry.getValue();
            for (String entryValue : entryValues) {
                res.add(new LabelResource(entry.getKey(), entryValue));
            }
        }
        return res;
    }

    public static List<LabelResource> convertListToXmlLabelResource(List<String> values) {
        if (values == null) {
            return null;
        }
        List<LabelResource> res = new ArrayList<>();
        values.stream().forEach(value -> res.add(new LabelResource("def", value)));
        return res;
    }

    public static List<Part> convertListToXmlPart(List<String> values) {
        if (values == null) {
            return null;
        }
        List<Part> res = new ArrayList<>();
        values.stream().forEach(value -> res.add(new Part(value)));
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
