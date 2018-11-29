package eu.europeana.metis.mediaprocessing.temp;

import java.io.Closeable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaprocessing.MediaProcessorException;
import eu.europeana.metis.mediaservice.EdmObject;
import eu.europeana.metis.mediaservice.MediaException;
import eu.europeana.metis.mediaservice.MediaProcessor;
import eu.europeana.metis.mediaservice.MediaProcessor.Thumbnail;
import eu.europeana.metis.mediaservice.UrlType;

@Deprecated
public class TemporaryMediaService extends TemporaryMediaHandler implements Closeable {

  private final MediaProcessor mediaProcessor;

  public TemporaryMediaService() throws MediaProcessorException {
    try {
      mediaProcessor = new MediaProcessor();
    } catch (MediaException e) {
      throw new MediaProcessorException(e);
    }
  }

  // This method is not thread-safe.
  // The files are done sequentially: the next one begins only if the previous one is completed.
  public Pair<RDF, List<Thumbnail>> performMediaProcessing(RDF incomingRdf,
      List<FileInfo> fileInfos, MediaProcessingListener listener) {
    final EdmObject edm = new EdmObject(incomingRdf);
    mediaProcessor.setEdm(edm);
    for (FileInfo file : fileInfos) {
      try {
        listener.beforeStartingFile(file);
        mediaProcessor.processResource(file.getUrl(), file.getMimeType(), file.getContent());
      } catch (Exception e) {
        final boolean stopProcessing;
        if (e instanceof MediaException) {
          stopProcessing = listener.handleMediaException(file, (MediaException) e);
        } else {
          stopProcessing = listener.handleOtherException(file, e);
        }
        if (stopProcessing) {
          return new ImmutablePair<>(mediaProcessor.getEdm().getRdf(),
              mediaProcessor.getThumbnails());
        }
      } finally {
        listener.afterCompletingFile(file);
      }
    }
    final EdmObject resultEdm = mediaProcessor.getEdm();
    updateEdmPreview(resultEdm);
    return new ImmutablePair<>(resultEdm.getRdf(), mediaProcessor.getThumbnails());
  }

  private void updateEdmPreview(EdmObject edm) {
    Set<String> objectUrls = edm.getResourceUrls(Arrays.asList(UrlType.OBJECT)).keySet();
    Set<String> otherUrls =
        edm.getResourceUrls(Arrays.asList(UrlType.IS_SHOWN_BY, UrlType.HAS_VIEW)).keySet();

    if (!objectUrls.isEmpty()) {
      edm.updateEdmPreview(objectUrls.iterator().next());
    } else {
      Optional<Thumbnail> thumbnail = mediaProcessor.getThumbnails().stream()
          .filter(t -> t.targetName.contains("-LARGE") && otherUrls.contains(t.url)).findFirst();

      if (thumbnail.isPresent()) {
        String url = String.format("%s", thumbnail.get().targetName);
        edm.updateEdmPreview(url);
      }
    }
  }

  @Override
  public void close() {
    mediaProcessor.close();
  }

  public interface MediaProcessingListener {

    void beforeStartingFile(FileInfo file) throws MediaException;

    // If this returns true, we will stop processing.
    boolean handleMediaException(FileInfo file, MediaException exception);

    // If this returns true, we will stop processing.
    boolean handleOtherException(FileInfo file, Exception exception);

    void afterCompletingFile(FileInfo file);

  }
}
