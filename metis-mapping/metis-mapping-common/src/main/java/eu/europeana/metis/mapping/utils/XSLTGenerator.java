package eu.europeana.metis.mapping.utils;


import eu.europeana.metis.mapping.model.Attribute;
import eu.europeana.metis.mapping.model.Clause;
import eu.europeana.metis.mapping.model.ConditionMapping;
import eu.europeana.metis.mapping.model.Element;
import eu.europeana.metis.mapping.model.Function;
import eu.europeana.metis.mapping.model.FunctionType;
import eu.europeana.metis.mapping.model.IMapping;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.model.MappingType;
import eu.europeana.metis.mapping.model.SimpleMapping;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XSLT generator from a {@link Mapping}
 * TODO: beautify the code
 */
public class XSLTGenerator {
    public static final Logger log = LoggerFactory.getLogger(XSLTGenerator.class);

    public static final String OPTION_ADD_COMMENTS = "xsl.generator.addComments";
    public static final String OPTION_ADD_XSL_DEBUG_COMMENTS = "xsl.generator.addXslDebugComments";

    public static final String OPTION_SKIP_CHECK_FOR_MISSING_MANDATORY_MAPPINGS = "xsl.generator.skipCheckForMissingMandatoryMappings";
    public static final String OPTION_COMPUTE_ITEM_XPATH_IF_NOT_SET = "xsl.generator.computeItemXPathIfNotSet";
    public static final String OPTION_OMIT_XML_DECLARATION = "xsl.generator.omitXMLDeclaration";

    private String root = null;
    public XSLUtils.Variables variables = new XSLUtils.Variables();
    public XSLUtils.Parameters parameters = new XSLUtils.Parameters();
    private HashMap<String, Boolean> options = new HashMap<String, Boolean>();
    private Stack<String> xpathPrefix = new Stack<String>();
    private Map<String,List<String>> enumerations = new HashMap<>();

    /**
     * Default constructor
     */
    public XSLTGenerator() {
        this.setOption(OPTION_ADD_COMMENTS, true);
        this.setOption(OPTION_ADD_XSL_DEBUG_COMMENTS, false);
        this.setOption(OPTION_COMPUTE_ITEM_XPATH_IF_NOT_SET, false);
        this.setOption(OPTION_SKIP_CHECK_FOR_MISSING_MANDATORY_MAPPINGS, false);
        log.debug(options.toString());
    }



    /**
     * Set an option for this generator.
     *
     * @param option Option name
     * @param value  Option value (boolean)
     */
    public void setOption(String option, Boolean value) {
        this.options.put(option, value);
    }

    /**
     * Get option value of this generator.
     *
     * @param option Option name.
     */
    public boolean getOption(String option) {
        Boolean result = this.options.get(option);
        if (result == null) return false;
        else return result.booleanValue();
    }

    /**
     * Sets the XPath that represents that item's top element. This will be used as the match xpath for the template that will handle the transformation.
     *
     * @param xpath
     */
    public void setItemXPath(String xpath) {
        this.root = xpath;
    }

    public String getItemXPath() {
        return this.root;
    }


    /**
     * Generate XSL stylesheet from a json object based on mapping format.
     *
     * @param mapping json object in mapping format.
     * @return XSL stylesheet in a string.
     */
    public String generateFromMappings(Mapping mapping) {
        variables.reset();
        parameters.reset();
        String stylesheetNamespace = "";
        StringBuilder sb = new StringBuilder();
        setItemXPath(mapping.getTargetSchema().getRootPath().getXpath());
        if (mapping.getParameters() != null) {
            parameters = new XSLUtils.Parameters();
            parameters.setDefaults(mapping.getParameters());
        }
        if (mapping.getMappings().getNamespaces() != null) {
            Map<String, String> namespaces = mapping.getMappings().getNamespaces();
            for (String o : namespaces.keySet()) {
                String key = o;
                String value = namespaces.get(key);
                sb.append("xmlns:" + value + "=\"" + key + "\" ");
            }
        }
        stylesheetNamespace = sb.toString();
        StringBuffer content = new StringBuffer();
        String match = this.getItemXPath();
        String template = this.generateTemplate(mapping, match);
        if (this.getOption(OPTION_OMIT_XML_DECLARATION)) content.append(XSLUtils.OMIT_XML_DECLARATION);
        String defaultTemplate = XSLUtils.xslApplyTemplates(match);
        content.append(variables.toString());
        content.append(parameters.toString());
        content.append(XSLUtils.xslTemplate("/", defaultTemplate));
        content.append(template);
        String result = XSLUtils.xslStylesheet(stylesheetNamespace, null, content.toString());
        log.debug(result);
        return result;
    }

