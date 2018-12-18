package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdfImpl;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaservice.MediaProcessor;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

@Deprecated
public class TemporaryMediaService implements Closeable {

  private final MediaProcessor mediaProcessor;

  public TemporaryMediaService() throws MediaProcessorException {
    mediaProcessor = new MediaProcessor();
  }

  // This method is probably thread-safe.
  // The files are done sequentially: the next one begins only if the previous one is completed.
  public <I extends DownloadedResource> Pair<RDF, List<Thumbnail>> performMediaProcessing(
      RDF incomingRdf, List<I> sources, MediaProcessingListener<I> listener) {
    final List<ResourceExtractionResult> processedResources = performResourceProcessing(sources, listener);
    return mergeProcessedResources(incomingRdf, processedResources);
  }

  // This method is probably thread-safe.
  // The files are done sequentially: the next one begins only if the previous one is completed.
  // TODO this should be only for one source.
  public <I extends DownloadedResource> List<ResourceExtractionResult> performResourceProcessing(
      List<I> sources, MediaProcessingListener<I> listener) {
    final List<ResourceExtractionResult> result = new ArrayList<>();
    for (I source : sources) {
      try {
        listener.beforeStartingFile(source);
        final File file = source.getContentPath() == null ? null : source.getContentPath().toFile();
        final ResourceExtractionResult resourceResult = mediaProcessor
            .processResource(source.getResourceUrl(), source.getUrlTypes(), source.getMimeType(),
                file);
        if (resourceResult != null) {
          result.add(resourceResult);
        }
      } catch (Exception e) {
        if (e instanceof MediaExtractionException) {
          listener.handleMediaExtractionException(source, (MediaExtractionException) e);
        } else {
          listener.handleOtherException(source, e);
        }
      } finally {
        listener.afterCompletingFile(source);
      }
    }
    return result;
  }

  // This method is probably thread-safe.
  public Pair<RDF, List<Thumbnail>> mergeProcessedResources(RDF incomingRdf,
      List<ResourceExtractionResult> processedResources) {
    final EnrichedRdf enrichedRdf = new EnrichedRdfImpl(incomingRdf);
    processedResources.stream().map(ResourceExtractionResult::getMetadata)
        .forEach(enrichedRdf::enrichResource);
    final List<Thumbnail> thumbnails = processedResources.stream()
        .map(ResourceExtractionResult::getThumbnails).filter(Objects::nonNull).flatMap(List::stream)
        .collect(Collectors.toList());
    return new ImmutablePair<>(enrichedRdf.finalizeRdf(), thumbnails);
  }

  @Override
  public void close() {
    mediaProcessor.close();
  }

  public interface MediaProcessingListener<I extends DownloadedResource> {

    void beforeStartingFile(I source) throws MediaExtractionException;

    void handleMediaExtractionException(I source, MediaExtractionException exception);

    void handleOtherException(I source, Exception exception);

    void afterCompletingFile(I source);

  }
}
