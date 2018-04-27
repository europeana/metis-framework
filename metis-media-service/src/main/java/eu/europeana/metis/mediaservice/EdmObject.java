package eu.europeana.metis.mediaservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.HasView;
import eu.europeana.corelib.definitions.jibx.Preview;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

/**
 * Objects of this class represent contents of EDM XML documents that can be
 * modified by {@link MediaProcessor}.
 */
public final class EdmObject {
	
	private static IBindingFactory rdfBindingFactory;
	
	static {
		try {
			rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
		} catch (JiBXException e) {
			throw new RuntimeException("Unable to create binding factory", e);
		}
	}
	
	private final RDF rdf;
	
	private EdmObject(RDF rdf) {
		this.rdf = rdf;
	}
	
	/**
	 * Finds links to resources listed in this EDM. Each resource can be listed
	 * under some URL type.
	 * @param urlTypes types of URLs to find
	 * @return resource URLs mapped to their types
	 */
	public Map<String, List<UrlType>> getResourceUrls(Collection<UrlType> urlTypes) {
		Map<String, List<UrlType>> urls = new HashMap<>();
		Function<String, List<UrlType>> listProd = k -> new ArrayList<>();
		for (Aggregation aggregation : rdf.getAggregationList()) {
			if (urlTypes.contains(UrlType.OBJECT) && aggregation.getObject() != null) {
				urls.computeIfAbsent(aggregation.getObject().getResource(), listProd).add(UrlType.OBJECT);
			}
			if (urlTypes.contains(UrlType.HAS_VIEW) && aggregation.getHasViewList() != null) {
				for (HasView hv : aggregation.getHasViewList())
					urls.computeIfAbsent(hv.getResource(), listProd).add(UrlType.HAS_VIEW);
			}
			if (urlTypes.contains(UrlType.IS_SHOWN_BY) && aggregation.getIsShownBy() != null) {
				urls.computeIfAbsent(aggregation.getIsShownBy().getResource(), listProd).add(UrlType.IS_SHOWN_BY);
			}
			if (urlTypes.contains(UrlType.IS_SHOWN_AT) && aggregation.getIsShownAt() != null) {
				urls.computeIfAbsent(aggregation.getIsShownAt().getResource(), listProd).add(UrlType.IS_SHOWN_AT);
			}
		}
		return urls;
	}
	
	public void updateEdmPreview(String url) {
		Preview preview = new Preview();
		preview.setResource(url);
		
		if (rdf.getEuropeanaAggregationList() == null || rdf.getEuropeanaAggregationList().isEmpty()) {
			EuropeanaAggregationType aggregationType = new EuropeanaAggregationType();
			rdf.setEuropeanaAggregationList(Arrays.asList(aggregationType));
		}
		rdf.getEuropeanaAggregationList().get(0).setPreview(preview);
	}
	
	WebResource getWebResource(String url) {
		if (rdf.getWebResourceList() == null)
			rdf.setWebResourceList(new ArrayList<>());
		for (WebResourceType resource : rdf.getWebResourceList()) {
			if (resource.getAbout().equals(url))
				return new WebResource(resource);
		}
		WebResourceType resource = new WebResourceType();
		resource.setAbout(url);
		rdf.getWebResourceList().add(resource);
		return new WebResource(resource);
	}
	
	/**
	 * Creates {@code EdmObject}s from xml.
	 * <p>
	 * It's recommended to keep an instance for reuse.
	 * <p>
	 * It's not thread safe.
	 */
	public static class Parser {
		private final IUnmarshallingContext context;
		
		public Parser() {
			try {
				context = rdfBindingFactory.createUnmarshallingContext();
			} catch (JiBXException e) {
				throw new AssertionError("JiBX unmarshalling problem", e);
			}
		}
		
		public EdmObject parseXml(InputStream inputStream) throws MediaException {
			try {
				return new EdmObject((RDF) context.unmarshalDocument(inputStream, "UTF-8"));
			} catch (JiBXException e) {
				throw new MediaException("Edm parsing error", "EDM PARSE", e);
			}
		}
	}
	
	/**
	 * Creates xml from {@code EdmObject}s.
	 * <p>
	 * It's recommended to keep an instance for reuse.
	 * <p>
	 * It's not thread safe.
	 */
	public static class Writer {
		private final IMarshallingContext context;
		
		public Writer() {
			try {
				context = rdfBindingFactory.createMarshallingContext();
			} catch (JiBXException e) {
				throw new AssertionError("JiBX marshalling problem", e);
			}
		}
		
		public byte[] toXmlBytes(EdmObject edm) {
			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
				context.marshalDocument(edm.rdf, "UTF-8", null, byteStream);
				return byteStream.toByteArray();
			} catch (IOException | JiBXException e) {
				throw new AssertionError("RDF should always be able to marshall", e);
			}
		}
	}
}