    /**
     * Generate an XSL template based on an XPath
     * @param template The mapping to analyze
     * @param match The element to create the template from
     * @return
     */
    private String generateTemplate(Mapping template, String match) {
        String result;
        if (template != null) {
            xpathPrefix.push(match);
            result = generate(template);
            xpathPrefix.pop();
        } else result = comment("warning: template json has no name set");

        return XSLUtils.xslTemplate(match, XSLUtils.rootElement(template.getMappings().getRootElement(),result));
    }

    /**
     * Generates the XSL code related to an element and its descendants.
     *
     * @param item Mapping handler for a mapping element.
     * @return XSL code.
     */
    private String generate(Mapping item) {
        String result = "";
        if (item.getMappings().isHasMappings()) {
            result += generateMappings(item, item.getTargetSchema().getMandatoryXpath());
        }
        return result;
    }

    private String generateMappings(Element elem, Set<String> mandatory,String root) {
        String generatedMappings = "";
        String result = "";
        if ((elem.getMappings() != null && elem.getMappings().size() > 0) || (elem.getConditionalMappings() != null && elem.getConditionalMappings().size() > 0)) {
            if (elem.getMappings() != null && elem.getMappings().size() > 0) {

                generatedMappings = generateWithMappings(elem, elem.getMappings(),root);
                result += generatedMappings;
            }
            if (elem.getConditionalMappings() != null && elem.getConditionalMappings().size() > 0) {
                generatedMappings += generateWithMappings(elem, elem.getConditionalMappings(),root);
                String test = conditionTest(elem.getConditionalMappings());
                if (test != null && test.length() > 0) {
                    result += XSLUtils.xslWhen(test, generatedMappings);
                } else {
                    result += XSLUtils.xslOtherwise(generatedMappings);
                }
            }
            //if (result.length() > 0) {
            //    result = XSLUtils.xslChoose(result);
            //}
        }
        //else if (elem.isHasMapping()) {
        //    result = generateWithInternalMappings(elem, mandatory);
        //}
        /*
        if (!this.getOption(OPTION_SKIP_CHECK_FOR_MISSING_MANDATORY_MAPPINGS)&&elem.isHasMapping()) {

            if (mandatory != null && mandatory.size() > 0) {
                String conditionTest = "";
                for (String xpath : mandatory) {
                    if (conditionTest.length() > 0) conditionTest += " and ";
                    conditionTest += normaliseXPath(xpath);
                }
                result = comment("Check for mandatory elements on " + elem.getPrefix() + ":" + elem.getName()) + XSLUtils.xslIf(StringUtils.substringAfter(conditionTest,root+"/"), result);
            }
        }*/
        return result;
    }

    private String generateWithInternalMappings(Element elem, Set<String> mandatory,String root) {
        String result = "";
        String name = elem.getPrefix() + ":" + elem.getName();
        String attr = "";
        String elemStr = "";
        if(elem.getAttributes()!=null) {
            for (Attribute at : elem.getAttributes()) {
                attr += generateAttribute(at);
            }
        }
        if(elem.getElements()!=null) {
            for (Element child : elem.getElements()) {
                elemStr += generateMappings(child, mandatory,root);
            }
        }
      //  result += XSLUtils.xslForEach(name, XSLUtils.xslIfOptional("", XSLUtils.element(name, elemStr, attr)));
        result += XSLUtils.xslForEach(name, XSLUtils.element(name, elemStr, attr));
        return result;
    }


    private String generateMappings(Mapping item, Set<String> mandatory) {
        String result= "";
        if (item.getMappings().isHasMappings()) {
            List<Element> elements = item.getMappings().getElements();
            List<Attribute> attributes = item.getMappings().getAttributes();
            if (attributes != null && attributes.size() > 0) {
                for (Attribute attribute : attributes) {
                    result += generateAttribute(attribute);
                }
            }
            for (Element elem : elements) {
                result += generateMappings(elem, elem.getMandatoryXpath(),item.getMappings().getRootElement());
            }
        }
        return result;
    }

    private String generateAttributes(Element item) {
        StringBuffer result = new StringBuffer();
        if (item.getAttributes() != null) {
            for (Attribute attribute : item.getAttributes()) {
                result.append(generateAttribute(attribute));
            }
        }
        return result.toString();
    }

    private String generateSingleCaseAttributeMapping(Attribute item, List<ConditionMapping> aCase) {
        return XSLUtils.xslIfOptional(conditionTest(aCase), generateAttributeMappings(item, aCase));
    }

