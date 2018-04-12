package eu.europeana.metis.mediaservice;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.Matrix;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;

class TextProcessor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TextProcessor.class);
	
	static boolean isText(String mimeType) {
		switch (mimeType) {
		case "application/xml":
		case "application/rtf":
		case "application/epub":
		case "application/pdf":
			return true;
		default:
			return mimeType.startsWith("text/");
		}
	}
	
	void processText(String url, Collection<UrlType> urlTypes, String mimeType, File content, EdmObject edm)
			throws IOException {
		if (!MediaProcessor.shouldExtractMetadata(urlTypes))
			return;
		if (content == null)
			throw new IllegalArgumentException("content cannot be null");
		
		boolean containsText = mimeType.startsWith("text/");
		Integer resolution = null;
		
		if ("application/pdf".equals(mimeType)) {
			PdfReader reader = new PdfReader(content.getAbsolutePath());
			try {
				PdfReaderContentParser parser = new PdfReaderContentParser(reader);
				PdfListener pdfListener = new PdfListener();
				for (int i = 1; i <= reader.getNumberOfPages(); i++) {
					parser.processContent(i, pdfListener);
					resolution = pdfListener.dpi;
					containsText = !StringUtils.isBlank(pdfListener.getResultantText());
					if (resolution != null && containsText)
						break;
				}
			} finally {
				reader.close();
			}
		}
		
		WebResource resource = edm.getWebResource(url);
		resource.setMimeType(mimeType);
		resource.setFileSize(content.length());
		resource.setContainsText(containsText);
		resource.setResolution(resolution);
	}
	
	private static class PdfListener extends SimpleTextExtractionStrategy {
		private Integer dpi;
		
		@Override
		public void renderImage(ImageRenderInfo iri) {
			try {
				if (dpi != null)
					return;
				BufferedImage image = iri.getImage().getBufferedImage();
				int wPx = image.getWidth();
				int hPx = image.getHeight();
				
				Matrix m = iri.getImageCTM();
				final int displayDpi = 72;
				double wInch = (double) m.get(Matrix.I11) / displayDpi;
				double hInch = (double) m.get(Matrix.I22) / displayDpi;
				
				long xdpi = Math.abs(Math.round(wPx / wInch));
				long ydpi = Math.abs(Math.round(hPx / hInch));
				dpi = (int) Math.min(xdpi, ydpi);
			} catch (IOException e) {
				LOGGER.info("Could not extract PDF image", e);
			}
		}
	}
}
