package eu.europeana.metis.utils;

import java.util.regex.Pattern;

/**
 * Contains common used String values
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-02
 */
public final class CommonStringValues {

  public static final String WRONG_ACCESS_TOKEN = "Wrong access token";
  public static final String COULD_NOT_PARSE_USER_RETURNED_FROM_ZOHO = "Could not parse user returned from Zoho";
  public static final String BATCH_OF_DATASETS_RETURNED = "Batch of: {} datasets returned, using batch nextPage: {}";
  public static final String NEXT_PAGE_CANNOT_BE_NEGATIVE = "nextPage cannot be a negative number";
  public static final String PAGE_COUNT_CANNOT_BE_ZERO_OR_NEGATIVE = "pageCount cannot be zero or a negative number";
  public static final String PLUGIN_EXECUTION_NOT_ALLOWED = "Plugin Execution Not Allowed";
  public static final String UNAUTHORIZED = "Unauthorized";
  public static final String EUROPEANA_ID_CREATOR_INITIALIZATION_FAILED = "EuropeanaIdCreator initialization failed.";

  public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
  public static final String DATE_FORMAT_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  public static final String DATE_FORMAT_FOR_SCHEDULING = "yyyy-MM-dd'T'HH:mm:ssXXX";
  public static final String DATE_FORMAT_FOR_REQUEST_PARAM = "yyyy-MM-dd'T'HH:mm:ssZ";

  public static final String S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE = "%s/data-providers/%s/data-sets/%s";

  public static final String REPLACEABLE_CRLF_CHARACTERS_REGEX = "[\r\n\t]";

  public static final Pattern CRLF_PATTERN = Pattern.compile(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX);

  private CommonStringValues() {
  }
}
