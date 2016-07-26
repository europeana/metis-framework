/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.enrichment.converter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.europeana.enrichment.api.ObjectRule;
import eu.europeana.enrichment.api.Task;
import eu.europeana.enrichment.path.Path;
import eu.europeana.enrichment.triple.Value;
import eu.europeana.enrichment.triple.XmlValue;
import eu.europeana.enrichment.xconverter.api.DataObject;

/**
 * Converter XML/SQL -> RDF. XML handler.
 * 
 * @author Borys Omelayenko
 */
public class ConverterHandler extends DefaultHandler implements DataObject {

	public enum ConversionResult {
		neverRun, success, failure
	};


	Logger log = LoggerFactory.getLogger(getClass().getName());

	private Set<String> tagsToIgnore = new HashSet<String>();

	/**
	 * XML tags that would be completely ignored.
	 * 
	 * @param tag
	 */
	public void addTagToIgnore(String tag) {
		tagsToIgnore.add(tag);
	}

	// mostly for development
	private int maximalRecordsToPass = -1;
	private int currentPassedRecord = 0;

	// to warn if no records are passed
	private boolean passedARecord = false;

	private boolean htmlMode = false;

	/**
	 * The mode when the sub-tags of a tag would be included in tags' value.
	 * Useful for XML that stored HTML inside, as all formatting tags would be
	 * included.
	 * 
	 * @param mode
	 */
	public void setHtmlMode(boolean mode) {
		htmlMode = mode;
	}

	Locator locator;

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	public Locator getDocumentLocator() {
		return locator;
	}

	private String tagBeingIgnored = null;

	// current element stack, used when we deepen into the nested XML tags
	private Stack<Path> tagPath = new Stack<Path>();

	// parts not yet completed
	private Stack<DataObject> partsNotYetCompleted = new Stack<DataObject>();

	// parts already completed
	private Stack<DataObject> partsCompleted = new Stack<DataObject>();

	private DataObject getDataObject() {
		return partsCompleted.peek();
	}

	private Task task;

	/*
	 * Interface DataObject
	 */
	public Task getTask() {
		return task;
	}

	@Override
	public Iterator<Path> iterator() {
		return getDataObject().iterator();
	}

	public ListOfValues getValues(Path propertyName) throws Exception {
		return getDataObject().getValues(propertyName);
	}

	public long size() {
		return getDataObject().size();
	}

	public Path getSeparatingPath() {
		return getDataObject().getSeparatingPath();
	}

	public XmlValue getFirstValue(Path propertyName) throws Exception {
		return getDataObject().getFirstValue(propertyName);
	}

	public void addValue(Path propertyName, XmlValue newValue) throws Exception {
		getDataObject().addValue(propertyName, newValue);
	}

	public List<DataObject> findAllChildren() {
		return getDataObject().findAllChildren();
	}

	public List<DataObject> getChildren() {
		return getDataObject().getChildren();
	}

	public ObjectRule getDataObjectRule() {
		return getDataObject().getDataObjectRule();
	}

	public Path getIdPath() throws Exception {
		return getDataObject().getIdPath();
	}

	public DataObject getParent() {
		return getDataObject().getParent();
	}

	/*
	 * Proxy interface that allows merging multiple files.
	 */
	public void multiFileStartDocument() throws SAXException {
		super.startDocument();
		tagPath = new Stack<Path>();
		tagBeingIgnored = null;
		passedARecord = false;
	}

	public void multiFileEndDocument() throws SAXException {
		super.endDocument();
		if (!passedARecord) {
			throw new SAXException(
					"No single record was passed: an error in the record tag");
		}
	}

	/*
	 * Interface of DefaultHandler
	 */
	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void characters(char[] characters, int begin, int end)
			throws SAXException {
		try {
			if (tagBeingIgnored != null)
				return;

			// append to the top element at the stack / current element
			if (tagPath.size() > 0) {
				tagPath.peek().appendValue(characters, begin, end);
			}
		} catch (Exception e) {
			throw new SAXException("Exception on characaters "
					+ new String(characters), e);
		}
	}

