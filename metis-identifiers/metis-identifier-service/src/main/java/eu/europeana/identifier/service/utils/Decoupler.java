/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.identifier.service.utils;

import eu.europeana.corelib.definitions.jibx.*;
import eu.europeana.identifier.service.exceptions.DeduplicationException;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Helper Class that checks if a received EDM record contains
 * 
 * @author Georgios Markakis <gwarkx@hotmail.com>
 * @since 27 Sep 2012
 */
public class Decoupler {
	private IBindingFactory context;


	/**
	 * Default constructor
	 */
	public Decoupler() {
	}

	/**
	 * Method that performs the decoupling on a given EDM xml string.
	 * 
	 * @param edmXML
	 *            the edm xml
	 * @return a list of the decoupled RDF jibx objects
	 * @throws DeduplicationException
	 */
	public List<RDF> decouple(String edmXML) throws DeduplicationException {

		if (edmXML == null) {
			throw new DeduplicationException(
					"Parameter null passed as an argument in Decoupler.decouple(RDF edmXML) method");
		}



		try {

			context = BindingDirectory.getFactory(RDF.class);

			IUnmarshallingContext uctx = context.createUnmarshallingContext();
			RDF edmOBJ = (RDF) uctx.unmarshalDocument(new StringReader(edmXML));

			InfoStub stub = new InfoStub(edmOBJ);
			stub.init();

			if (stub.proxyList.size() == 1) {
				List<RDF> list = new ArrayList<>();

				list.add(edmOBJ);

				return list;
			} else {
				return process(stub);
			}

		} catch (JiBXException e) {

			throw new DeduplicationException(e);
		}

	}

	/**
	 * Populates a given list of RDF resources given a populated "stub" object
	 * 
	 * @param stub
	 *            an object used as information holder for the decoupling
	 *            operation
	 * @return a list of RDF resources
	 */
	private List<RDF> process(InfoStub stub) {
		Vector<RDF> edmList = new Vector<>();

		for (ProxyType proxy : stub.proxyList) {
			RDF cleandoc = new RDF();
			cleandoc.getProxyList().add(proxy);
			appendPrCHOs(proxy, stub, cleandoc);
			List<Aggregation> aggregations = appendAggregations(proxy, stub,
					cleandoc);
			appendEuropeanaAggregations(proxy, stub, cleandoc);
			appendWebResources(aggregations, stub, cleandoc);
			HashSet<String> resrefs = processWebresources(cleandoc);
			appendContextualEntities(proxy, stub, cleandoc, resrefs);
			appendCCLicences(cleandoc, stub);
			edmList.add(cleandoc);
		}

		return edmList;
	}

	/**
	 * Append the cc:License on the RDF
	 * 
	 * @param cleandoc
	 * @param stub
	 */

	private void appendCCLicences(RDF cleandoc, InfoStub stub) {
		Map<String, License> licAbout = new HashMap<String, License>();
		Set<License> found = new HashSet<>();
		for (License lic : stub.licenseList) {
			licAbout.put(lic.getAbout(), lic);
		}
		for (Aggregation aggregation : cleandoc.getAggregationList()) {
			if (licAbout.keySet() != null
					&& aggregation.getRights()!=null && licAbout.keySet().contains(
							aggregation.getRights().getResource())) {
				found.add(licAbout.get(aggregation.getRights().getResource()));
			}
		}

		if (cleandoc.getEuropeanaAggregationList() != null) {
			for (EuropeanaAggregationType aggregation : cleandoc
					.getEuropeanaAggregationList()) {
				if (licAbout.keySet() != null
						&& aggregation.getRights() != null
						&& licAbout.keySet().contains(
								aggregation.getRights().getResource())) {
					found.add(licAbout.get(aggregation.getRights()
							.getResource()));
				}
			}
		}

		if (cleandoc.getWebResourceList() != null) {
			for (WebResourceType wResource : cleandoc.getWebResourceList()) {
				if (licAbout.keySet() != null
						&& wResource.getRights() != null
						&& licAbout.keySet().contains(
								wResource.getRights().getResource())) {
					found.add(licAbout.get(wResource.getRights().getResource()));
				}
			}
		}

		cleandoc.setLicenseList(new ArrayList<>(found));
	}

