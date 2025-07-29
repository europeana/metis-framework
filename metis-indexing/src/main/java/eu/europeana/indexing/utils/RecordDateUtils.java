package eu.europeana.indexing.utils;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.beans.IdBean;
import java.util.Date;
import java.util.Optional;

/**
 * This class contains utilities in relation to setting and updating the various event dates in the
 * records we are about to save/index.
 */
public final class RecordDateUtils {

  /**
   * This is a helper method for updating the created and updated dates in a {@link FullBean}
   * instance as part of the indexing process. The updated date is taken from the
   * <code>updatedDate</code> parameter. If that is null, the current instant ({@link Date#Date()})
   * will be used. The created date will be selected as follows:
   * <ol>
   *   <li>The value of the <code>createdDateOverride</code> parameter,</li>
   *   <li>Or else, the created date of the <code>source</code> parameter,</li>
   *   <li>Or else, the value of the updated date computed as explained above (i.e., the record
   *   is assumed to be new).</li>
   * </ol>
   *
   * @param source              The source to get the created date from. Can be null.
   * @param destination         The destination in which to set the dates. Is not null.
   * @param updatedDate         The new value for the updated date. Can be null.
   * @param createdDateOverride An override value for the creation date. Can be null.
   */
  public static void setUpdateAndCreateTime(IdBean source, FullBean destination,
      Date updatedDate, Date createdDateOverride) {
    final Date nonNullUpdatedDate = Optional.ofNullable(updatedDate).orElseGet(Date::new);
    final Date nonNullCreatedDate;
    if (createdDateOverride == null) {
      nonNullCreatedDate = Optional.ofNullable(source)
          .map(IdBean::getTimestampCreated)
          .orElse(updatedDate);
    } else {
      nonNullCreatedDate = createdDateOverride;
    }
    destination.setTimestampCreated(nonNullCreatedDate);
    destination.setTimestampUpdated(nonNullUpdatedDate);
  }

  /**
   * This is a helper method for updating the created and updated dates in a {@link FullBean}
   * instance as part of the indexing process. The updated date is taken from the
   * <code>updatedDate</code> parameter. If that is null, the current instant ({@link Date#Date()})
   * will be used. The created date will be taken from the <code>createdDate</code> parameter. If
   * that is null, the same value is used as for the updated date (i.e., the record is assumed to be
   * new).
   *
   * @param destination The destination in which to set the dates. Is not null.
   * @param updatedDate The new value for the updated date. Can be null.
   * @param createdDate An override value for the creation date. Can be null.
   */
  public static void setUpdateAndCreateTime(FullBean destination,
      Date updatedDate, Date createdDate) {
    setUpdateAndCreateTime(null, destination, updatedDate, createdDate);
  }
}
