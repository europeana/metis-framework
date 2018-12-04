package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import java.nio.file.Path;

@Deprecated
public interface DownloadedResource extends RdfResourceEntry {

  String getMimeType();

  Path getContentPath();

}
