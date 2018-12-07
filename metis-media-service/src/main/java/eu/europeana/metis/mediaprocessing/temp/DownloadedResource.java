package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.metis.mediaprocessing.model.ResourceFile;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.nio.file.Path;
import java.util.Set;

@Deprecated
public interface DownloadedResource extends ResourceFile {

  String getMimeType();

  Path getContentPath();

  Set<UrlType> getUrlTypes();

}
