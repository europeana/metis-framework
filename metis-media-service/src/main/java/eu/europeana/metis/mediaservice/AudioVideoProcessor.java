package eu.europeana.metis.mediaservice;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AudioVideoProcessor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AudioVideoProcessor.class);
	
	private static String ffprobeCmd;
	
	private CommandExecutor ce;
	
	AudioVideoProcessor(CommandExecutor ce) {
		this.ce = ce;
		init(ce);
	}
	
	private static synchronized void init(CommandExecutor ce) {
		if (ffprobeCmd != null)
			return;
		try {
			String output = String.join("", ce.runCommand(Arrays.asList("ffprobe"), true));
			if (!output.startsWith("ffprobe version 2") && !output.startsWith("ffprobe version 3"))
				throw new RuntimeException("ffprobe 2.x/3.x not found");
			ffprobeCmd = "ffprobe";
		} catch (IOException e) {
			throw new RuntimeException("Error while looking for ffprobe tools", e);
		}
	}
	
	static boolean isAudioVideo(String mimeType) {
		return mimeType.startsWith("audio/") || mimeType.startsWith("video/");
	}
	
	void processAudioVideo(String url, Collection<UrlType> urlTypes, String mimeType, File contents, EdmObject edm)
			throws MediaException, IOException {
		if (!UrlType.shouldExtractMetadata(urlTypes))
			return;
		
		List<String> command = Arrays.asList(ffprobeCmd, "-v", "quiet", "-print_format", "json",
				"-show_format", "-show_streams", "-hide_banner", contents != null ? contents.getPath() : url);
		List<String> resultLines = ce.runCommand(command, false);
		
		try {
			JSONObject result = new JSONObject(new JSONTokener(String.join("", resultLines)));
			if (contents == null && result.length() == 0) {
				throw new MediaException("Probably downlaod failed", "", null, true);
			}
			WebResource resource = edm.getWebResource(url);
			resource.setMimeType(mimeType);
			resource.setFileSize(result.getJSONObject("format").getLong("size"));
			
			JSONObject videoStream = findStream(result, "video");
			JSONObject audioStream = findStream(result, "audio");
			if (videoStream != null) {
				resource.setDuration(videoStream.getDouble("duration"));
				resource.setBitrate(videoStream.getInt("bit_rate"));
				resource.setWidth(videoStream.getInt("width"));
				resource.setHeight(videoStream.getInt("height"));
				resource.setCodecName(videoStream.getString("codec_name"));
				
				String[] frameRateParts = videoStream.getString("avg_frame_rate").split("/");
				double frameRate = Double.parseDouble(frameRateParts[0]) / Double.parseDouble(frameRateParts[1]);
				resource.setFrameRete(frameRate);
			} else if (audioStream != null) {
				resource.setDuration(audioStream.getDouble("duration"));
				resource.setBitrate(audioStream.getInt("bit_rate"));
				resource.setCahhnels(audioStream.getInt("channels"));
				resource.setSampleRate(audioStream.getInt("sample_rate"));
				resource.setSampleSize(audioStream.getInt("bits_per_sample"));
			} else {
				throw new MediaException("No media streams", "AUDIOVIDEO ERROR");
			}
		} catch (MediaException e) {
			throw e;
		} catch (RuntimeException e) {
			LOGGER.info("Could not parse ffprobe response:\n" + StringUtils.join(resultLines, "\n"), e);
			throw new MediaException("File seems to be corrupted", "AUDIOVIDEO ERROR", e);
		}
	}
	
	private JSONObject findStream(JSONObject data, String codecType) {
		for (Object streamObject : data.getJSONArray("streams")) {
			JSONObject stream = (JSONObject) streamObject;
			if (codecType.equals(stream.getString("codec_type")))
				return stream;
		}
		return null;
	}
	
	static void setCommand(String ffprobe) {
		AudioVideoProcessor.ffprobeCmd = ffprobe;
	}
}
