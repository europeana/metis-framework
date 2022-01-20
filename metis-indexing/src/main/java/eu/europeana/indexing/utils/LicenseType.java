package eu.europeana.indexing.utils;

import eu.europeana.corelib.web.model.rights.RightReusabilityCategorizer;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.metis.schema.jibx.Rights1;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This enum contains licenses for reuse.
 */
public enum LicenseType implements Comparable<LicenseType> {
  /**
   * Closed license.
   */
  CLOSED(0, MediaTier.T2),

  /**
   * A restricted license.
   */
  RESTRICTED(1, MediaTier.T3),

  /**
   * An open license.
   */
  OPEN(2, MediaTier.T4);

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenseType.class);
  private final int order;
  private final MediaTier mediaTier;

  LicenseType(int order, MediaTier mediaTier) {
    this.order = order;
    this.mediaTier = mediaTier;
  }

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
      result = CLOSED;
    } else if (open == 0 && restricted == 1) {
      result = RESTRICTED;
    } else if (open == 1 && restricted == 0) {
      result = OPEN;
    } else {
      LOGGER.warn("Impossible combination of open and restricted counts: {} and {} respectively.",
          open, restricted);
      result = CLOSED;
    }

    // Done.
    return result;
  }

  public static BinaryOperator<LicenseType> getLicenseBinaryOperator() {
    return BinaryOperator.maxBy(Comparator.comparingInt(LicenseType::getOrder));
  }

  public int getOrder() {
    return order;
  }

  public MediaTier getMediaTier() {
    return mediaTier;
  }
}

