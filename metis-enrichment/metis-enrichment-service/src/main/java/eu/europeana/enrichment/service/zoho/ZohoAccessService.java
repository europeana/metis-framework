package eu.europeana.enrichment.service.zoho;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import eu.europeana.corelib.definitions.edm.entity.Organization;
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
		OrganizationImpl res = new OrganizationImpl();

		ZohoOrganizationAdapter zoa = null;
		try {
			zoa = new ZohoOrganizationAdapter(jsonRecord);
		} catch (IOException e) {
			throw new ZohoAccessException("Cannot find fields label in zoho response." + jsonRecord, e);
		}

		res.setDcIdentifier(createMap(zoa.getLanguage(), zoa.getZohoId()));
		res.setPrefLabel(createMap(zoa.getLanguage(), zoa.getOrganizationName()));
		res.setAltLabel(createMapFromList(zoa.getLanguage(), zoa.getAlternativeOrganizationName()));
		res.setEdmAcronym(createMap(zoa.getLanguage(), zoa.getAcronym()));
		// res.setFoafLogo(zoa.getLogo());
		res.setFoafHomepage(zoa.getWebsite());
		res.setEdmEuropeanaRole(createMap(zoa.getLanguage(), zoa.getRole()));
		res.setEdmOrganizationDomain(createMapOfStrings(zoa.getLanguage(), zoa.getDomain()));
		res.setEdmOrganizationSector(createMapOfStrings(zoa.getLanguage(), zoa.getSector()));
		res.setEdmOrganizationScope(createMapOfStrings(zoa.getLanguage(), zoa.getScope()));
		res.setEdmGeorgraphicLevel(createMapOfStrings(zoa.getLanguage(), zoa.getGeographicLevel()));
		res.setEdmCountry(zoa.getOrganizationCountry());

		return res;
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
	private Map<String, List<String>> createMap(String key, String value) {
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		List<String> valueList = new ArrayList<String>();
		valueList.add(value);
		resMap.put(key, valueList);
		return resMap;
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
	private Map<String, String> createMapOfStrings(String key, String value) {
		Map<String, String> resMap = new HashMap<String, String>();
		resMap.put(key, value);
		return resMap;
	}

	/**
	 * Create OrganizationImpl map from list of values
	 * 
	 * @param key
	 *            The field name
	 * @param value
	 *            The list of values
	 * @return map of strings and lists
	 */
	private Map<String, List<String>> createMapFromList(String key, List<String> value) {
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		resMap.put(key, value);
		return resMap;
	}

}
