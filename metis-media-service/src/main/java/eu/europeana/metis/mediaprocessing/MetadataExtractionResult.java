package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaservice.MediaProcessor.Thumbnail;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MetadataExtractionResult {

  private final byte[] recordWithMetadata;
  private final Collection<Thumbnail> thumbnails;

  public MetadataExtractionResult(byte[] recordWithMetadata, List<Thumbnail> thumbnails) {
    this.recordWithMetadata = recordWithMetadata;
    this.thumbnails = new ArrayList<>(thumbnails);
  }

  public InputStream getRecordWithMetadata() {
    return new ByteArrayInputStream(recordWithMetadata);
  }

  public Collection<Thumbnail> getThumbnails() {
    return Collections.unmodifiableCollection(thumbnails);
  }
}