	/**
	 * Extract contextual resources references from Webresources.
	 * 
	 * @param stub
	 * @param cleandoc
	 */
	private HashSet<String> processWebresources(RDF cleandoc) {

		List<WebResourceType> wrlist = cleandoc.getWebResourceList();
		HashSet<String> refset = new HashSet<String>();

		for (WebResourceType wtype : wrlist) {

			List<ConformsTo> conformsToList = wtype.getConformsToList();
			List<Created> createdList = wtype.getCreatedList();
			List<Description> descriptionList = wtype.getDescriptionList();
			List<Extent> extentList = wtype.getExtentList();
			List<Format> formatList = wtype.getFormatList();
			List<HasPart> hasPartList = wtype.getHasPartList();
			List<IsFormatOf> isFormatOfList = wtype.getIsFormatOfList();
			IsNextInSequence isNextInSequence = wtype.getIsNextInSequence();
			List<IsPartOf> IsPartOfList = wtype.getIsPartOfList();
			List<Issued> issuedList = wtype.getIssuedList();
			List<Rights> rightList = wtype.getRightList();
			List<Source> sourceList = wtype.getSourceList();

			refset.addAll(returnResourceFromList(conformsToList));
			refset.addAll(returnResourceFromList(createdList));
			refset.addAll(returnResourceFromList(descriptionList));
			refset.addAll(returnResourceFromList(extentList));
			refset.addAll(returnResourceFromList(formatList));
			refset.addAll(returnResourceFromList(hasPartList));
			refset.addAll(returnResourceFromList(isFormatOfList));
			refset.add(returnResourceFromClass(isNextInSequence));
			refset.addAll(returnResourceFromList(IsPartOfList));
			refset.addAll(returnResourceFromList(issuedList));
			refset.addAll(returnResourceFromList(rightList));
			refset.addAll(returnResourceFromList(sourceList));

		}

		return refset;
	}

	/**
	 * Appends the related Aggregations to an RDF document given a specific
	 * Proxy object
	 * 
	 * @param proxy
	 *            the proxy object
	 * @param stub
	 *            an object used as information holder for the decoupling
	 *            operation
	 * @param cleandoc
	 *            a JIBX representation of a reconstructed EDM document
	 * @return a copy of the appended Aggregations
	 */
	private List<Aggregation> appendAggregations(ProxyType proxy,
												 InfoStub stub, RDF cleandoc) {
		// Get the Aggregator References
		List<Aggregation> foundaggregationlist = new ArrayList<>();

		for (Aggregation agg : stub.aggregationList) {
			// if the edm:aggregatedCHO property value of the Aggregation equals
			// the rdf:about
			// value of the Proxy then append it to the RDF document
			if (agg.getAggregatedCHO().getResource().equals(proxy.getAbout())) {
				foundaggregationlist.add(agg);
				cleandoc.getAggregationList().add(agg);
			}
		}

		return foundaggregationlist;
	}

	/**
	 * Appends the related EuropeanaAggregations to an RDF document given a
	 * specific Proxy object
	 * 
	 * @param proxy
	 *            the proxy object
	 * @param stub
	 *            an object used as information holder for the decoupling
	 *            operation
	 * @param cleandoc
	 *            a JIBX representation of a reconstructed EDM document
	 * @return a copy of the appended EuropeanaAggregationTypes
	 */
	private List<EuropeanaAggregationType> appendEuropeanaAggregations(
			ProxyType proxy, InfoStub stub, RDF cleandoc) {

		// Get the EuropeanaAggregator References
		List<EuropeanaAggregationType> foundeuaggregationlist = new ArrayList<EuropeanaAggregationType>();

		Vector<EuropeanaAggregationType> euaglist = stub.euaggregationList;

		for (EuropeanaAggregationType euagg : euaglist) {

			if (euagg.getAggregatedCHO().getResource().equals(proxy.getAbout())) {
				foundeuaggregationlist.add(euagg);
				cleandoc.getEuropeanaAggregationList().add(euagg);
			}

		}

		return foundeuaggregationlist;
	}

