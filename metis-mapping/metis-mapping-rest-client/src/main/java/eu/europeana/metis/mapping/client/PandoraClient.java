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
            restEndpoint = props.getProperty("mapping.rest.url");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Persist a Mapping
     * @param mapping The mapping to persist
     * @return The id of the mapping
     */
    public String createMapping(Mapping mapping) {
        return template.postForObject(restEndpoint + RestEndpoints.MAPPING, mapping, String.class);
    }

    /**
     * Update a mapping
     * @param mapping The mapping to update
     */
    public void updateMapping(Mapping mapping) {
        template.put(restEndpoint + RestEndpoints.MAPPING, mapping);
    }

    /**
     * Delete a mapping
     * @param mapping The mapping to delete
     */
    public void deleteMapping(Mapping mapping) {
        template.delete(restEndpoint + RestEndpoints.MAPPING, mapping);
    }

    /**
     * Retrieve a mapping using its id
     * @param id The mapping id
     * @return The mapping
     */
    public Mapping getMappingById(String id) {
        return template.getForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.MAPPING_BYID, id), Mapping.class);
    }

    /**
     * Retrieve a mapping using its name
     * @param name The name of the mapping
     * @return The mapping
     */
    public Mapping getMappingByName(String name) {
        return template.getForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.MAPPING_DATASETNAME, name), Mapping.class);
    }

    /**
     * Get the mappings for an organization id
     * @param orgId The organization id
     * @return The list of Mappings registered to an organization
     */
    public List<Mapping> getMappingsByOrgId(String orgId) {
        return template.getForObject(restEndpoint + RestEndpoints.resolve(
                RestEndpoints.MAPPINGS_BYORGANIZATIONID, orgId), List.class);
    }

    /**
     * Retrieve the mapping names for an organization
     * @param orgId The organization id
     * @return The mapping names registered to the organization
     */
    public List<String> getMappingNamesByOrgId(String orgId) {
        return template.getForObject(restEndpoint + RestEndpoints.resolve(
                RestEndpoints.MAPPINGS_NAMES_BYORGANIZATIONID, orgId), List.class);
    }

    /**
     * Retrieve the list of mapping templates (empty mappings)
     * @return The list of mapping templates
     */
    public List<Mapping> getMappingTemplates() {
        return template.getForObject(restEndpoint + RestEndpoints.MAPPING_TEMPLATES, List.class);
    }

    /**
     * Delete the statistics for a mapping. Should be called everytime a dataset is updated or a mapping is modified
     * @param name The name of the mapping
     */
    public void deleteStatisticsForMapping(String name) {
        template.delete(restEndpoint + RestEndpoints.resolve(RestEndpoints.MAPPING_STATISTICS_BYNAME, name));
    }

    /**
     * Append schematron rules for mapping
     * @param mappingId The id of the mapping
     * @param rules The schematron rules
     */
    public void setSchematroRulesForMapping(String mappingId , Set<String> rules) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("rules", rules);
        params.add("mappingId",mappingId);
        template.put(restEndpoint + RestEndpoints.MAPPING_SCHEMATRON, params);
    }

    /**
     * Append namespaces to the mappings. These cannot be automatically extracted by the XSD
     * as they are only used as a naming convention, and the XSOM library does not provide a
     * meaningful way to extract them
     *
     * @param mappingId The id of the mapping (should exist)
     * @param namespaces The namespaces to append
     */
    public void setNamespacesForMapping(String mappingId, Map<String, String> namespaces) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("namespaces", namespaces);
        params.add("mappingId",mappingId);
        template.put(restEndpoint + RestEndpoints.MAPPING_NAMESPACES, params);
    }

    /**
     * Calculate the statistics of a dataset
     * @param datasetId The name of the dataset
     * @param file The tgz file with the records of the dataset
     * @return The statistics per field
     */
    public DatasetStatistics calculateStatisticsForDataset(String datasetId, File file) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("file", file);
        return template.postForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.STATISTICS_CALCULATE, datasetId),
                params, DatasetStatistics.class);
    }

    /**
     * Append the dataset statistics to a mapping
     * @param datasetId The dataset id
     * @param mapping The mapping to update
     */
    public void appendStatisticsToMapping(String datasetId, Mapping mapping) {
        template.put(restEndpoint + RestEndpoints.resolve(RestEndpoints.STATISTICS_APPEND, datasetId),
                mapping);
    }

    /**
     * Validate an attribute from a mapping id against its flags
     * @param mappingId The meppaing id (used to retrieve the flags for this mapping)
     * @param attr The attribute to validate
     * @return The List of Flags that were identified to exist in the mapping of the specific attribute
     */
    public List<Flag> validateAttribute(String mappingId, Attribute attr) {
        return template.postForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.VALIDATE_ATTRIBUTE, mappingId),
                attr, List.class);
    }

    /**
     * Validate an element from a mapping id against its flags
     * @param mappingId The meppaing id (used to retrieve the flags for this mapping)
     * @param elem The element to validate
     * @return The List of Flags that were identified to exist in the mapping of the specific element
     */
    public List<Flag> validateElement(String mappingId, Element elem) {
        return template.postForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.VALIDATE_ELEMENT, mappingId),
                elem, List.class);
    }

    /**
     * Manually create a flag for an attribute
     * @param mappingId The id of the mapping to check against
     * @param type The type of flag to be created
     * @param value The value that will trigger the flag
     * @param message The human readable message for this value
     * @param attr The attribute that needs to be checked to trigger this flag
     * @return The newly generated flag
     */
    public Flag createAttributeFlag(String mappingId, FlagType type, String value, String message, Attribute attr) {

        AttributeFlagDTO dto = new AttributeFlagDTO();
        dto.setAttr(attr);
        dto.setMessage(message);
        return template.postForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.VALIDATE_CREATE_ATTTRIBUTE_FLAG, mappingId, value, type.name()),
                dto, Flag.class);
    }

    /**
     * Manually create a flag for an element
     * @param mappingId The id of the mapping to check against
     * @param type The type of flag to be created
     * @param value The value that will trigger the flag
     * @param message The human readable message for this value
     * @param elem The element that needs to be checked to trigger this flag
     * @return The newly generated flag
     */
    public Flag createElementFlag(String mappingId, FlagType type, String value, String message, Element elem) {
        ElementFlagDTO dto = new ElementFlagDTO();
        dto.setMessage(message);
        dto.setElem(elem);
        return template.postForObject(restEndpoint + RestEndpoints.resolve(RestEndpoints.VALIDATE_CREATE_ELEMENT_FLAG, mappingId, value, type.name()),
                dto, Flag.class);
    }

    /**
     * Delete an attribute flag
     * @param mappingId The mapping id
     * @param value The value of the flag
     * @param attr The attribute for which to delete it
     */
    public void deleteAttributeFlag(String mappingId, String value, Attribute attr) {
        template.delete(restEndpoint + RestEndpoints.resolve(RestEndpoints.VALIDATE_DELETE_ATTRIBUTE_FLAG, mappingId,value),attr);
    }

    /**
     * Delete an element flag
     * @param mappingId The mapping id
     * @param value The value of the flag
     * @param element The element for which to delete it
     */
    public void deleteElementFlag(String mappingId, String value, Element element) {
        template.delete(restEndpoint + RestEndpoints.resolve(RestEndpoints.VALIDATE_DELETE_ATTRIBUTE_FLAG, mappingId,value),element);
    }

    /**
     * Validate a mapping
     * @param mapping The mapping to validate
     * @return The mapping populated with all the flags for the elements and attributes that were identified
     */
    public Mapping validateMapping(Mapping mapping) {
        return template.postForObject(restEndpoint + RestEndpoints.VALIDATE_MAPPING,
                mapping, Mapping.class);
    }

    /**
     * Generate a mapping template from a tgz containing an XSD
     * @param rootFile The file containing the main xsd
     * @param mappingName The name of the mapping to generate (a "template_" will be automatically added)
     * @param rootXPath The XPath for the data that will be mapped using this template
     * @param namespaces The namespaces of the mapping
     * @param file The tgz file
     * @param schema The schema for which this mapping is created for
     * @return The id of the mapping
     * @throws IOException
     */
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
    /**
     * Generate a mapping template from a remote tgz containing an XSD
     * @param rootFile The file containing the main xsd
     * @param mappingName The name of the mapping to generate (a "template_" will be automatically added)
     * @param rootXPath The XPath for the data that will be mapped using this template
     * @param namespaces The namespaces of the mapping
     * @param url The url of the tgz file
     * @param schema The schema for which this mapping is created for
     * @return The id of the mapping
     */
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

    /**
     * Convert a mapping to an XSL
     * @param mapping The mapping to convert to XSL
     * @return The XSL
     */
    public String createXslFromMapping(Mapping mapping) {
        return template.postForObject(restEndpoint + RestEndpoints.XSL_GENERATE,
                mapping, String.class);
    }

    /**
     * Download the XSL for a given mapping
     * @param mappingId The id of the mapping
     * @return The XSL
     */
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
