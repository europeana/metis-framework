package eu.europeana.enrichment.service.zoho;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import eu.europeana.corelib.definitions.edm.entity.Address;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.AddressImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.service.exception.ZohoAccessException;
import eu.europeana.enrichment.service.zoho.model.ZohoOrganizationAdapter;
import eu.europeana.metis.authentication.dao.PsqlMetisUserDao;
import eu.europeana.metis.authentication.dao.ZohoAccessClientDao;
import eu.europeana.metis.authentication.dao.ZohoApiFields;
import eu.europeana.metis.exception.GenericMetisException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@Service
public class ZohoAccessService {

	private static final String URL_ORGANIZATION_PREFFIX = "http://data.europeana.eu/organization/";
	private static final String UNDEFINED_LANGUAGE_KEY = "def";
	private final ZohoAccessClientDao zohoAccessClientDao;
	// private SimpleDateFormat dateFormatter = new
	// SimpleDateFormat(ZohoApiFields.ZOHO_TIME_FORMAT);

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
	protected Organization getOrganization(String organizationId)
			throws ZohoAccessException {

		JsonNode jsonRecordsResponse;
		try {
			jsonRecordsResponse = zohoAccessClientDao
					.getOrganizationById(organizationId);
		} catch (GenericMetisException e) {
			throw new ZohoAccessException(
					"Cannot get organization by id: " + organizationId, e);
		}
		JsonNode accountsNode = findRecordsByType(jsonRecordsResponse,
				ZohoApiFields.ACCOUNTS_MODULE_STRING);
		JsonNode jsonRecord = accountsNode
				.findValue(ZohoApiFields.FIELDS_LABEL);

		return getOrganizationFromJsonNode(jsonRecord);
	}

	/**
	 * This method retrieves a list of Zoho records in {@link JsonNode} format.
	 * 
	 * @param jsonLeadsResponse
	 * @param type
	 *            the type of the objects to be retrieved
	 * @return {@link JsonNode} representation of the organization
	 */
	JsonNode findRecordsByType(JsonNode jsonLeadsResponse, String type) {
		if (jsonLeadsResponse.get(ZohoApiFields.RESPONSE_STRING)
				.get(ZohoApiFields.RESULT_STRING) == null) {
			return null;
		}
		return jsonLeadsResponse.get(ZohoApiFields.RESPONSE_STRING)
				.get(ZohoApiFields.RESULT_STRING).get(type)
				.get(ZohoApiFields.ROW_STRING);
	}

	Organization getOrganizationFromJsonNode(JsonNode jsonRecord)
			throws ZohoAccessException {
		OrganizationImpl org = new OrganizationImpl();

		ZohoOrganizationAdapter zoa = new ZohoOrganizationAdapter(jsonRecord);

		org.setAbout(URL_ORGANIZATION_PREFFIX + zoa.getZohoId());
		org.setDcIdentifier(
				createMapOfStringList(UNDEFINED_LANGUAGE_KEY, zoa.getZohoId()));
		String isoLanguage = toIsoLanguage(
				zoa.getLanguageForOrganizationName());
		org.setPrefLabel(
				createMapOfStringList(isoLanguage, zoa.getOrganizationName()));
		org.setAltLabel(
				createLanguageMapOfStringList(zoa.getAlternativeLanguage(),
						zoa.getAlternativeOrganizationName()));
		org.setEdmAcronym(createLanguageMapOfStringList(zoa.getLangAcronym(),
				zoa.getAcronym()));
		org.setFoafLogo(zoa.getLogo());
		org.setFoafHomepage(zoa.getWebsite());
		String[] role = zoa.getRole().split(";");
		org.setEdmEuropeanaRole(createLanguageMapOfStringList(
				Locale.ENGLISH.getLanguage(), Arrays.asList(role)));
		org.setEdmOrganizationDomain(
				createMap(Locale.ENGLISH.getLanguage(), zoa.getDomain()));
		org.setEdmOrganizationSector(
				createMap(Locale.ENGLISH.getLanguage(), zoa.getSector()));
		org.setEdmOrganizationScope(
				createMap(Locale.ENGLISH.getLanguage(), zoa.getScope()));
		org.setEdmGeorgraphicLevel(createMap(Locale.ENGLISH.getLanguage(),
				zoa.getGeographicLevel()));
		String organizationCountry = toEdmCountry(zoa.getOrganizationCountry());
		org.setEdmCountry(
				createMap(Locale.ENGLISH.getLanguage(), organizationCountry));
		
		if(zoa.getSameAs() != null && !zoa.getSameAs().isEmpty())
		    org.setOwlSameAs(zoa.getSameAs().toArray(new String[] {}));
		
		// address
		Address address = new AddressImpl();

		address.setAbout(org.getAbout() + "#address");
		address.setVcardStreetAddress(zoa.getStreet());
		address.setVcardLocality(zoa.getCity());
		address.setVcardCountryName(zoa.getCountry());
		address.setVcardPostalCode(zoa.getZipCode());
		address.setVcardPostOfficeBox(zoa.getPostBox());

		org.setAddress(address);

		//technical fields
		org.setModified(zoa.getModified());
		org.setCreated(zoa.getCreated());

		return org;
	}

