package eu.europeana.enrichment.service.zoho;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.europeana.corelib.definitions.edm.entity.Address;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.AddressImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.external.model.zoho.ZohoOrganization;
import eu.europeana.enrichment.service.EntityConverterUtils;
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

  EntityConverterUtils entityConverterUtils = new EntityConverterUtils();
	  
  public EntityConverterUtils getEntityConverterUtils() {
    return entityConverterUtils;
  }
	
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
	public ZohoOrganization getOrganization(String organizationId)
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

		return new ZohoOrganizationAdapter(jsonRecord);
	}

    /**
     * This method retrieves OrganizationImpl object for Zoho organization
     * record from given file.
     * 
     * @param content file
     * @return representation of the Zoho organization record in OrganizatinImpl
     *         format
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public ZohoOrganization getOrganizationFromFile(File contentFile)
            throws ZohoAccessException {

        JsonNode jsonRecordsResponse;
        try {
            jsonRecordsResponse = zohoAccessClientDao
                    .getOrganizationFromFile(contentFile);
        } catch (IOException e) {
          throw new ZohoAccessException(
                  "Cannot get organization from file. ", e);
        } catch (GenericMetisException e) {
          throw new ZohoAccessException(
                  "Cannot extract organization from file. ", e);
        }
        JsonNode accountsNode = findRecordsByType(jsonRecordsResponse,
                ZohoApiFields.ACCOUNTS_MODULE_STRING);
        JsonNode jsonRecord = accountsNode
                .findValue(ZohoApiFields.FIELDS_LABEL);

        return new ZohoOrganizationAdapter(jsonRecord);
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

	public Organization toEdmOrganization(ZohoOrganization zoa)
			throws ZohoAccessException {

		OrganizationImpl org = new OrganizationImpl();

		org.setAbout(URL_ORGANIZATION_PREFFIX + zoa.getZohoId());
		org.setDcIdentifier(
		    getEntityConverterUtils().createMapOfStringList(UNDEFINED_LANGUAGE_KEY, zoa.getZohoId()));
		String isoLanguage = toIsoLanguage(
				zoa.getLanguageForOrganizationName());
		org.setPrefLabel(
		    getEntityConverterUtils().createMapOfStringList(isoLanguage, zoa.getOrganizationName()));
		org.setAltLabel(
		    getEntityConverterUtils().createLanguageMapOfStringList(zoa.getAlternativeLanguage(),
						zoa.getAlternativeOrganizationName()));
		org.setEdmAcronym(
		    getEntityConverterUtils().createLanguageMapOfStringList(zoa.getLangAcronym(),
				zoa.getAcronym()));
		org.setFoafLogo(zoa.getLogo());
		org.setFoafHomepage(zoa.getWebsite());

		if (zoa.getRole() != null) {
			String[] role = zoa.getRole().split(";");
			org.setEdmEuropeanaRole(
			    getEntityConverterUtils().createLanguageMapOfStringList(
					Locale.ENGLISH.getLanguage(), Arrays.asList(role)));
		}
		org.setEdmOrganizationDomain(
		    getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(), zoa.getDomain()));
		org.setEdmOrganizationSector(
		    getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(), zoa.getSector()));
		org.setEdmOrganizationScope(
		    getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(), zoa.getScope()));
		org.setEdmGeorgraphicLevel(
		    getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(),
				zoa.getGeographicLevel()));
		String organizationCountry = toEdmCountry(zoa.getOrganizationCountry());
		org.setEdmCountry(
		    getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(), organizationCountry));

		if (zoa.getSameAs() != null && !zoa.getSameAs().isEmpty())
			org.setOwlSameAs(zoa.getSameAs().toArray(new String[]{}));

		// address
		Address address = new AddressImpl();
		address.setAbout(org.getAbout() + "#address");
		address.setVcardStreetAddress(zoa.getStreet());
		address.setVcardLocality(zoa.getCity());
		address.setVcardCountryName(zoa.getCountry());
		address.setVcardPostalCode(zoa.getZipCode());
		address.setVcardPostOfficeBox(zoa.getPostBox());
		org.setAddress(address);

		return org;
	}

	String toEdmCountry(String organizationCountry) {
		if (StringUtils.isBlank(organizationCountry))
			return null;

		String isoCode = null;
		int commaSeparatorPos = organizationCountry.indexOf(",");
		//TODO: remove the support for FR(France), when Zoho data is updated and consistent. 
		int bracketSeparatorPos = organizationCountry.indexOf("(");

		if (commaSeparatorPos > 0){
			// example: FR(France)
			isoCode = organizationCountry.substring(commaSeparatorPos + 1).trim();
		} else if (bracketSeparatorPos > 0){
			// example: France, FR
			isoCode = organizationCountry.substring(0, bracketSeparatorPos).trim();
		}
		
		return isoCode;
	}

	String toIsoLanguage(String language) {
		if (StringUtils.isBlank(language))
			return UNDEFINED_LANGUAGE_KEY;

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
	public List<ZohoOrganization> getOrganizations(int start, int rows,
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
	public List<ZohoOrganization> getOrganizations(int start, int rows,
			Date lastModified, Map<String, String> searchCriteria)
			throws ZohoAccessException {

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
				jsonRecordsResponse = zohoAccessClientDao.searchOrganizations(
						start, rows, lastModifiedTime, searchCriteria);
			else
				jsonRecordsResponse = zohoAccessClientDao
						.getOrganizations(start, rows, lastModifiedTime);
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
	protected List<ZohoOrganization> getOrganizationsListFromJsonNode(
			JsonNode jsonRecordsResponse) throws ZohoAccessException {

		JsonNode accountsNode = findRecordsByType(jsonRecordsResponse,
				ZohoApiFields.ACCOUNTS_MODULE_STRING);
		List<ZohoOrganization> res = new ArrayList<ZohoOrganization>();
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
			List<ZohoOrganization> res) throws ZohoAccessException {
		JsonNode organizationNode = accountNode.get(ZohoApiFields.FIELDS_LABEL);
		ZohoOrganization zoa = new ZohoOrganizationAdapter(organizationNode);
		res.add(zoa);
	}

	public FastDateFormat getDateFormatter() {
		return ZohoApiFields.getZohoTimeFormatter();
	}
}
