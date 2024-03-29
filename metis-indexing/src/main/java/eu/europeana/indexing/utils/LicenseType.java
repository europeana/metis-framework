package eu.europeana.indexing.utils;

import eu.europeana.corelib.web.model.rights.RightReusabilityCategorizer;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.metis.schema.jibx.Rights1;
import java.util.Comparator;
import java.util.function.BinaryOperator;

/**
 * This enum contains licenses for reuse.
 */
public enum LicenseType {
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
   * @return The license. Is {@link #CLOSED} if not found to be {@link #OPEN} or {@link #RESTRICTED}.
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
    if (open == 1 && restricted == 0) {
      result = OPEN;
    } else if (open == 0 && restricted == 1) {
      result = RESTRICTED;
    } else {
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

