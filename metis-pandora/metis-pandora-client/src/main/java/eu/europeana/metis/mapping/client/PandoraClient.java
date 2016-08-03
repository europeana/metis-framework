package eu.europeana.metis.mapping.client;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.model.MappingSchema;
import eu.europeana.metis.mapping.statistics.DatasetStatistics;
import eu.europeana.metis.mapping.validation.AttributeFlagDTO;
import eu.europeana.metis.mapping.validation.ElementFlagDTO;
import eu.europeana.metis.mapping.validation.Flag;
import eu.europeana.metis.mapping.validation.FlagType;
import eu.europeana.metis.mapping.xsd.FileXSDUploadDTO;
import eu.europeana.metis.mapping.xsd.UrlXSDUploadDTO;
import org.apache.commons.io.FileUtils;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * REST Client for Pandora
 * Created by ymamakis on 8/1/16.
 */
public class PandoraClient {
    private RestTemplate template = new RestTemplate();
    private String restEndpoint;

    public PandoraClient() {
        Properties props = new Properties();
        try {
            props.load(this.getClass().getClassLoader().getResourceAsStream("client.properties"));
            restEndpoint = props.getProperty("pandora.rest");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String createMapping(Mapping mapping) {
        return template.postForObject(restEndpoint + RestEndpoints.MAPPING, mapping, String.class);
    }

    public void updateMapping(Mapping mapping) {
        template.put(restEndpoint + RestEndpoints.MAPPING, mapping);
    }

    public void deleteMapping(Mapping mapping) {
        template.delete(restEndpoint + RestEndpoints.MAPPING, mapping);
    }

    public Mapping getMappingById(String id) {
        return template.getForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.MAPPING_BYID, id), Mapping.class);
    }

    public Mapping getMappingByName(String name) {
        return template.getForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.MAPPING_DATASETNAME, name), Mapping.class);
    }

    public List<Mapping> getMappingsByOrgId(String orgId) {
        return template.getForObject(restEndpoint + RestEndpoints.resolve(
                RestEndpoints.MAPPINGS_BYORGANIZATIONID, orgId), List.class);
    }

    public List<String> getMappingNamesByOrgId(String orgId) {
        return template.getForObject(restEndpoint + RestEndpoints.resolve(
                RestEndpoints.MAPPINGS_NAMES_BYORGANIZATIONID, orgId), List.class);
    }

    public List<Mapping> getMappingTemplates() {
        return template.getForObject(restEndpoint + RestEndpoints.MAPPING_TEMPLATES, List.class);
    }

    public void deleteStatisticsForMapping(String name) {
        template.delete(restEndpoint + RestEndpoints.resolve(RestEndpoints.MAPPING_STATISTICS_BYNAME, name));
    }

    public void setSchematroRulesForMapping(String mappingId , Set<String> rules) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("rules", rules);
        params.add("mappingId",mappingId);
        template.put(restEndpoint + RestEndpoints.MAPPING_SCHEMATRON, params);
    }

    public void setNamespacesForMapping(String mappingId, Map<String, String> namespaces) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("namespaces", namespaces);
        params.add("mappingId",mappingId);
        template.put(restEndpoint + RestEndpoints.MAPPING_NAMESPACES, params);
    }

    public DatasetStatistics calculateStatisticsForMapping(String datasetId, File file) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("file", file);
        return template.postForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.STATISTICS_CALCULATE, datasetId),
                params, DatasetStatistics.class);
    }

    public void appendStatisticsToMapping(String datasetId, Mapping mapping) {
        template.put(restEndpoint + RestEndpoints.resolve(RestEndpoints.STATISTICS_APPEND, datasetId),
                mapping);
    }

    public List<Flag> validateAttribute(String mappingId, Attribute attr) {
        return template.postForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.VALIDATE_ATTRIBUTE, mappingId),
                attr, List.class);
    }

    public List<Flag> validateElement(String mappingId, Element elem) {
        return template.postForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.VALIDATE_ELEMENT, mappingId),
                elem, List.class);
    }

    public Flag createAttributeFlag(String mappingId, FlagType type, String value, String message, Attribute attr) {

        AttributeFlagDTO dto = new AttributeFlagDTO();
        dto.setAttr(attr);
        dto.setMessage(message);
        return template.postForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.VALIDATE_CREATE_ATTTRIBUTE_FLAG, mappingId, value, type.name()),
                dto, Flag.class);
    }

    public Flag createElementFlag(String mappingId, FlagType type, String value, String message, Element elem) {
        ElementFlagDTO dto = new ElementFlagDTO();
        dto.setMessage(message);
        dto.setElem(elem);
        return template.postForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.VALIDATE_CREATE_ELEMENT_FLAG, mappingId, value, type.name()),
                dto, Flag.class);
    }

    public void deleteAttributeFlag(String mappingId, String value, Attribute attr) {
        template.delete(restEndpoint + RestEndpoints.resolve(RestEndpoints.VALIDATE_DELETE_ATTRIBUTE_FLAG, mappingId,value),attr);
    }

    public void deleteElementFlag(String mappingId, String value, Element element) {
        template.delete(restEndpoint + RestEndpoints.resolve(RestEndpoints.VALIDATE_DELETE_ATTRIBUTE_FLAG, mappingId,value),element);
    }

    public Mapping validateMapping(Mapping mapping) {
        return template.postForObject(restEndpoint + RestEndpoints.VALIDATE_MAPPING,
                mapping, Mapping.class);
    }

    public String uploadTemplateFromFile(String rootFile, String mappingName, String rootXPath, Map<String,String> namespaces,
                                          File file, MappingSchema schema) throws IOException {
        FileXSDUploadDTO dto = new FileXSDUploadDTO();
        dto.setFile(FileUtils.readFileToByteArray(file));
        dto.setMappingName(mappingName);
        dto.setRootFile(rootFile);
        dto.setNamespaces(namespaces);
        dto.setRootXPath(rootXPath);
        dto.setSchema(schema);
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type",MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<FileXSDUploadDTO> entity = new HttpEntity<>(dto,headers);
        return template.postForObject(restEndpoint + RestEndpoints.XSD_UPLOAD,
                entity, String.class);
    }

    public String uploadTemplateFromUrl(String rootFile, String mappingName, String rootXPath, Map<String,String> namespaces,
                                         String url, MappingSchema schema) {
        UrlXSDUploadDTO dto = new UrlXSDUploadDTO();
        dto.setUrl(url);
        dto.setMappingName(mappingName);
        dto.setRootFile(rootFile);
        dto.setNamespaces(namespaces);
        dto.setRootXPath(rootXPath);
        dto.setSchema(schema);
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type",MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<UrlXSDUploadDTO> entity = new HttpEntity<>(dto,headers);
        return template.postForObject(restEndpoint + RestEndpoints.XSD_URL,
                entity, String.class);
    }

    public String createXslFromMapping(Mapping mapping) {
        return template.postForObject(restEndpoint + RestEndpoints.XSL_GENERATE,
                mapping, String.class);
    }

    public byte[] downloadXslForMapping(String mappingId) {
        template.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = template.exchange(RestEndpoints.resolve(
                RestEndpoints.XSL_MAPPINGID,mappingId), HttpMethod.GET, entity, byte[].class, "1");
        if(response.getStatusCode().equals(HttpStatus.OK))
        {
            return response.getBody();
        }
        return null;
    }
}
