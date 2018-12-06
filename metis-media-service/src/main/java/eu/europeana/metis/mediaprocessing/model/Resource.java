package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaprocessing.temp.DownloadedResource;
import java.nio.file.Path;

public interface Resource extends TemporaryFile, DownloadedResource {

  String getMimeType();

  Path getContentPath();

}
