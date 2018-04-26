package eu.europeana.metis.mediaservice;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Extracts technical metadata and generates thumbnails for web resources. */
public class MediaProcessor implements Closeable {
	
	/** Information about a generated thumbnail */
	public static class Thumbnail {
		/** The original resource url */
		public final String url;
		/** The name this thumbnail should be stored under */
		public final String targetName;
		/** Temporary file with the thumbnail content. Don't forget to remove it! */
		public final File content;
		
		Thumbnail(String url, String targetName, File content) {
			this.url = url;
			this.targetName = targetName;
			this.content = content;
		}
	}
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MediaProcessor.class);
	
	private static Tika tika = new Tika();
	
	private final CommandExecutor commandExecutor;
	private final ImageProcessor imageProcessor;
	private final AudioVideoProcessor audioVideoProcessor;
	private final TextProcessor textProcessor;
	
	private EdmObject edm;
	private Map<String, List<UrlType>> urlTypes;
	
	MediaProcessor(CommandExecutor commandExecutor) {
		this.commandExecutor = commandExecutor;
		imageProcessor = new ImageProcessor(commandExecutor);
		audioVideoProcessor = new AudioVideoProcessor(commandExecutor);
		textProcessor = new TextProcessor();
	}
	
	public MediaProcessor() {
		this(new CommandExecutor());
	}
	
	/**
	 * @param edm future calls to {@link #processResource(String, String, File)}
	 *        will store extracted metadata in given EDM.
	 */
	public void setEdm(EdmObject edm) {
		this.edm = edm;
		urlTypes = edm.getResourceUrls(Arrays.asList(UrlType.values()));
		imageProcessor.thumbnails.clear();
	}
	
	public EdmObject getEdm() {
		return edm;
	}
	
	/**
	 * @param contents downloaded file, can be {@code null} for mime types accepted
	 *        by {@link #supportsLinkProcessing(String)}
	 */
	public void processResource(String url, String providedMimeType, File contents) throws MediaException {
		String mimeType;
		try {
			mimeType = contents != null ? tika.detect(contents) : tika.detect(URI.create(url).toURL());
		} catch (IOException e) {
			throw new MediaException("Mime type checking error", "IOException " + e.getMessage(), e, contents == null);
		}
		
		if (!mimeType.equals(providedMimeType))
			LOGGER.info("Invalid mime type provided (should be {}, was {}): {}", mimeType, providedMimeType, url);
		if (contents == null && !supportsLinkProcessing(mimeType))
			throw new IllegalArgumentException("Contents file is required for mime type " + mimeType);
		
		try {
			if (ImageProcessor.isImage(mimeType))
				imageProcessor.processImage(url, urlTypes.get(url), mimeType, contents, edm);
			if (AudioVideoProcessor.isAudioVideo(mimeType))
				audioVideoProcessor.processAudioVideo(url, urlTypes.get(url), mimeType, contents, edm);
			if (TextProcessor.isText(mimeType))
				textProcessor.processText(url, urlTypes.get(url), mimeType, contents, edm);
		} catch (IOException e) {
			throw new MediaException("I/O error during procesing", "IOException " + e.getMessage(), e);
		}
	}
	
	/**
	 * @return thumbnails for all the image resources processed since the last call
	 *         to {@link #setEdm(EdmObject)}. Remember to remove the files when they
	 *         are no longer needed.
	 */
	public List<Thumbnail> getThumbnails() {
		return new ArrayList<>(imageProcessor.thumbnails);
	}
	
	@Override
	public void close() {
		commandExecutor.shutdown();
	}
	
	/**
	 * @return if true, resources of given type don't need to be downloaded before
	 *         processing.
	 */
	public static boolean supportsLinkProcessing(String mimeType) {
		return AudioVideoProcessor.isAudioVideo(mimeType);
	}
	
	static void setTika(Tika tika) {
		MediaProcessor.tika = tika;
	}
}
