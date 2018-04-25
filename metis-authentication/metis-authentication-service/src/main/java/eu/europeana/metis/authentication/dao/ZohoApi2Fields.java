package eu.europeana.metis.authentication.dao;

/**
 * Fields used in the Zoho version 2 API request and response fields, but not Model fields. 
 * see @link{ZohoOrganizationAdapter} 
 * @author GrafR
 *
 */
public abstract class ZohoApi2Fields {

    public static final String DELETED_STRING = "deleted";
    public static final String PAGE_STRING = "page";
    public static final String DATA_STRING = "data";
    public static final String DISPLAY_NAME = "display_name";
	  	
	private ZohoApi2Fields(){
	  //this class is "static" and should never be instantiated or subclassed 
	}
}