    private String generateAttribute(Attribute item) {
        String result = "";
        if ((item.getMappings() != null && item.getMappings().size() > 0) || (item.getConditionalMappings() != null && item.getConditionalMappings().size() > 0)) {
            String generatedMappings = generateAttributeMappings(item, null);
            result += generatedMappings;
            if (item.getConditionalMappings() != null && item.getConditionalMappings().size() > 1) {
                generatedMappings += generateAttributeMappings(item, item.getConditionalMappings());
                String test = conditionTest(item.getConditionalMappings());
                if (test != null && test.length() > 0) {
                    result += XSLUtils.xslWhen(test, generatedMappings);
                } else {
                    result += XSLUtils.xslOtherwise(generatedMappings);
                }
                //if (result.length() > 0) {
                //    result = XSLUtils.xslChoose(result);
                //}
            } else if (item.getConditionalMappings() != null && item.getConditionalMappings().size() == 1) {
                result = generateSingleCaseAttributeMapping(item, item.getConditionalMappings());
            }
        }
        return result;
    }

    private String generateWithMappings(Element item, List<? extends SimpleMapping> aCase,String root) {
        String result = "";
        String name = item.getPrefix() + ":" + item.getName();
        boolean enumExists = false;
        if(enumerations.containsKey(name)) {
            enumExists = true;
        } else {
            enumerations.put(name, item.getEnumerations());
        }
        if (aCase.size() > 0) {
            if (aCase.size() > 1) {
                return generateWithMappingsConcat(item, aCase);
            } else {
                IMapping simpleMapping = aCase.get(0);
                if (simpleMapping.getType() == MappingType.XPATH) {
                    String select = StringUtils
                        .contains(((SimpleMapping)simpleMapping).getSourceField(),"/")?
                            StringUtils.substringAfterLast(((SimpleMapping)simpleMapping).getSourceField(),"/"):
                            ((SimpleMapping)simpleMapping).getSourceField();
                    xpathPrefix.push(name);
                    String attributes = generateAttributes(item);

                    String content ="";


                    if(item.getElements()!=null){
                        for(Element elem:item.getElements()){
                            content+=generateMappings(elem,elem.getMandatoryXpath(),root);
                        }
                    } else {
                        content+=XSLUtils.function(simpleMapping.getFunction());
                    }
                    String element = XSLUtils.element(name, content, attributes);
                    xpathPrefix.pop();
                    boolean hasValueMappings = simpleMapping.getValueMappings() != null;
                    if (hasValueMappings) {
                        XSLUtils.Variables.VariableSet set = variables.addValueMapping(simpleMapping.getValueMappings());
                        String whenValueExists = XSLUtils.element(name, set.getValueOf(), attributes);
                        whenValueExists += this.debugComment(XSLUtils.xslValueOf("$" + set.index));
                        String otherwise = element;
                        result += set.valueMapping(whenValueExists, otherwise);
                    } else {
                        result = element;

                        if (enumerations!=null && enumerations.get(name) != null && !enumExists) {
                            XSLUtils.Variables.VariableSet set = variables.addEnumeration(enumerations.get(name), name);
                            result = set.enumeration(result);
                        }
                    }
                    result = XSLUtils.xslIfFirst(result);

                    boolean tokenize = false;
                    Function function = simpleMapping.getFunction();
                    if (function != null && function.getType() == FunctionType.FUNCTION_CALL_TOKENIZE) {
                        String delimeter = ",";
                        if (function.getArguments().length > 0) {
                            delimeter = XSLUtils.escapeConstant(function.getArguments()[0]);
                        }
                        if (simpleMapping.getClass().isAssignableFrom(ConditionMapping.class)) {
                            select = normaliseXPath(name, ((ConditionMapping) simpleMapping).getClauses());
                            String select2 = "tokenize(" + ".,'" + delimeter + "')"; //+ seslect + "[1],'" +
                            result = result.replaceAll("@", Matcher.quoteReplacement("$match/@"));
                            result = XSLUtils.xslForEach(select2, result);
                            tokenize = true;
                        }
                    }
                    if (tokenize)
                        result = XSLUtils.xslForEach(select, "<xsl:variable name=\"match\" select=\".\"/>" + result);
                    else
                        result = XSLUtils.xslIf(select,XSLUtils.xslForEach(select, result));
                } else if (simpleMapping.getType() == MappingType.CONSTANT) {
                    result += XSLUtils.element(name, XSLUtils.constant(simpleMapping.getConstant()), generateAttributes(item));
                } else if (simpleMapping.getType() == MappingType.PARAMETER) {
                    parameters.add(simpleMapping.getParameter());
                    result += XSLUtils.element(name, XSLUtils.parameterValue(simpleMapping.getParameter()), generateAttributes(item));
                }
            }
        }

        return result;
    }

