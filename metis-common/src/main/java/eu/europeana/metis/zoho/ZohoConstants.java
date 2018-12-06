package eu.europeana.metis.zoho;

/**
 * Final class containing Zoho constants used for modules, fields, operations.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-12-06
 */
public final class ZohoConstants {

  //Modules
  public static final String CONTACTS_MODULE = "Contacts";
  public static final String ACCOUNTS_MODULE = "Accounts";

  //Fields
  //Accounts is the equivalent to Organizations
  public static final String ID_FIELD = "id";
  public static final String ACCOUNT_NAME_FIELD = "Account_Name"; //This is the organization Name in Zoho
  public static final String FIRST_NAME_FIELD = "First_Name";
  public static final String LAST_NAME_FIELD = "Last_Name";
  public static final String EMAIL_FIELD = "Email";
  public static final String COUNTRY_FIELD = "Country";
  public static final String PARTICIPATION_LEVEL_FIELD = "Participation_level";
  public static final String METIS_USER_FIELD = "Metis_user";
  public static final String ACCOUNT_ROLE_FIELD = "Pick_List_3"; // This is the Account/Organization Role field
  public static final String ORGANIZATION_ROLE_FIELD = "Organisation_Role2";

  //Operations
  public static final String EQUALS_OPERATION = "equals";

  //General constants
  public static final String ZOHO_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
  public static final String DELIMITER_COMMA = ",";
  public static final String OR = "OR";


  private ZohoConstants() {
  }
}
