package eu.europeana.metis.authentication.dao;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.metis.common.model.OrganizationRole;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
public class ZohoAccessClientDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZohoAccessClientDao.class);

	private final String zohoBaseUrl;
	private final String zohoAuthenticationToken;

	/**
	 * Constructor with required fields that will be used to access the Zoho
	 * service.
	 *
	 * @param zohoBaseUrl
	 *            the remote url endpoint
	 * @param zohoAuthenticationToken
	 *            the remote authentication token required to access its REST
	 *            API
	 */
	public ZohoAccessClientDao(String zohoBaseUrl, String zohoAuthenticationToken) {
		this.zohoBaseUrl = zohoBaseUrl;
		this.zohoAuthenticationToken = zohoAuthenticationToken;
	}

	/**
	 * Retrieves a {@link JsonNode} containing user details from the remote CRM,
	 * using an email
	 *
	 * @param email
	 *            the email to search for the user
	 * @return {@link JsonNode}
	 * @throws GenericMetisException
	 *             which can be one of:
	 *             <ul>
	 *             <li>{@link BadContentException} if any other problem occurred
	 *             while constructing the user, if the response cannot be
	 *             converted to {@link JsonNode}</li>
	 *             </ul>
	 */
	public JsonNode getUserByEmail(String email) throws GenericMetisException {
		String contactsSearchUrl = String.format("%s/%s/%s/%s", zohoBaseUrl, ZohoFields.JSON_STRING,
				ZohoFields.CONTACTS_MODULE_STRING, ZohoFields.SEARCH_RECORDS_STRING);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
				.queryParam(ZohoFields.AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
				.queryParam(ZohoFields.SCOPE_STRING, ZohoFields.CRMAPI_STRING)
				.queryParam(ZohoFields.CRITERIA_STRING, String.format("(%s:%s)", ZohoFields.EMAIL_FIELD, email));

		RestTemplate restTemplate = new RestTemplate();
		String contactResponse = restTemplate.getForObject(builder.build().encode().toUri(), String.class);
		LOGGER.info(contactResponse);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonContactResponse = null;
		try {
			jsonContactResponse = mapper.readTree(contactResponse);
		} catch (IOException e) {
			throw new BadContentException(String.format("Cannot retrieve user with email %s, from Zoho", email), e);
		}
		if (jsonContactResponse.get(ZohoFields.RESPONSE_STRING).get(ZohoFields.RESULT_STRING) == null) {
			return null;
		}
		return jsonContactResponse.get(ZohoFields.RESPONSE_STRING).get(ZohoFields.RESULT_STRING)
				.get(ZohoFields.CONTACTS_MODULE_STRING).get(ZohoFields.ROW_STRING).get(ZohoFields.FIELDS_LABEL);
	}

	/**
	 * Using an organizationName find its corresponding organizationId.
	 * <p>
	 * It will try to fetch the organization from the external CRM. The external
	 * CRM does NOT check for an exact match, so it is possible that instead of
	 * a singe organization it will return a list of organization in json
	 * format. The exact match will be checked in memory and the correct
	 * organizationId will be returned
	 * </p>
	 *
	 * @param organizationName
	 *            to search for
	 * @return the String representation of the organizationId
	 * @throws GenericMetisException
	 *             which can be one of:
	 *             <ul>
	 *             <li>{@link BadContentException} if any other problem occurred
	 *             while constructing the user, like an organization did not
	 *             have a role defined or the response cannot be converted to
	 *             {@link JsonNode}</li>
	 *             </ul>
	 */
	public String getOrganizationIdByOrganizationName(String organizationName) throws GenericMetisException {
		String contactsSearchUrl = String.format("%s/%s/%s/%s", zohoBaseUrl, ZohoFields.JSON_STRING,
				ZohoFields.ACCOUNTS_MODULE_STRING, ZohoFields.SEARCH_RECORDS_STRING);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
				.queryParam(ZohoFields.AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
				.queryParam(ZohoFields.SCOPE_STRING, ZohoFields.CRMAPI_STRING).queryParam(ZohoFields.CRITERIA_STRING,
						String.format("(%s:%s)", ZohoFields.ORGANIZATION_NAME_FIELD, organizationName));

		RestTemplate restTemplate = new RestTemplate();
		String contactResponse = restTemplate.getForObject(builder.build().encode().toUri(), String.class);
		LOGGER.info(contactResponse);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonContactResponse;
		try {
			jsonContactResponse = mapper.readTree(contactResponse);
		} catch (IOException e) {
			throw new BadContentException(String
					.format("Cannot retrieve organization with orgnaization name %s, from Zoho", organizationName), e);
		}
		return checkOrganizationRoleAndGetOrganizationIdFromJsonNode(
				findExactMatchOfOrganization(jsonContactResponse, organizationName));
	}

	private JsonNode findExactMatchOfOrganization(JsonNode jsonOrgizationsResponse, String organizationName) {
		if (jsonOrgizationsResponse.get(ZohoFields.RESPONSE_STRING).get(ZohoFields.RESULT_STRING) == null) {
			return null;
		}
		if (jsonOrgizationsResponse.get(ZohoFields.RESPONSE_STRING).get(ZohoFields.RESULT_STRING)
				.get(ZohoFields.ACCOUNTS_MODULE_STRING).get(ZohoFields.ROW_STRING).isArray()) {
			return findOrganizationFromListOfJsonNodes(jsonOrgizationsResponse, organizationName);
		}
		return jsonOrgizationsResponse.get(ZohoFields.RESPONSE_STRING).get(ZohoFields.RESULT_STRING)
				.get(ZohoFields.ACCOUNTS_MODULE_STRING).get(ZohoFields.ROW_STRING).get(ZohoFields.FIELDS_LABEL);
	}

	private JsonNode findOrganizationFromListOfJsonNodes(JsonNode jsonOrgizationsResponse, String organizationName) {
		Iterator<JsonNode> organizationJsonNodes = jsonOrgizationsResponse.get(ZohoFields.RESPONSE_STRING)
				.get(ZohoFields.RESULT_STRING).get(ZohoFields.ACCOUNTS_MODULE_STRING).get(ZohoFields.ROW_STRING)
				.elements();
		if (organizationJsonNodes == null || !organizationJsonNodes.hasNext()) {
			return null;
		}
		while (organizationJsonNodes.hasNext()) {
			JsonNode nextOrganizationJsonNode = organizationJsonNodes.next().get(ZohoFields.FIELDS_LABEL);
			Iterator<JsonNode> organizationFields = nextOrganizationJsonNode.elements();
			while (organizationFields.hasNext()) {
				JsonNode organizationField = organizationFields.next();
				JsonNode val = organizationField.get(ZohoFields.VALUE_LABEL);
				JsonNode content = organizationField.get(ZohoFields.CONTENT_LABEL);
				if (StringUtils.equals(val.textValue(), ZohoFields.ORGANIZATION_NAME_FIELD)
						&& StringUtils.equals(content.textValue(), organizationName)) {
					return nextOrganizationJsonNode;
				}
			}
		}
		return null;
	}

	private String checkOrganizationRoleAndGetOrganizationIdFromJsonNode(JsonNode jsonNode) throws BadContentException {
		String organizationId = null;
		if (jsonNode != null) {
			Iterator<JsonNode> elements = jsonNode.elements();
			OrganizationRole organizationRole = null;
			while (elements.hasNext()) {
				JsonNode next = elements.next();
				JsonNode val = next.get(ZohoFields.VALUE_LABEL);
				JsonNode content = next.get(ZohoFields.CONTENT_LABEL);
				switch (val.textValue()) {
				case "ACCOUNTID":
					organizationId = content.textValue();
					break;
				case "Organisation Role":
					organizationRole = OrganizationRole.getRoleFromName(content.textValue());
					break;
				default:
					break;
				}
			}
			if (organizationRole == null) {
				throw new BadContentException("Organization Role from Zoho is empty");
			}
		}
		return organizationId;
	}

	/**
	 * Retrieve Zoho organization by ID.
	 * <p>
	 * It will try to fetch the organization from the external Zoho CRM. This
	 * method returns an organization in JSON format.
	 * </p>
	 * 
	 * Example query:
	 * https://crm.zoho.com/crm/private/json/Accounts/getRecords?authtoken=<token>&scope=crmapi&id=123
	 *
	 * @param organizationId
	 *            The Zoho ID of organization
	 * @return the Zoho organizations in JsonNode format
	 * @throws GenericMetisException
	 *             which can be one of:
	 *             <ul>
	 *             <li>{@link BadContentException} if any other problem occurred
	 *             while constructing the user, like an organization did not
	 *             have a role defined or the response cannot be converted to
	 *             {@link JsonNode}</li>
	 *             </ul>
	 * @throws IOException
	 */
	public JsonNode getOrganizationById(String organizationId) throws GenericMetisException {

		String contactsSearchUrl = String.format("%s/%s/%s/%s", zohoBaseUrl, ZohoFields.JSON_STRING,
				ZohoFields.ACCOUNTS_MODULE_STRING, ZohoFields.GET_RECORDS_STRING);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
				.queryParam(ZohoFields.AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
				.queryParam(ZohoFields.SCOPE_STRING, ZohoFields.CRMAPI_STRING)
				.queryParam(ZohoFields.ID, organizationId);

		RestTemplate restTemplate = new RestTemplate();
		URI uri = builder.build().encode().toUri();
		LOGGER.trace(uri.toString());

		String organisationsResponse = restTemplate.getForObject(uri, String.class);
		LOGGER.debug(organisationsResponse);

		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readTree(organisationsResponse);
		} catch (IOException e) {
			throw new GenericMetisException("Cannot parse zoho response: " + organisationsResponse, e);
		}
	}

	

	/**
	 * Retrieve organizations using start and end index.
	 * <p>
	 * It will try to fetch the organizations from the external CRM. This method
	 * returns a list of organizations in json format.
	 * </p>
	 * 
	 * Example query:
	 * https://crm.zoho.com/crm/private/json/Accounts/getRecords?authtoken=<token>&scope=crmapi&fromIndex=1&toIndex=10
	 *
	 * @param start
	 *            to start search from this index
	 * @param end
	 *            to end search by this index
	 * @return the list of the organizations
	 * @throws GenericMetisException
	 *             which can be one of:
	 *             <ul>
	 *             <li>{@link BadContentException} if any other problem occurred
	 *             while constructing the user, like an organization did not
	 *             have a role defined or the response cannot be converted to
	 *             {@link JsonNode}</li>
	 *             </ul>
	 * @throws IOException
	 */
	public JsonNode getOrganizations(int start, int rows) throws GenericMetisException {

		String contactsSearchUrl = String.format("%s/%s/%s/%s", zohoBaseUrl, ZohoFields.JSON_STRING,
				ZohoFields.ACCOUNTS_MODULE_STRING, ZohoFields.GET_RECORDS_STRING);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
				.queryParam(ZohoFields.AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
				.queryParam(ZohoFields.SCOPE_STRING, ZohoFields.CRMAPI_STRING)
				.queryParam(ZohoFields.FROM_INDEX_STRING, start).queryParam(ZohoFields.TO_INDEX_STRING, start + rows -1);

		RestTemplate restTemplate = new RestTemplate();
		URI uri = builder.build().encode().toUri();
		LOGGER.trace(uri.toString());
		
		String organisationsResponse = restTemplate.getForObject(uri, String.class);
		LOGGER.info(organisationsResponse);
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readTree(organisationsResponse);
		} catch (IOException e) {
			throw new GenericMetisException("Cannot parse zoho response: " + organisationsResponse, e);
		}
	}
}