	// raw xml-level filtering
	boolean startRawFilter(String qName) {
		if (tagBeingIgnored != null)
			return true;
		if (tagsToIgnore.contains(qName)) {
			// start ignoring
			tagBeingIgnored = qName;
			return true;
		}
		return false;
	}

	boolean endRawFilter(String qName) {
		if (tagBeingIgnored != null) {
			if (tagBeingIgnored.equals(qName)) {
				// closing ignore
				tagBeingIgnored = null;
				return true;
			} else {
				// other tag - ignore
				return true;
			}
		}
		return false;
	}

	List<ObjectRule> findRule(String namespaceURI, String localName,
			Attributes atts) throws SAXException {
		try {
			Path parent = tagPath.isEmpty() ? null : tagPath.peek();
			tagPath.push(new Path(parent, namespaceURI, localName, atts));
			return task.getRuleForSourcePath(tagPath.peek());
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {

		try {
			if (startRawFilter(namespaceURI + localName)) {
				return;
			}

			List<ObjectRule> rules = findRule(namespaceURI, localName, atts);

			if (rules.size() > 1)
				throw new SAXException("Multiple rules found on path: "
						+ tagPath.peek());

			if (rules.size() == 1) {
				// If a data object rule is found then the current tag
				// starts a new object that is pushed to stack.
				ObjectRule rule = rules.get(0);
				// new element-object. Searching for its parent
				// first instance of the parent ObjectMap is the parent instance
				DataObject parent = null;
				for (DataObject part : partsNotYetCompleted) {
					if (rule.getParent() == part.getDataObjectRule()) {
						parent = part;
						break;
					}
				}
				DataObject newObject = makeDataObjectForNewRecord(
						tagPath.peek(), rule, parent);
				partsNotYetCompleted.push(newObject);
				if (partsNotYetCompleted.size() > 1000) {
					throw new SAXException(
							"Data object has more than 1000 parts. This suggests that the record separating XML path is incorrect");
				}

			}
		} catch (Exception e) {
			throw new SAXException("Exception on starting tag " + namespaceURI
					+ localName, e);
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		try {
			if (endRawFilter(qName)) {
				return;
			}

			// closing a triple
			if (tagPath.size() > 0) {
				// process this tag value
				Path closingTag = tagPath.pop();
				DataObject dataObject = peekNotYetCompletedDataObject();
				if (dataObject != null) {
					XmlValue value = new XmlValue(StringUtils.trim(closingTag
							.getValue()), closingTag.getLang());
					if (value != null) {
						try {
							dataObject.addValue(closingTag, value);
						} catch (Exception e) {
							throw new SAXException("error on " + closingTag, e);
						}

						if (dataObject.size() > 10000) {
							throw new SAXException(
									"Data object is longer than 10000 elements. This suggests that the record separating XML path is incorrect");
						}
					}
				}

				// closing a record
				List<ObjectRule> rules;
				try {
					rules = task.getRuleForSourcePath(closingTag);
				} catch (Exception e) {
					throw new SAXException(e);
				}

				if (rules.size() > 1)
					throw new SAXException("Multiple rules found on path: "
							+ tagPath.peek());

				if (rules.size() == 1) {
					/*
					 * This is the end of a data object
					 */
					// check consistency
					if (closingTag == null)
						throw new SAXException(
								"Data object separator tag is not closed");

					if (!partsNotYetCompleted.peek().getSeparatingPath()
							.equals(closingTag))
						throw new SAXException(
								"Open-close tags mismatch on tag " + localName
										+ " value " + closingTag.getValue());

					// complete this part
					partsCompleted.add(partsNotYetCompleted.pop());

					// if we completed all parts - process them
					if (partsNotYetCompleted.isEmpty()) {
						passedARecord = true;
						currentPassedRecord++;

						while (!partsCompleted.isEmpty()) {
							try {
								processDataObject();
							} catch (Exception e) {
								try {
									reportException(e);
								} catch (Exception ex) {
									throw new SAXException(ex);
								}
								throw new SAXException(e);
							}
							partsCompleted.pop();
						}
					}
				}

				// append this tag value to the parent
				if (htmlMode)
					if (tagPath.size() > 0) {
						// TODO: should be local tag ipv full ns tagPath
						tagPath.peek().appendValue(
								'<' + tagPath.peek().getPath() + '>'
										+ closingTag.getValue() + "</"
										+ tagPath.peek().getPath() + ">");
					}

			} else
				throw new SAXException("Error in XML structure");
		} catch (Exception e) {
			throw new SAXException("Exception on ending tag " + namespaceURI
					+ localName, e);
		}

	}

	private void reportException(Exception e) throws Exception {
		String msg = e.getMessage() + "\n on " + getPrimaryIdTag()
				+ "\n on record id: " + getFirstValue(getPrimaryIdTag())
				+ "\n on record secId: " + getFirstValue(getSecondaryIdTag());
		// print the record
		for (Path propertyName : getDataObject()) {
			List<XmlValue> values = getValues(propertyName);
			String pathStr = propertyName.getPath();
			pathStr = pathStr.substring(pathStr.indexOf("*") + 1);
			pathStr = pathStr.substring(pathStr.indexOf("*") + 1);
			if (values != null) {
				msg += "\n" + pathStr + "-" + values;
			}
		}
		log.error(msg);
		Thread.sleep(1000);
		e.printStackTrace();
	}

	/*
	 * Factory
	 */

	protected DataObject makeDataObjectForNewRecord(Path path, ObjectRule rule,
			DataObject parent) {
		return new DataObjectImpl(path, rule, parent);
	}

	/*
	 * Private methods
	 */

	protected DataObject peekNotYetCompletedDataObject() {
		if (!partsNotYetCompleted.isEmpty())
			return partsNotYetCompleted.peek();
		return null;
	}

	/**
	 * Creates a converter.
	 * 
	 * @param task
	 *            conversion task
	 */
	public ConverterHandler(Task task) {
		this.task = task;
	}

	public void setMaximalRecordsToPass(int maximalRecordsToPass) {
		this.maximalRecordsToPass = maximalRecordsToPass;
	}

	/**
	 * When a part of the source XML representing a data object is passed then
	 * this fragment is converted to RDF with this method.
	 */
	protected void processDataObject() throws Exception {
		if (maximalRecordsToPass > 0
				&& currentPassedRecord > maximalRecordsToPass) {
			// simulate conversion end
			endDocument();
			throw new RuntimeException("Converter ended after "
					+ maximalRecordsToPass + " records (this is debug mode)");
		}
		DataObject dataObject = getDataObject();
		ObjectRule rule = dataObject.getDataObjectRule();

		String subject = null;
		try {
			Value checkedSubjectValue = rule
					.checkConditionsAndGetSubject(dataObject);
			if (checkedSubjectValue != null) {
				subject = checkedSubjectValue.getValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Problem with preprocessor on object rule\n"
					+ getDataObject().getDataObjectRule().toString(), e);
		}

		if (subject == null) {
			// this object should be ignored, remove unprocessed children from
			// the
			// queue
			for (DataObject childDataObject : dataObject.findAllChildren()) {
				if (childDataObject == dataObject)
					throw new Exception(
							"Internal error: when removing child object fo the parent that should not be converted - a child is the same as the father");
				partsCompleted.remove(childDataObject);
			}
		} else {
			// process this object
			try {
				rule.processDataObject(subject, this);
			} catch (Exception e) {
				throw new Exception(
						"Exception with running property rules on object rule\n"
								+ getDataObject().getDataObjectRule()
										.toString(), e);
			}
		}
	}

	protected String getTopCompletedPartSubject() throws Exception {
		return getDataObject().getDataObjectRule().getDataObjectId(
				getDataObject());
	}

	private Path getPrimaryIdTag() {
		return getDataObject().getDataObjectRule().getPrimaryRecordIdPath();
	}

	private Path getSecondaryIdTag() {
		return getDataObject().getDataObjectRule().getSecondaryRecordIdPath();
	}

	private ConversionResult conversionResult = ConversionResult.neverRun;

	public ConversionResult getConversionResult() {
		return conversionResult;
	}

	public void setConversionResult(ConversionResult conversionResult) {
		this.conversionResult = conversionResult;
	}

}