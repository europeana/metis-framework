package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MetadataExtractionResult {

  private final byte[] recordWithMetadata;
  private final Collection<ThumbnailImpl> thumbnails;

  public MetadataExtractionResult(byte[] recordWithMetadata, List<ThumbnailImpl> thumbnails) {
    this.recordWithMetadata = recordWithMetadata;
    this.thumbnails = new ArrayList<>(thumbnails);
  }

  public InputStream getRecordWithMetadata() {
    return new ByteArrayInputStream(recordWithMetadata);
  }

  public Collection<ThumbnailImpl> getThumbnails() {
    return Collections.unmodifiableCollection(thumbnails);
  }
}
