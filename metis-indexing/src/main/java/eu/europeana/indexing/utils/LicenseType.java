package eu.europeana.indexing.utils;

import eu.europeana.corelib.definitions.jibx.Rights1;
import eu.europeana.corelib.web.model.rights.RightReusabilityCategorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This enum contains licenses for reuse.
 */
public enum LicenseType {

  /**
   * An open license.
   */
  OPEN,

  /**
   * A restricted license.
   */
  RESTRICTED;

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenseType.class);

  /**
   * Retrieves the license corresponding to the given url.
   *
   * @param rights The url denoting the license.
   * @return The license. Is null if not found to be {@link #OPEN} or {@link #RESTRICTED}.
   */
  public static LicenseType getLicenseType(Rights1 rights) {

    // Sanity check.
    if (rights == null || rights.getResource() == null) {
      return null;
    }

    // Find the reuse policy.
    final RightReusabilityCategorizer categorizer = new RightReusabilityCategorizer();
    categorizer.categorize(rights.getResource(), 1);
    final long open = categorizer.getNumberOfOpen();
    final long restricted = categorizer.getNumberOfRestricted();

    // Analyze the results.
    final LicenseType result;
    if (open == 0 && restricted == 0) {
      result = null;
    } else if (open == 0 && restricted == 1) {
      result = RESTRICTED;
    } else if (open == 1 && restricted == 0) {
      result = OPEN;
    } else {
      LOGGER.warn("Impossible combination of open and restricted counts: {} and {} respectively.",
          open, restricted);
      result = null;
    }

    // Done.
    return result;
  }
}
