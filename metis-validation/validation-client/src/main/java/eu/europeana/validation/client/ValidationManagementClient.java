package eu.europeana.validation.client;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.validation.model.Schema;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by ymamakis on 8/1/16.
 */
public class ValidationManagementClient {
    private RestTemplate template = new RestTemplate();
    private String validationEndpoint;

    public ValidationManagementClient(){
        Properties props = new Properties();
        try {
            props.load(this.getClass().getClassLoader().getResourceAsStream("validation.properties"));
            validationEndpoint = props.getProperty("validation.endpoint");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Download a byte array of the schema
     * @param name The name of the schema to download
     * @param version The version of the schema (can be null)
     * @return A tgz with the schema
     */
    public byte[] downloadSchemaFileBytes(String name, String version){
        template.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = template.exchange(validationEndpoint+RestEndpoints.resolve(
                RestEndpoints.SCHEMAS_DOWNLOAD_BY_NAME,name)+"?version="+version, HttpMethod.GET, entity, byte[].class, "1");
        if(response.getStatusCode().equals(HttpStatus.OK))
        {
            return response.getBody();
        }
        return null;
    }

    /**
     * Create a new schema
     * @param name The name of the schema
     * @param schemaPath The root file of the schema (in case its sprea over to multiple files)
     * @param schematronPath The schematron rules of the schema (must be separate)
     * @param version The version of the schema (can be null)
     * @param file A tgx containing the schema
     * @return The uri the schema is accesible from
     */
    public String createSchemaByName(String name, String schemaPath, String schematronPath, String version, File file){
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("schemaPath",schemaPath);
        params.add("schematronPath",schematronPath);
        if(StringUtils.isNotEmpty(version)){
            params.add("version",version);
        }
        FileSystemResource resource = new FileSystemResource(file);
        params.add("file",resource);

        return template.postForObject(validationEndpoint+RestEndpoints.resolve(RestEndpoints.SCHEMAS_MANAGE_BY_NAME,
                name),params,String.class);
    }
    /**
     * Update a schema for a given version
     * @param name The name of the schema
     * @param schemaPath The root file of the schema (in case its sprea over to multiple files)
     * @param schematronPath The schematron rules of the schema (must be separate)
     * @param version The version of the schema (can be null)
     * @param file A tgx containing the schema
     */
    public void updateSchemaByName(String name, String schemaPath, String schematronPath, String version, File file){
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("schemaPath",schemaPath);
        params.add("schematronPath",schematronPath);
        if(StringUtils.isNotEmpty(version)){
            params.add("version",version);
        }
        params.add("file",file);
        template.put(validationEndpoint+RestEndpoints.resolve(RestEndpoints.SCHEMAS_MANAGE_BY_NAME,
                name),params);
    }

    /**
     * Delete a schema
     * @param name The name of the schema
     * @param version The version of the schema (can be null)
     */
    public void deleteSchemaByName(String name,String version){
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        if(StringUtils.isNotEmpty(version)){
            params.add("version",version);
        }
        template.delete(validationEndpoint+RestEndpoints.resolve(RestEndpoints.SCHEMAS_MANAGE_BY_NAME,
                name),params);
    }

    /**
     * Get a schema by name (and optionally version)
     * @param name The name of the schema to look for
     * @param version The version of the schema
     */
    public Schema getSchemaByName(String name,String version){
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        if(StringUtils.isNotEmpty(version)){
            params.add("version",version);
        }
        return template.getForObject(validationEndpoint+RestEndpoints.resolve(RestEndpoints.SCHEMAS_MANAGE_BY_NAME,
                name),Schema.class,params);
    }

    public List<Schema> getAll(){
        return template.getForObject(validationEndpoint+RestEndpoints.SCHEMAS_ALL,List.class);
    }
}
