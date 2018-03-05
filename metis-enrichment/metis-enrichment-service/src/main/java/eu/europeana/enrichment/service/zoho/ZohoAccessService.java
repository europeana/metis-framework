package eu.europeana.enrichment.service.zoho;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.Address;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.service.exception.ZohoAccessException;
import eu.europeana.enrichment.service.zoho.model.ZohoOrganizationAdapter;
import eu.europeana.metis.authentication.dao.PsqlMetisUserDao;
import eu.europeana.metis.authentication.dao.ZohoAccessClientDao;
import eu.europeana.metis.authentication.dao.ZohoFields;
import eu.europeana.metis.exception.GenericMetisException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@Service
public class ZohoAccessService {

	private static final String URL_ORGANIZATION_BASE = "http://data.europeana.eu/organization/base/";
	private final ZohoAccessClientDao zohoAccessClientDao;

	/**
	 * Constructor of class with required parameters
	 *
	 * @param zohoAccessClientDao
	 *            {@link ZohoAccessClientDao}
	 * @param psqlMetisUserDao
	 *            {@link PsqlMetisUserDao}
	 */
	@Autowired
	public ZohoAccessService(ZohoAccessClientDao zohoAccessClientDao) {
		this.zohoAccessClientDao = zohoAccessClientDao;
	}

	/**
	 * This method retrieves OrganizationImpl object from Zoho organization
	 * record.
	 * 
	 * @param jsonRecordResponse
	 * @return representation of the Zoho organization record in OrganizatinImpl
	 *         format
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	protected Organization getOrganization(String organizationId) throws ZohoAccessException {

		JsonNode jsonRecordsResponse;
		try {
			jsonRecordsResponse = zohoAccessClientDao.getOrganizationById(organizationId);
		} catch (GenericMetisException e) {
			throw new ZohoAccessException("Cannot get organization by id: " + organizationId, e);
		}
		JsonNode accountsNode = findRecordsByType(jsonRecordsResponse, ZohoFields.ACCOUNTS_MODULE_STRING);
		JsonNode jsonRecord = accountsNode.findValue(ZohoFields.FIELDS_LABEL);

		return getOrganizationFromJsonNode(jsonRecord);
	}

	/**
	 * This method retrieves a list of organizations in {@link JsonNode} format.
	 * 
	 * @param jsonLeadsResponse
	 * @param type
	 *            the type of the objects to be retrieved
	 * @return {@link JsonNode} representation of the organization
	 */
	JsonNode findRecordsByType(JsonNode jsonLeadsResponse, String type) {
		if (jsonLeadsResponse.get(ZohoFields.RESPONSE_STRING).get(ZohoFields.RESULT_STRING) == null) {
			return null;
		}
		return jsonLeadsResponse.get(ZohoFields.RESPONSE_STRING).get(ZohoFields.RESULT_STRING).get(type)
				.get(ZohoFields.ROW_STRING);
	}

	Organization getOrganizationFromJsonNode(JsonNode jsonRecord) throws ZohoAccessException {
		OrganizationImpl org = new OrganizationImpl();

		ZohoOrganizationAdapter zoa = null;
		try {
			zoa = new ZohoOrganizationAdapter(jsonRecord);
		} catch (IOException e) {
			throw new ZohoAccessException("Cannot find fields label in zoho response." + jsonRecord, e);
		}

		org.setAbout(URL_ORGANIZATION_BASE + zoa.getZohoId());
		org.setDcIdentifier(createMapOfStringList(zoa.getLanguage(), zoa.getZohoId()));
		org.setPrefLabel(createMapOfStringList(zoa.getLanguage(), zoa.getOrganizationName()));
		org.setAltLabel(createMapOfStringList(zoa.getLanguage(), zoa.getAlternativeOrganizationName()));
		org.setEdmAcronym(createMapOfStringList(zoa.getLanguage(), zoa.getAcronym()));
		org.setFoafLogo(zoa.getLogo());
		org.setFoafHomepage(zoa.getWebsite());
		org.setEdmEuropeanaRole(createList(zoa.getRole()));
		org.setEdmOrganizationDomain(zoa.getDomain());
		org.setEdmOrganizationSector(zoa.getSector());
		org.setEdmOrganizationScope(zoa.getScope());
		org.setEdmGeorgraphicLevel(zoa.getGeographicLevel());
		org.setEdmCountry(zoa.getOrganizationCountry());
		org.setModified(zoa.getModified());
		org.setCreated(zoa.getCreated());
		
		//address
		Address address = new Address();
	
		address.setAbout(org.getAbout()+"#address");
		address.setVcardStreetAddress(zoa.getStreet());
		address.setVcardLocality(zoa.getCity());
		address.setVcardCountryName(zoa.getCountry());
		address.setVcardPostalCode(zoa.getZipCode());
		address.setVcardPostOfficeBox(zoa.getPostBox());
		
		org.setAddress(address);
		
		return org;
	}

	/**
	 * 
	 * @param start
	 *            first index starts with 1
	 * @param rows
	 *            the number of entries to be returned
	 * @return
	 * @throws ZohoAccessException
	 */
	public List<Organization> getOrganizations(int start, int rows) throws ZohoAccessException {

		if (start < 1)
			throw new ZohoAccessException("Invalid start index. Index must be >= 1",
					new IllegalArgumentException("start: " + start));

		JsonNode jsonRecordsResponse;
		try {
			jsonRecordsResponse = zohoAccessClientDao.getOrganizations(start, rows);
		} catch (GenericMetisException e) {
			throw new ZohoAccessException("Cannot get organization list from: " + start + " rows :" + rows, e);
		}
		return getOrganizationsListFromJsonNode(jsonRecordsResponse);

	}

	/**
	 * This method retrieves map of records.
	 * 
	 * @param jsonRecordsResponse
	 * @return map representation of the records
	 * @throws ZohoAccessException
	 */
	protected List<Organization> getOrganizationsListFromJsonNode(JsonNode jsonRecordsResponse)
			throws ZohoAccessException {

		JsonNode accountsNode = findRecordsByType(jsonRecordsResponse, ZohoFields.ACCOUNTS_MODULE_STRING);
		if (accountsNode == null)
			return null;

		List<Organization> res = new ArrayList<Organization>();
		JsonNode organizationNode;

		for (JsonNode accountNode : accountsNode) {
			organizationNode = accountNode.get(ZohoFields.FIELDS_LABEL);
			res.add(getOrganizationFromJsonNode(organizationNode));
		}

		return res;
	}

	/**
	 * Create OrganizationImpl map from value
	 * 
	 * @param key
	 *            The field name
	 * @param value
	 *            The value
	 * @return map of strings and lists
	 */
	private Map<String, List<String>> createMapOfStringList(String key, String value) {
		List<String> valueList = createList(value);
		return createMapOfStringList(key, valueList);
	}

	private List<String> createList(String value) {
		return Arrays.asList(new String[]{value});
	}

	/**
	 * Create OrganizationImpl map of strings from value
	 * 
	 * @param key
	 *            The field name
	 * @param value
	 *            The value
	 * @return map of strings
	 */
//	private Map<String, String> createMapOfStrings(String key, String value) {
//		Map<String, String> resMap = new HashMap<String, String>();
//		resMap.put(key, value);
//		return resMap;
//	}

	/**
	 * Create OrganizationImpl map from list of values
	 * 
	 * @param key
	 *            The field name
	 * @param value
	 *            The list of values
	 * @return map of strings and lists
	 */
	private Map<String, List<String>> createMapOfStringList(String key, List<String> value) {
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		resMap.put(key, value);
		return resMap;
	}

}