    private String generateAttributeMappings(Attribute item, List<ConditionMapping> mappings) {
        String result = "";
        boolean needsCheckIfEmpty = true;
        String check = "";
        if (item.getMappings() != null) {
            for (SimpleMapping mapping : item.getMappings()) {
                boolean hasValueMappings = mapping.getFunction() != null && mapping.getFunction().getType() == FunctionType.FUNCTION_CALL_VALUE;
                if (mapping.getType() == MappingType.XPATH) {
                    String value = mapping.getSourceField();
                    String select = StringUtils.substringAfterLast(value,"/");
                    xpathPrefix.push(value);
                    String content = XSLUtils.function(mapping.getFunction());
                    if (hasValueMappings) {
                        XSLUtils.Variables.VariableSet set = variables.addValueMapping(mapping.getValueMappings());
                        content = set.valueMapping(set.getValueOf() + debugComment(XSLUtils.xslValueOf("$" + set.index)), content);
                    }
                    content = XSLUtils.xslIfFirst(content);
                    if (check.length() > 0) check += " or ";
                    check += select;
                    content = XSLUtils.xslForEach(select, content);
                    result += content;
                    xpathPrefix.pop();
                } else if (mapping.getType() == MappingType.CONSTANT) {
                    needsCheckIfEmpty = false;
                    result += XSLUtils.constant(mapping.getConstant());
                } else if (mapping.getType() == MappingType.PARAMETER) {
                    parameters.add(mapping.getParameter());
                    result += XSLUtils.parameterValue(mapping.getParameter());
                }
            }
            result = XSLUtils.attribute(StringUtils.substringAfter(item.getPrefix(),"@") + ":" + item.getName(), result);
            if (needsCheckIfEmpty) {
                result = XSLUtils.xslIfOptional(check, result);
            }
        }
        return result;
    }


    private String generateWithMappingsConcat(Element item, List<? extends SimpleMapping> aCase) {
        String concatenation = "";
        String name = item.getPrefix() + ":" + item.getName();
        for (IMapping mapping : aCase) {
            String result = "";
            if (mapping.getType() == MappingType.XPATH) {
                String normalised = "";
                if (mapping.getClass().isAssignableFrom(SimpleMapping.class)) {
                    normalised += normaliseXPath(name, null);
                } else {
                    normalised += normaliseXPath(name, ((ConditionMapping) mapping).getClauses());
                }
                xpathPrefix.push(name);
                if (mapping.getValueMappings() != null) {
                    XSLUtils.Variables.VariableSet set = variables.addValueMapping(mapping.getValueMappings());
                    result = set.valueMapping(set.getValueOf() + debugComment(XSLUtils.xslValueOf("$" + set.index)), "");
                } else {
                    result = XSLUtils.function(mapping.getFunction());
                }
                result = XSLUtils.xslForEach(normalised, result);
                xpathPrefix.pop();
            } else if (mapping.getType() == MappingType.CONSTANT) {
                result = XSLUtils.constant(mapping.getConstant());
            } else if (mapping.getType() == MappingType.PARAMETER) {
                parameters.add(name);
                result = XSLUtils.parameterValue(mapping.getParameter());
            }
            concatenation += result;
        }
        return XSLUtils.element(name, concatenation, generateAttributes(item));
    }

    private String getNormalisationPrefix() {
        if (!xpathPrefix.empty()) {
            return xpathPrefix.peek();
        }
        return null;
    }


	/*
	 * XSLUtils wrappers
	 */

    private String normaliseXPath(String xpath) {
        return XSLUtils.normaliseXPath(xpath, null, getNormalisationPrefix());
    }

    private String normaliseXPath(String xpath, List<Clause> condition) {
        return XSLUtils.normaliseXPath(xpath, condition, getNormalisationPrefix());
    }

    private String conditionTest(List<ConditionMapping> condition) {
        return XSLUtils.conditionTest(condition, getNormalisationPrefix());
    }

    private String debugComment(String comment) {
        String result = "";
        if (this.getOption(XSLTGenerator.OPTION_ADD_XSL_DEBUG_COMMENTS)) {
            return XSLUtils.xslComment(comment);
        }
        return result;
    }

    private String comment(String comment) {
        String result = "";
        if (this.getOption(XSLTGenerator.OPTION_ADD_COMMENTS)) {
            return XSLUtils.comment(comment);
        }
        return result;
    }

}
