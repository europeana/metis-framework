package eu.europeana.metis.mediaprocessing.temp;

import java.nio.file.Path;

@Deprecated
public interface ThumbnailSource {

  String getMimeType();

  String getResourceUrl();

  Path getContentPath();

}
