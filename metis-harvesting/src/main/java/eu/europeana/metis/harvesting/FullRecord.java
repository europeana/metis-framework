package eu.europeana.metis.harvesting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This interface represents a harvested record.
 */
public interface FullRecord {

  /**
   * Makes the record's content available.
   *
   * @param outputStream The output stream to which to write the content. The caller needs to close
   *                     it after use.
   * @throws IOException           In case there was a problem obtaining the content.
   * @throws IllegalStateException In case the record is deleted at source (see
   *                               {@link #isDeleted()}).
   */
  void writeContent(OutputStream outputStream) throws IOException;

  /**
   * Makes the record's content available.
   *
   * @return An input stream containing the record. The caller needs to close it after use.
   * @throws IOException           In case there was a problem obtaining the content.
   * @throws IllegalStateException In case the record is deleted at source (see
   *                               {@link #isDeleted()}).
   */
  InputStream getContent() throws IOException;

  /**
   * @return Whether this record is deleted at source. If the specific harvest type does not support
   * identifying which records are deleted at source, this method will return false.
   */
  boolean isDeleted();

  /**
   * @return The harvesting identifier of the entry. This should be unique. This may be different
   * from the record ID (rdf:about) and may exist only in the context of this harvest.
   */
  String getHarvestingIdentifier();
}