	/**
	 * Appends the related Aggregations to an RDF document given a list of
	 * Aggregations
	 * 
	 * @param aggregations
	 *            the list of Aggregations to inspect
	 * @param stub
	 *            an object used as information holder for the decoupling
	 *            operation
	 */
	private void appendWebResources(List<Aggregation> aggregations,
			InfoStub stub, RDF cleandoc) {
		HashSet<String> refstring = new HashSet<String>();

		for (Aggregation agg : aggregations) {

			// First try to isolate any possible resource references to
			// WebResources by checking all relevant Aggregation fields
			// (edm:object,edm:isShownBy,edm:isShownAt,edm:hasViewList)
			// and add these references to a Set
			if (agg.getObject() != null
					&& agg.getObject().getResource() != null) {
				String resource = agg.getObject().getResource();
				refstring.add(resource);
			}
			if (agg.getIsShownBy() != null
					&& agg.getIsShownBy().getResource() != null) {
				String resource = agg.getIsShownBy().getResource();
				refstring.add(resource);
			}
			if (agg.getIsShownAt() != null
					&& agg.getIsShownAt().getResource() != null) {
				String resource = agg.getIsShownAt().getResource();
				refstring.add(resource);
			}
			if (agg.getHasViewList() != null && !agg.getHasViewList().isEmpty()) {
				List<HasView> viewlist = agg.getHasViewList();

				for (HasView view : viewlist) {
					refstring.add(view.getResource());
				}
			}
		}

		// Then for all registered Web resources in the "stub" object
		// check if their rdf:about is contained in the refstring
		// set. In case it does then append them to the document
		Vector<WebResourceType> wrlist = stub.webresourceList;

		for (WebResourceType wtype : wrlist) {
			if (refstring.contains(wtype.getAbout())) {
				cleandoc.getWebResourceList().add(wtype);
			}
		}
	}

	/**
	 * Appends the related ContextualEntities to an RDF document given a
	 * specific Proxy object
	 * 
	 * @param proxy
	 *            the proxy object
	 * @param stub
	 *            an object used as information holder for the decoupling
	 *            operation
	 * @param cleandoc
	 *            a JIBX representation of a reconstructed EDM document
	 */
	private void appendContextualEntities(ProxyType proxy, InfoStub stub,
			RDF cleandoc, HashSet<String> refset) {

		// HashSet<String> refset = new HashSet<String>();

		// First itearate the dc & dcterms elements of the given Proxy looking
		// for references
		// to contextual resources. Append these references to the refset
		// HashSet.
		List<eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice> dclist = proxy
				.getChoiceList();

		for (eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice choiceitem : dclist) {
			if (choiceitem.ifAlternative()) {
				refset.add(returnResourceFromClass(choiceitem.getAlternative()));
			}
			if (choiceitem.ifConformsTo()) {
				refset.add(returnResourceFromClass(choiceitem.getConformsTo()));
			}
			if (choiceitem.ifContributor()) {
				refset.add(returnResourceFromClass(choiceitem.getContributor()));
			}
			if (choiceitem.ifCoverage()) {
				refset.add(returnResourceFromClass(choiceitem.getCoverage()));
			}
			if (choiceitem.ifCreated()) {
				refset.add(returnResourceFromClass(choiceitem.getCreated()));
			}
			if (choiceitem.ifCreator()) {
				refset.add(returnResourceFromClass(choiceitem.getCreator()));
			}
			if (choiceitem.ifDate()) {
				refset.add(returnResourceFromClass(choiceitem.getDate()));
			}
			if (choiceitem.ifDescription()) {
				refset.add(returnResourceFromClass(choiceitem.getDescription()));
			}
			if (choiceitem.ifExtent()) {
				refset.add(returnResourceFromClass(choiceitem.getExtent()));
			}
			if (choiceitem.ifFormat()) {
				refset.add(returnResourceFromClass(choiceitem.getFormat()));
			}
			if (choiceitem.ifHasFormat()) {
				refset.add(returnResourceFromClass(choiceitem.getHasFormat()));
			}
			if (choiceitem.ifHasPart()) {
				refset.add(returnResourceFromClass(choiceitem.getHasPart()));
			}
			if (choiceitem.ifHasVersion()) {
				refset.add(returnResourceFromClass(choiceitem.getHasVersion()));
			}
			if (choiceitem.ifIdentifier()) {
				refset.add(returnResourceFromClass(choiceitem.getIdentifier()));
			}
			if (choiceitem.ifIsFormatOf()) {
				refset.add(returnResourceFromClass(choiceitem.getIsFormatOf()));
			}
			if (choiceitem.ifIsPartOf()) {
				refset.add(returnResourceFromClass(choiceitem.getIsPartOf()));
			}
			if (choiceitem.ifIsReferencedBy()) {
				refset.add(returnResourceFromClass(choiceitem
						.getIsReferencedBy()));
			}
			if (choiceitem.ifIsReplacedBy()) {
				refset.add(returnResourceFromClass(choiceitem.getIsReplacedBy()));
			}
			if (choiceitem.ifIssued()) {
				refset.add(returnResourceFromClass(choiceitem.getIssued()));
			}
			if (choiceitem.ifIsVersionOf()) {
				refset.add(returnResourceFromClass(choiceitem.getIsVersionOf()));
			}
			if (choiceitem.ifLanguage()) {
				refset.add(returnResourceFromClass(choiceitem.getLanguage()));
			}
			if (choiceitem.ifMedium()) {
				refset.add(returnResourceFromClass(choiceitem.getMedium()));
			}
			if (choiceitem.ifProvenance()) {
				refset.add(returnResourceFromClass(choiceitem.getProvenance()));
			}
			if (choiceitem.ifPublisher()) {
				refset.add(returnResourceFromClass(choiceitem.getPublisher()));
			}
			if (choiceitem.ifReferences()) {
				refset.add(returnResourceFromClass(choiceitem.getReferences()));
			}
			if (choiceitem.ifRelation()) {
				refset.add(returnResourceFromClass(choiceitem.getRelation()));
			}
			if (choiceitem.ifReplaces()) {
				refset.add(returnResourceFromClass(choiceitem.getReplaces()));
			}
			if (choiceitem.ifRequires()) {
				refset.add(returnResourceFromClass(choiceitem.getRequires()));
			}
			if (choiceitem.ifRights()) {
				refset.add(returnResourceFromClass(choiceitem.getRights()));
			}
			if (choiceitem.ifSpatial()) {
				refset.add(returnResourceFromClass(choiceitem.getSpatial()));
			}
			if (choiceitem.ifSubject()) {
				refset.add(returnResourceFromClass(choiceitem.getSubject()));
			}
			if (choiceitem.ifSource()) {
				refset.add(returnResourceFromClass(choiceitem.getSource()));
			}
			if (choiceitem.ifTableOfContents()) {
				refset.add(returnResourceFromClass(choiceitem
						.getTableOfContents()));
			}
			if (choiceitem.ifTemporal()) {
				refset.add(returnResourceFromClass(choiceitem.getTemporal()));
			}
			if (choiceitem.ifTitle()) {
				refset.add(returnResourceFromClass(choiceitem.getTitle()));
			}
			if (choiceitem.ifType()) {
				refset.add(returnResourceFromClass(choiceitem.getType()));
			}
		}

		// Do the same for the remaining EDM elements in the Proxy
		refset.add(returnResourceFromClass(proxy.getCurrentLocation()));

		refset.addAll(returnResourceFromList(proxy.getHasTypeList()));

		// refset.addAll(returnResourceFromList(proxy.getIncorporateList()));

		// refset.addAll(returnResourceFromList(proxy.getIsDerivativeOfList()));
		//
		// refset.add(returnResourceFromClass(proxy.getIsNextInSequence()));
		//
		// refset.addAll(returnResourceFromList(proxy.getIsRelatedToList()));
		//
		// refset.addAll(returnResourceFromList(proxy.getIsRelatedToList()));
		//
		// refset.add(returnResourceFromClass(proxy.getIsRepresentationOf()));
		//
		// refset.addAll(returnResourceFromList(proxy.getIsSimilarToList()));
		//
		// refset.addAll(returnResourceFromList(proxy.getIsSuccessorOfList()));
		//
		// refset.addAll(returnResourceFromList(proxy.getRealizeList()));

		// Populate the contextualEntities given the references located in
		// refset
		populateContextualEntities(refset, stub, cleandoc);

	}

