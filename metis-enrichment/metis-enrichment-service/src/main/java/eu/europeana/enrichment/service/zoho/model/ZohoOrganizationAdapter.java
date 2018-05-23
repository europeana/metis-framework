package eu.europeana.enrichment.service.zoho.model;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.enrichment.api.external.model.zoho.ZohoOrganization;
import eu.europeana.enrichment.api.external.model.zoho.ZohoResponseField;
import eu.europeana.enrichment.service.exception.ZohoAccessException;
import eu.europeana.metis.authentication.dao.ZohoApiFields;

/**
 * This class provides interface to Zoho orgnization object.
 * 
 * @author GrafR
 *
 */
public class ZohoOrganizationAdapter implements ZohoOrganization {

	/** Fields from Zoho organization */
	private static final String ACCOUNTID = "ACCOUNTID";
	private static final String ACCOUNT_NAME = "Account Name";
	private static final String ACCOUNT_OWNER = "Account Owner";
	private static final String LANG_ORGANIZATION_NAME = "Lang Organisation Name";
	private static final String WEBSITE = "Website";
	private static final String DESCRIPTION = "Description";
	private static final String DOMAIN = "Domain";
	private static final String STREET = "Street";
	private static final String CITY = "City";
	private static final String ZIP_CODE = "ZIP code";
	private static final String COUNTRY = "Country";
	private static final String ALTERNATIVE = "Alternative";
	private static final String LANG_ALTERNATIVE = "Lang Alternative";
	private static final String SCOPE = "Scope";
	private static final String SAME_AS = "SameAs";
	private static final String ORGANIZATION_ROLE = ZohoApiFields.ORGANIZATION_ROLE;
	private static final String GEOGRAPHIC_LEVEL = "Geographic Level";
	private static final String ACRONYM = "Acronym";
	private static final String LANG_ACRONYM = "Lang Acronym";
	private static final String ORGNAIZATION_COUNTRY = "Organisation Country";
	private static final String LOGO = "Logo (link to WikimediaCommons)";
	private static final String SECTOR = "Sector";
	private static final String POST_BOX = "PO box";
	private static final String MODIFIED = ZohoApiFields.MODIFIED_TIME;
	private static final String CREATED = "Created Time";
	private static final String MODIFIED_BY = "Modified By";
	private static final int MAX_ALTERNATIVES = 5;
	private static final int MAX_LANG_ALTERNATIVES = 5;
	private static final int MAX_SAME_AS = 3;

	private List<ZohoResponseField> organizationFields;

	public ZohoOrganizationAdapter(JsonNode response)
			throws ZohoAccessException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			organizationFields = mapper.readValue(response.toString(),
					new TypeReference<List<ZohoResponseField>>() {
					});
		} catch (IOException e) {
			throw new ZohoAccessException("Cannot parse zoho response ", e);
		}
	}

	/**
	 * This method retrieves 'content' value from array of Zoho
	 * 'value'/'content' pairs, if such a value exists.
	 * 
	 * @param fieldName
	 *            The name of the Zoho organization 'val' field.
	 * @return The value of Zoho 'content' field for passed 'val' field
	 */
	private String getContent(String fieldName) {
		ZohoResponseField zohoFieldObject = new ZohoResponseField();
		zohoFieldObject.setVal(fieldName);
		int fieldIndex = organizationFields.indexOf(zohoFieldObject);
		if (fieldIndex > -1)
			return organizationFields.get(fieldIndex).getContent();
		return null;
	}

	@Override
	public String toString() {
		return getZohoId() + ", " + getOrganizationName();
	}

	@Override
	public String getZohoId() {
		return getContent(ACCOUNTID);
	}

	@Override
	public String getOrganizationName() {
		return getContent(ACCOUNT_NAME);
	}

	@Override
	public List<String> getAlternativeOrganizationName() {
		return getFieldArray(ALTERNATIVE, MAX_ALTERNATIVES);
	}

	@Override
	public String getOrganizationOwner() {
		return getContent(ACCOUNT_OWNER);
	}

	@Override
	public String getAcronym() {
		return getContent(ACRONYM);
	}

	@Override
    public String getLangAcronym() {
		return getContent(LANG_ACRONYM);
	}

	@Override
	public String getDomain() {
		return getContent(DOMAIN);
	}

	@Override
	public String getOrganizationCountry() {
		return getContent(ORGNAIZATION_COUNTRY);
	}

	@Override
	public String getSector() {
		return getContent(SECTOR);
	}

	@Override
	public String getLogo() {
		return getContent(LOGO);
	}

	@Override
	public String getWebsite() {
		return getContent(WEBSITE);
	}

	@Override
	public String getLanguageForOrganizationName() {
		return getContent(LANG_ORGANIZATION_NAME);
	}

	@Override
	public List<String> getAlternativeLanguage() {
		return getFieldArray(LANG_ALTERNATIVE, MAX_LANG_ALTERNATIVES);
	}

	/**
	 * This methods retrieves multiple values of one base field (e.g.
	 * alternatives). It checks maximal number of such fields limited by passed
	 * size parameter.
	 * 
	 * @param fieldBaseName
	 *            The base field that can have multiple values
	 * @param size
	 *            The maximal number of fields
	 * @return list of the field values
	 */
	private List<String> getFieldArray(String fieldBaseName, int size) {
		List<String> res = new ArrayList<String>(size);
		String fieldName = fieldBaseName + " " + "%d";
		for (int i = 0; i < size; i++) {
			String fieldValue = getContent(String.format(fieldName, i));
			// add only existing values
			if (StringUtils.isNotBlank(fieldValue))
				res.add(fieldValue);
		}
		if(res.isEmpty())
			return null;
		return res;
	}

	@Override
	public String getRole() {
		return getContent(ORGANIZATION_ROLE);
	}

	@Override
	public String getScope() {
		return getContent(SCOPE);
	}

	@Override
	public String getGeographicLevel() {
		return getContent(GEOGRAPHIC_LEVEL);
	}

	@Override
	public List<String> getSameAs() {
		return getFieldArray(SAME_AS, MAX_SAME_AS);
	}

	@Override
	public String getPostBox() {
		return getContent(POST_BOX);
	}

	@Override
	public String getStreet() {
		return getContent(STREET);
	}

	@Override
	public String getCity() {
		return getContent(CITY);
	}

	@Override
	public String getZipCode() {
		return getContent(ZIP_CODE);
	}

	@Override
	public String getCountry() {
		return getContent(COUNTRY);
	}

	@Override
	public String getDescription() {
		return getContent(DESCRIPTION);
	}

	@Override
	public Date getModified() {
		String modified = getContent(MODIFIED);
		return getDateOrDefault(modified);
	}

	private Date getDateOrDefault(String dateTime) {
		if (StringUtils.isBlank(dateTime))
			return new Date(0);

		try {
			return ZohoApiFields.getZohoTimeFormatter().parse(dateTime);
		} catch (ParseException e) {
			throw new IllegalArgumentException(
					"Cannot parse modified date. Wrong format: " + dateTime, e);
		}
	}

	@Override
	public Date getCreated() {
		String created = getContent(CREATED);
		return getDateOrDefault(created);
	}

	@Override
	public String getModifiedBy() {
		return getContent(MODIFIED_BY);
	}
}
