package eu.europeana.enrichment.service.zoho.model;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.enrichment.api.external.model.zoho.ZohoOrganization;
import eu.europeana.enrichment.api.external.model.zoho.ZohoOrganizationField;



/**
 * This class provides interface to Zoho orgnization object.
 * @author GrafR
 *
 */
public class ZohoOrganizationAdapter implements ZohoOrganization {

	/** Fields from Zoho organization */
	private final String ACCOUNTID = "ACCOUNTID";
	private final String ACCOUNT_NAME = "Account Name";
	private final String ACCOUNT_OWNER = "Account Owner";
	private final String LANG_ORGANIZATION_NAME = "Lang Organisation Name";
	private final String WEBSITE = "Website";
	private final String DESCRIPTION = "Description";
	private final String DOMAIN = "Domain";
	private final String STREET = "Street";
	private final String CITY = "City";
	private final String ZIP_CODE = "ZIP code";
	private final String COUNTRY = "Country";
	private final String ALTERNATIVE = "Alternative";
	private final String LANG_ALTERNATIVE = "Lang Alternative";
	private final String SCOPE = "Scope";
	private final String SAME_AS = "SameAs";
	private final String ORGANIZATION_ROLE = "Organisation Role";
	private final String GEOGRAPHIC_LEVEL = "Geographic Level";
	private final String ACRONYM = "Acronym";
	private final String ORGNAIZATION_COUNTRY = "Organisation Country";
	private final String LOGO = "Logo (link to WikimediaCommons)";
	private final String SECTOR = "Sector";
	private final String POST_BOX = "PO box";
	private final String MODIFIED = "Modified Time";
	private final String CREATED = "Created Time";
	private final int MAX_ALTERNATIVES = 5;
	private final int MAX_LANG_ALTERNATIVES = 5;
	private final int MAX_SAME_AS = 3;

	//force 0 timezone
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	List<ZohoOrganizationField> organizationFields = null;
	
	public ZohoOrganizationAdapter(JsonNode response) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();		
		organizationFields = mapper.readValue(
				response.toString(), new TypeReference<List<ZohoOrganizationField>>(){});
	}
	
	/**
	 * This method retrieves 'content' value from array of Zoho 'value'/'content' pairs,
	 * if such a value exists.
	 * @param fieldName The name of the Zoho organization 'val' field.
	 * @return The value of Zoho 'content' field for passed 'val' field
	 */
	private String getContent(String fieldName) {
		String res = "";
		
		ZohoOrganizationField zohoFieldObject = new ZohoOrganizationField();
		zohoFieldObject.setVal(fieldName);
		int fieldIndex = organizationFields.indexOf(zohoFieldObject);
		if (fieldIndex != -1)
			res = organizationFields.get(fieldIndex).getContent();
		return res;
	}
	
	@Override
	public String toString() {
		return getZohoId() + ", " + getOrganizationName();
	}

	@Override
	public boolean equals(Object o) {
		return false;
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
	public String getLanguage() {
		return getContent(LANG_ORGANIZATION_NAME);
	}

	@Override
	public List<String> getAlternativeLanguage() {
		return getFieldArray(LANG_ALTERNATIVE, MAX_LANG_ALTERNATIVES);
	}

	/**
	 * This methods retrieves multiple values of one base field (e.g. alternatives).
	 * It checks maximal number of such fields limited by passed size parameter.
	 * @param fieldBaseName The base field that can have multiple values
	 * @param size The maximal number of fields
	 * @return list of the field values
	 */
	private List<String> getFieldArray(String fieldBaseName, int size) {
		List<String> res = new ArrayList<String>();
		String fieldName = fieldBaseName + " " + "%d";
		for (int i = 0; i < size; i++) {
			String fieldValue = getContent(String.format(fieldName, i));
			//add only existing values
			if(StringUtils.isNotBlank(fieldValue))
				res.add(fieldValue);
		}
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

//	@Override
//	public String getModifiedBy() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public List<String> getSameAs() {
		return getFieldArray(SAME_AS, MAX_SAME_AS);
	}

	@Override
	public String getPostBox() {
		return getContent(POST_BOX);
	}

//	@Override
//	public String getHasAddress() {
//		// TODO Auto-generated method stub
//		return null;
//	}

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
	public Date getModified(){
		String modified = getContent(MODIFIED);
		return getDateOrDefault(modified);
	}

	private Date getDateOrDefault(String dateTime) {
		if(StringUtils.isBlank(dateTime))
			return new Date(0);
		
		try {
			return formatter. parse(dateTime);			
		} catch (ParseException e) {
			throw new RuntimeException("Cannot parse modified date. Wrong format: " + dateTime, e);
		}
	}

	@Override
	public Date getCreated() {
		String created = getContent(CREATED);
		return getDateOrDefault(created);
	}

}