	/**
	 * Appends the related Povided CHOS to an RDF document given a specific
	 * Proxy object
	 * 
	 * @param proxy
	 *            the proxy object
	 * @param stub
	 *            an object used as information holder for the decoupling
	 *            operation
	 * @param cleandoc
	 *            a JIBX representation of a reconstructed EDM document
	 */
	private void appendPrCHOs(ProxyType proxy, InfoStub stub, RDF cleandoc) {

		Vector<ProvidedCHOType> cholist = stub.prchoList;

		String id = proxy.getAbout();
		for (ProvidedCHOType cho : cholist) {
			if (id.equals(cho.getAbout())) {
				cleandoc.getProvidedCHOList().add(cho);
			}
		}

	}

	/**
	 * Appends the related ContextualEntities to an RDF document given a
	 * specific Set of references to contextual entities.
	 * 
	 * @param refset
	 *            a set of references to resources
	 * @param stub
	 *            an object used as information holder for the decoupling
	 *            operation
	 * @param cleandoc
	 *            a JIBX representation of a reconstructed EDM document
	 */
	private void populateContextualEntities(Set<String> refset, InfoStub stub,
											RDF cleandoc) {

		// Check all contextual entities stored in the "stub" object and for
		// each one of
		// them check if they are present in the refset.

		for (AgentType agtype : stub.agentList) {
			if (refset.contains(agtype.getAbout())) {
				cleandoc.getAgentList().add(agtype);
			}
		}

		for (PlaceType type : stub.placeList) {
			if (refset.contains(type.getAbout())) {
				cleandoc.getPlaceList().add(type);
			}
		}

		for (TimeSpanType type : stub.timeList) {
			if (refset.contains(type.getAbout())) {
				cleandoc.getTimeSpanList().add(type);
			}
		}

		for (Concept type : stub.conceptList) {
			if (refset.contains(type.getAbout())) {
				cleandoc.getConceptList().add(type);
			}
		}
	}

