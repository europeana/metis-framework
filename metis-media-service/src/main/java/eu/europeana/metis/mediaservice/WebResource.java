package eu.europeana.metis.mediaservice;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WebResource {
	private final Element element;
	
	WebResource(Element element) {
		this.element = element;
	}
	
	public void setWidth(int width) {
		setValues(NS.EBUCORE, "width", Type.INT, width);
	}
	
	public void setHeight(int height) {
		setValues(NS.EBUCORE, "height", Type.INT, height);
	}

	public void setMimeType(String mimeType) {
		setValues(NS.EBUCORE, "hasMimeType", null, mimeType);
	}
	
	public void setFileSize(long fileSize) {
		setValues(NS.EBUCORE, "fileByteSize", Type.LONG, fileSize);
	}
	
	public void setColorspace(String colorspace) {
		if (!"grayscale".equals(colorspace) && !"sRGB".equals(colorspace))
			throw new IllegalArgumentException("Unrecognized color space: " + colorspace);
		setValues(NS.EDM, "hasColorSpace", null, colorspace);
	}
	
	public void setOrientation(boolean landscape) {
		setValues(NS.EDM, "orientation", Type.STRING, landscape ? "landscape" : "portrait");
	}
	
	public void setDominantColors(List<String> dominantColors) {
		setValues(NS.EDM, "componentColor", null, dominantColors.stream()
				.peek(c -> {
					if (!c.matches("[0-9A-F]{6}"))
						throw new IllegalArgumentException();
				})
				.map(c -> "#" + c) // TODO dominant colors start with '#' due to legacy systems
				.toArray());
	}
	
	public void setDuration(double duration) {
		setValues(NS.EDM, "duration", null, duration);
	}
	
	public void setBitrate(int bitrate) {
		setValues(NS.EDM, "bitRate", Type.UINT, bitrate);
	}

	public void setFrameRete(double frameRate) {
		setValues(NS.EDM, "frameRate", Type.DOUBLE, frameRate);
	}

	public void setCodecName(String codecName) {
		setValues(NS.EDM, "codecName", null, codecName);
	}
	
	public void setCahhnels(int channels) {
		setValues(NS.EDM, "audioChannelNumber", Type.UINT, channels);
	}
	
	public void setSampleRate(int sampleRate) {
		setValues(NS.EDM, "sampleRate", Type.INT, sampleRate);
	}
	
	public void setSampleSize(int sampleSize) {
		setValues(NS.EDM, "sampleSize", Type.INT, sampleSize);
	}
	
	public void setContainsText(boolean containsText) {
		if (containsText) {
			Element typeElement = setValues(NS.RDF, "type", null, (Object) null).get(0); // create empty rdf:type
			typeElement.setAttribute("rdf:resource", "http://www.europeana.eu/schemas/edm/FullTextResource");
		} else {
			setValues(NS.RDF, "type", null); // remove rdf:type
		}
	}
	
	public void setResolution(Integer resolution) {
		if (resolution != null) {
			setValues(NS.EDM, "spatialResolution", Type.UINT, resolution);
		} else {
			setValues(NS.EDM, "spatialResolution", null); // remove resolution
		}
	}
	
	private List<Element> setValues(NS namespace, String fieldName, String dataType, Object... values) {
		NodeList nodeList = element.getElementsByTagName(fieldName);
		for (int i = 0; i < nodeList.getLength(); i++)
			element.removeChild(nodeList.item(i));
		
		ArrayList<Element> children = new ArrayList<>();
		for (Object val : values) {
			Element child = element.getOwnerDocument().createElementNS(namespace.uri, fieldName);
			children.add(child);
			element.appendChild(child);
			if (dataType != null)
				child.setAttribute("rdf:datatype", "http://www.w3.org/2001/XMLSchema#" + dataType);
			if (val != null)
				child.setTextContent(val.toString());
		}
		return children;
	}
	
	/** Xml name spaces */
	static enum NS {
		EBUCORE("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#"),
		EDM("http://www.europeana.eu/schemas/edm/"),
		RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
		;
		
		public final String uri;
		
		NS(String uri) {
			this.uri = uri;
		}
		
		public String prefix() {
			return name().toLowerCase();
		}
	}
	
	private static class Type {
		static final String STRING = "string";
		static final String LONG = "long";
		static final String INT = "integer";
		static final String DOUBLE = "double";
		static final String UINT = "nonNegativeInteger";
	}
}
