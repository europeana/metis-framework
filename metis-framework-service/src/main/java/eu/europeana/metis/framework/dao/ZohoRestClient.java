package eu.europeana.metis.framework.dao;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.framework.crm.Field;
import eu.europeana.metis.framework.crm.Row;
import eu.europeana.metis.framework.crm.ZohoFields;
import eu.europeana.metis.framework.crm.ZohoResponse;
import eu.europeana.metis.framework.organization.Organization;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Rest Client for Zoho CRM
 * Created by ymamakis on 2/23/16.
 */
@Component
public class ZohoRestClient {

    private RestTemplate template = new RestTemplate();
    private String baseUrl;
    private String authorizationToken;
    private String scope;
    private final static String GETALLPROVIDERS = "getRecords";
    private final static String GETPROVIDERBYID = "getRecordById";

    /**
     * Constructor for Zoho Rest Client
     * @param baseUrl The URL of Zoho
     * @param authorizationToken The authorization token
     * @param scope The scope
     */
    public ZohoRestClient(String baseUrl, String authorizationToken, String scope) {
        this.authorizationToken = authorizationToken;
        this.baseUrl = baseUrl;
        this.scope = scope;
        List<HttpMessageConverter<?>> converters = template.getMessageConverters();
        converters.add(new MappingJackson2HttpMessageConverter());
        template.setMessageConverters(converters);
    }

    /**
     * Retrieve all the organizations from Zoho
     * @return A list of all the registered organizations in Zoho
     * @throws ParseException
     * @throws IOException
     */
    public List<Organization> getAllOrganizations() throws ParseException, IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        ZohoResponse resp = new ZohoResponse();
        HttpEntity<ZohoResponse> entity = new HttpEntity<>(resp, httpHeaders);
        ResponseEntity<String> ts = template.exchange(baseUrl + GETALLPROVIDERS + "?authtoken=" + authorizationToken + "&scope=" + scope, HttpMethod.GET, entity, String.class);
        ObjectMapper om = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        ZohoResponse ret = om.readValue(ts.getBody(), ZohoResponse.class);
        return fromListResponse(ret);
    }

    /**
     * Get an organization by a specific id from the CRM
     * @param id The id of the organization to search for
     * @return The Organization representation for that Id
     * @throws ParseException
     * @throws IOException
     */
    public Organization getOrganizationById(String id) throws ParseException, IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        ZohoResponse resp = new ZohoResponse();
        HttpEntity<ZohoResponse> entity = new HttpEntity<>(resp, httpHeaders);
        ResponseEntity<String>  ts = template.exchange(baseUrl + GETPROVIDERBYID + "?authtoken=" + authorizationToken + "&scope=" + scope + "&id=" + id, HttpMethod.GET, entity, String.class);
        ObjectMapper om = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        ZohoResponse ret = om.readValue(ts.getBody(), ZohoResponse.class);
        return fromOneResponse(ret);
    }

    private List<Organization> fromListResponse(ZohoResponse resp) throws ParseException {
        List<Row> rows = resp.getResponse().getResult().getModule().getRows();
        List<Organization> orgs = new ArrayList<>();
        for (Row row : rows) {
            orgs.add(readResponsetoOrganization(row));
        }
        return orgs;
    }

    private Organization fromOneResponse(ZohoResponse resp) throws ParseException {
        Row row = resp.getResponse().getResult().getModule().getRows().get(0);

        return readResponsetoOrganization(row);
    }

    private Organization readResponsetoOrganization(Row row) throws ParseException {
        Organization org = new Organization();
        for (Field field : row.getFields()) {
            if (StringUtils.equals(field.getVal(), ZohoFields.ID)) {
                org.setOrganizationId(field.getContent());
            }
            if (StringUtils.equals(field.getVal(), ZohoFields.ACRONYM)) {
                org.setAcronym(field.getContent());
            }
            if (StringUtils.equals(field.getVal(), ZohoFields.NAME)) {
                org.setName(field.getContent());
            }
            if (StringUtils.equals(field.getVal(), ZohoFields.CREATEDTIME)) {
                DateFormat fd = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

                org.setCreated(fd.parse(field.getContent()));

            }
            if (StringUtils.equals(field.getVal(), ZohoFields.MODFIEDTIME)) {
                DateFormat fd = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

                org.setModified(fd.parse(field.getContent()));

            }
            if (StringUtils.equals(field.getVal(), ZohoFields.ROLE)) {
                List<String> roles = Arrays.asList(field.getContent().split(";"));
                org.setRoles(roles);
            }
        }
        return org;
    }
}