	/**
	 * Invokes the getResource method on a list of objects via reflection
	 * 
	 * @param list
	 *            the list of objects where the operation needs to be applied
	 * @return
	 */
	private <T> List<String> returnResourceFromList(List<T> list) {

		if (list == null)
			return new ArrayList<>();

		List<String> returnList = new ArrayList<String>();

		for (T object : list) {
			String resource = returnResourceFromClass(object);
			if (resource != null) {
				returnList.add(resource);
			}
		}

		return returnList;
	}

	/**
	 * Invokes the getResource method on an object via reflection
	 * 
	 * @param object
	 * @return
	 */
	private <T> String returnResourceFromClass(T object) {

		if (object == null)
			return null;

		Method[] methods = object.getClass().getMethods();

		for (int i = 0; i < methods.length; i++) {

			if (methods[i].getName().equals("getResource")) {

				try {

					if (methods[i].invoke(object) instanceof ResourceOrLiteralType.Resource) {
						ResourceOrLiteralType.Resource resource = (ResourceOrLiteralType.Resource) methods[i]
								.invoke(object);

						if (resource != null) {
							return resource.getResource();
						}
					}

					if (methods[i].invoke(object) instanceof String) {
						String resource = (String) methods[i].invoke(object);
						return resource;
					}

				} catch (IllegalArgumentException e) {

				} catch (IllegalAccessException e) {

				} catch (InvocationTargetException e) {

				}
			}

		}

		return "";

	}

	/**
	 * Inner Class that creates a "registry" of all the EDM entities contained
	 * in the current decoupling process.
	 * 
	 * @author Georgios Markakis <gwarkx@hotmail.com>
	 * @since 1 Oct 2012
	 */
	private class InfoStub {
		// The original
		RDF edmXML;
		Vector<ProvidedCHOType> prchoList = new Vector<>();
		Vector<ProxyType> proxyList = new Vector<>();
		Vector<Aggregation> aggregationList = new Vector<>();
		Vector<EuropeanaAggregationType> euaggregationList = new Vector<>();
		Vector<AgentType> agentList = new Vector<>();
		Vector<Concept> conceptList = new Vector<>();
		Vector<PlaceType> placeList = new Vector<>();
		Vector<TimeSpanType> timeList = new Vector<>();
		Vector<WebResourceType> webresourceList = new Vector<>();
		Vector<Organization> organizationList = new Vector<>();
		Vector<Dataset> datasetList = new Vector<>();
		Vector<License> licenseList = new Vector<>();

		/**
		 * Default constructor
		 * 
		 * @param edmXML
		 */
		public InfoStub(RDF edmXML) {
			this.edmXML = edmXML;
		}

		/**
		 * Initialize the object by appending all elements in the given
		 */
		public void init() {
			if (edmXML.getProxyList() != null) {
				proxyList.addAll(edmXML.getProxyList());
			}

			if (edmXML.getAgentList() != null) {
				agentList.addAll(edmXML.getAgentList());
			}

			if (edmXML.getAggregationList() != null) {
				aggregationList.addAll(edmXML.getAggregationList());
			}

			if (edmXML.getConceptList() != null) {
				conceptList.addAll(edmXML.getConceptList());
			}

			if (edmXML.getEuropeanaAggregationList() != null) {
				euaggregationList.addAll(edmXML.getEuropeanaAggregationList());
			}

			if (edmXML.getPlaceList() != null) {
				placeList.addAll(edmXML.getPlaceList());
			}

			if (edmXML.getProvidedCHOList() != null) {
				prchoList.addAll(edmXML.getProvidedCHOList());
			}

			if (edmXML.getTimeSpanList() != null) {
				timeList.addAll(edmXML.getTimeSpanList());
			}

			if (edmXML.getWebResourceList() != null) {
				webresourceList.addAll(edmXML.getWebResourceList());
			}
			if (edmXML.getDatasetList() != null) {
				datasetList.addAll(edmXML.getDatasetList());
			}
			if (edmXML.getOrganizationList() != null) {
				organizationList.addAll(edmXML.getOrganizationList());
			}
			if (edmXML.getLicenseList() != null) {
				licenseList.addAll(edmXML.getLicenseList());
			}
		}

	}
}
