package eu.europeana.metis.authentication.dao;

import java.util.TimeZone;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * Fields used in the Zoho request and response fields, but not Model fields. see @link{ZohoOrganizationAdapter} 
 * @author GordeaS
 *
 */
public abstract class ZohoApiFields {

	public static final String CONTACTS_MODULE_STRING = "Contacts";
	public static final String ACCOUNTS_MODULE_STRING = "Accounts";
	public static final String SEARCH_RECORDS_STRING = "searchRecords";
	public static final String AUTHENTICATION_TOKEN_STRING = "authtoken";
	public static final String SCOPE_STRING = "scope";
	public static final String LAST_MODIFIED_TIME = "lastModifiedTime";
	public static final String CRITERIA_STRING = "criteria";
	public static final String RESPONSE_STRING = "response";
	public static final String RESULT_STRING = "result";
	public static final String ROW_STRING = "row";
	public static final String JSON_STRING = "json";
	public static final String CRMAPI_STRING = "crmapi";
	public static final String ORGANIZATION_NAME_FIELD = "Account Name";
	public static final String EMAIL_FIELD = "Email";
	public static final String VALUE_LABEL = "val";
	public static final String CONTENT_LABEL = "content";
	public static final String FIELDS_LABEL = "FL";
	public static final String GET_RECORDS_STRING = "getRecords";
	public static final String ID = "id";
	public static final String FROM_INDEX_STRING = "fromIndex";
	public static final String TO_INDEX_STRING = "toIndex";
	public static final String ZOHO_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String SORT_COLUMN = "sortColumnString";
	public static final String SORT_ORDER = "sortOrderString";
	public static final String SORT_ORDER_DESC = "desc";
	public static final String SORT_ORDER_ASC = "asc";
	public static final String MODIFIED_TIME = "Modified Time";
    public static final String ORGANIZATION_ROLE = "Organisation Role";
    public static final String DELIMITER_COMMA = ",";
    public static final String SEMICOLON = ";";
    public final static String OR = "OR";

	  
	private static final FastDateFormat formatter = FastDateFormat.getInstance(ZOHO_TIME_FORMAT, TimeZone.getTimeZone("GMT")) ; 
	
	public static FastDateFormat getZohoTimeFormatter() {
		return formatter;
	}
	
	private ZohoApiFields(){
	  //this class is "static" and should never be instantiated or subclassed 
	}
}