	String toEdmCountry(String organizationCountry) {
		if (StringUtils.isBlank(organizationCountry))
			return null;
		int separatorPos = organizationCountry.indexOf("(");
		if (separatorPos < 0)
			return organizationCountry;
		else
			return organizationCountry.substring(0, separatorPos);
	}

	String toIsoLanguage(String language) {
		if (StringUtils.isBlank(language))
			return UNDEFINED_LANGUAGE_KEY;

		// TODO we might want to validate the language using
		// Locale.getISOLanguages()
		return language.substring(0, 2).toLowerCase();
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
    public List<Organization> getOrganizations(int start, int rows,
            Date lastModified) throws ZohoAccessException {
      return getOrganizations(start, rows, lastModified, null);
    }
    
	/**
	 * 
	 * @param start
	 *            first index starts with 1
	 * @param rows
	 *            the number of entries to be returned
     * @param lastModified
     * @param searchCriteria
	 * @return
	 * @throws ZohoAccessException
	 */
	public List<Organization> getOrganizations(int start, int rows,
			Date lastModified, Map<String,String> searchCriteria) throws ZohoAccessException {

		if (start < 1)
			throw new ZohoAccessException(
					"Invalid start index. Index must be >= 1",
					new IllegalArgumentException("start: " + start));

		JsonNode jsonRecordsResponse;
		String lastModifiedTime = null;
		if (lastModified != null)
			lastModifiedTime = getDateFormatter().format(lastModified);

		try {
		    if (searchCriteria != null && !searchCriteria.isEmpty())
    			jsonRecordsResponse = zohoAccessClientDao.searchOrganizations(start,
    					rows, lastModifiedTime, searchCriteria);
		    else
	            jsonRecordsResponse = zohoAccessClientDao.getOrganizations(start,
                    rows, lastModifiedTime);
		} catch (GenericMetisException e) {
			throw new ZohoAccessException("Cannot get organization list from: "
					+ start + " rows :" + rows, e);
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
	protected List<Organization> getOrganizationsListFromJsonNode(
			JsonNode jsonRecordsResponse) throws ZohoAccessException {

		JsonNode accountsNode = findRecordsByType(jsonRecordsResponse,
				ZohoApiFields.ACCOUNTS_MODULE_STRING);
		List<Organization> res = new ArrayList<Organization>();
		if (accountsNode == null)
			return res;

		// one result in the response
		boolean oneResult = accountsNode.get(0) == null;
		if (oneResult) {
			addAccountToOrgList(accountsNode, res);
			return res;
		}

		// a list of results in the response
		for (JsonNode accountNode : accountsNode) {
			addAccountToOrgList(accountNode, res);
		}

		return res;
	}

	private void addAccountToOrgList(JsonNode accountNode,
			List<Organization> res) throws ZohoAccessException {
		JsonNode organizationNode = accountNode.get(ZohoApiFields.FIELDS_LABEL);
		res.add(getOrganizationFromJsonNode(organizationNode));
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
	Map<String, List<String>> createMapOfStringList(String key, String value) {
		List<String> valueList = createList(value);
		return createMapOfStringList(key, valueList);
	}

	Map<String, String> createMap(String key, String value) {
		Map<String, String> resMap = new HashMap<String, String>();
		resMap.put(key, value);
		return resMap;
	}

	List<String> createList(String value) {
		return Collections.singletonList(value);
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
	Map<String, List<String>> createMapOfStringList(String key,
			List<String> value) {
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		resMap.put(key, value);
		return resMap;
	}

	Map<String, List<String>> createLanguageMapOfStringList(
			List<String> languages, List<String> values) {
		if (languages == null)
			return null;

		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		for (int i = 0; i < languages.size(); i++) {
			resMap.put(toIsoLanguage(languages.get(i)),
					createList(values.get(i)));
		}
		return resMap;
	}

	Map<String, List<String>> createLanguageMapOfStringList(String language,
			String value) {
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		resMap.put(toIsoLanguage(language), createList(value));
		return resMap;
	}

	Map<String, List<String>> createLanguageMapOfStringList(String language,
			List<String> value) {
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		resMap.put(toIsoLanguage(language), value);
		return resMap;
	}

	public SimpleDateFormat getDateFormatter() {
		return ZohoApiFields.getZohoTimeFormatter();
	}
}
