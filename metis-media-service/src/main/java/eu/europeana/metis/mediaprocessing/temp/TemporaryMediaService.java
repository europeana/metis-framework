package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaprocessing.exception.MediaException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaservice.EdmObject;
import eu.europeana.metis.mediaservice.MediaProcessor;
import eu.europeana.metis.mediaservice.UrlType;
import java.io.Closeable;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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
  public <I extends ThumbnailSource> Pair<RDF, List<Thumbnail>> performMediaProcessing(
      RDF incomingRdf, List<I> sources, MediaProcessingListener<I> listener) {
    final EdmObject edm = new EdmObject(incomingRdf);
    mediaProcessor.setEdm(edm);
    for (I source : sources) {
      try {
        listener.beforeStartingFile(source);
        final File file = source.getContentPath() == null ? null : source.getContentPath().toFile();
        mediaProcessor.processResource(source.getResourceUrl(), source.getMimeType(), file);
      } catch (Exception e) {
        final boolean stopProcessing;
        if (e instanceof MediaException) {
          stopProcessing = listener.handleMediaException(source, (MediaException) e);
        } else {
          stopProcessing = listener.handleOtherException(source, e);
        }
        if (stopProcessing) {
          return new ImmutablePair<>(mediaProcessor.getEdm().getRdf(),
              mediaProcessor.getThumbnails());
        }
      } finally {
        listener.afterCompletingFile(source);
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
          .filter(t -> t.getTargetName().contains("-LARGE") && otherUrls.contains(t.getResourceUrl())).findFirst();

      if (thumbnail.isPresent()) {
        String url = String.format("%s", thumbnail.get().getTargetName());
        edm.updateEdmPreview(url);
      }
    }
  }

  @Override
  public void close() {
    mediaProcessor.close();
  }

  public interface MediaProcessingListener<I extends ThumbnailSource> {

    void beforeStartingFile(I source) throws MediaException;

    // If this returns true, we will stop processing.
    boolean handleMediaException(I source, MediaException exception);

    // If this returns true, we will stop processing.
    boolean handleOtherException(I source, Exception exception);

    void afterCompletingFile(I source);

  }
}
