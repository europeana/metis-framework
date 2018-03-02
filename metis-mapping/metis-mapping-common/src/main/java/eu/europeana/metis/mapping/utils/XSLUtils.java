package eu.europeana.metis.mapping.utils;


import eu.europeana.metis.mapping.model.Clause;
import eu.europeana.metis.mapping.model.ConditionMapping;
import eu.europeana.metis.mapping.model.Function;
import eu.europeana.metis.mapping.model.FunctionType;
import eu.europeana.metis.mapping.model.ValueMapping;
import eu.europeana.metis.mapping.model.ValueMappings;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * XSLUtils
 * TODO: beautify the code
 */
class XSLUtils {
    static final String OMIT_XML_DECLARATION = "<xsl:output omit-xml-declaration=\"yes\" />";

    static String xslStylesheet(String namespace, String excludeNamespaces, String content) {
        if (excludeNamespaces != null && excludeNamespaces.length() > 0) {
            excludeNamespaces = "exclude-result-prefixes=\"" + excludeNamespaces + "\"";
        } else {
            excludeNamespaces="";
        }

        return XMLUtils.XML_HEADER +
                "<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" " +
                "xmlns:xalan=\"http://xml.apache.org/xalan\" " + namespace + " "+  excludeNamespaces + ">" +
                content +
                "</xsl:stylesheet>";
    }

    static String xslTemplate(String match, String content) {
        return "<xsl:template match=\"" + match + "\">\n" + content + "\n</xsl:template>";
    }

    static String rootElement(String rootElem, String content) {
        return "<" + rootElem + ">" + content + "</" + rootElem + ">\n";
    }

    static String xslApplyTemplates(String select) {
        return "<xsl:apply-templates select=\"" + select + "\"/>\n";
    }

    public static String element(String name, String content, String attributes) {
        String result = "<xsl:element name=\"" + name + "\">\n";
        if (attributes != null) result += attributes+"\n";
        if (content != null) result += content+"\n";
        result += "</xsl:element>\n";

        return result;
    }

    public static String element(String name, String content) {
        return XSLUtils.element(name, content, null);
    }

    public static String attribute(String name, String content) {
        return "<xsl:attribute name=\"" + name + "\">" + content + "</xsl:attribute>\n";
    }

    static String xslForEach(String select, String content) {
        return "<xsl:for-each select=\"" + select + "\">" + content + "</xsl:for-each>\n";
    }


    static String conditionTest(List<ConditionMapping> condition, String normaliseBy) {
        String result = "";
        if (condition != null && condition.size() > 0) {
            for (ConditionMapping conditionMapping : condition) {
                if (conditionMapping.getClauses() != null && conditionMapping.getConditionalLogicalOperator() != null) {
                    String logicalop = conditionMapping.getConditionalLogicalOperator();
                    String clauseTest = "";
                    List<Clause> clauses = conditionMapping.getClauses();
                    for (Clause clause : clauses) {

                        String test = XSLUtils.clauseTest(clause, normaliseBy);
                        if (test.length() > 0) {
                            if (clauseTest.length() > 0) {
                                clauseTest += " " + logicalOpXSLTRepresentation(logicalop) + " ";
                            }

                            clauseTest += "(" + test + ")";
                        }
                    }

                    result += clauseTest;
                }
            }
        }

        return result;
    }

    private static String clauseTest(Clause clause, String normaliseBy) {
        String result = "";
        String relationalOp = "EQ";
        String conditionOp = "=";
        if (clause.getConditionRelationOperator() != null) {
            relationalOp = clause.getConditionRelationOperator();
            conditionOp = relationalOpXSLTRepresentation(relationalOp);
        }

        if (isUnaryOperator(relationalOp)) {
            if (clause.getxPathMapping() != null) {
                String conditionXPath = clause.getxPathMapping();
                if (conditionXPath.length() > 0) {
                    String testXPath = XSLUtils.normaliseXPath(conditionXPath, normaliseBy);
                    if (relationalOp.equals("EXISTS")) {
                        result += testXPath;
                    } else if (relationalOp.equals("NOTEXISTS")) {
                        result += "not(" + testXPath + ")";
                    }
                }
            }
        } else if (isFunctionOperator(relationalOp)) {
            if (clause.getxPathMapping() != null && clause.getValueMapping() != null) {
                String conditionXPath = clause.getxPathMapping();
                String conditionValue = clause.getValueMapping();
                if (conditionXPath.length() > 0) {
                    String testXPath = XSLUtils.normaliseXPath(conditionXPath, normaliseBy);
                    switch (relationalOp) {
                        case "CONTAINS":
                            result = testXPath + "[contains(., '" + conditionValue + "')]";
                            break;
                        case "NOTCONTAINS":
                            result = testXPath + "[not(contains(., '" + conditionValue + "'))]";
                            break;
                        case "STARTSWITH":
                            result = testXPath + "[starts-with(., '" + conditionValue + "')]";
                            break;
                        case "NOTSTARTSWITH":
                            result = testXPath + "[not(starts-with(., '" + conditionValue + "'))]";
                            break;
                        case "ENDSWITH":
                            result = testXPath + "[ends-with(., '" + conditionValue + "')]";
                            break;
                        case "NOTENDSWITH":
                            result = testXPath + "[not(ends-with(., '" + conditionValue + "'))]";
                            break;
                    }
                }
            }
        } else {
            if (clause.getxPathMapping() != null && clause.getValueMapping() != null) {
                String conditionXPath = clause.getxPathMapping();
                String conditionValue = clause.getValueMapping();
                if (conditionXPath.length() > 0) {
                    String testXPath = XSLUtils.normaliseXPath(conditionXPath, normaliseBy);
                    result += testXPath + " " + conditionOp + " '" + XSLUtils.escapeConstant(conditionValue) + "'";
                }
            }
        }
        return result;
    }


    private static String logicalOpXSLTRepresentation(String logicalop) {
        if (logicalop != null) {
            if (logicalop.equalsIgnoreCase("AND")) {
                return "and";
            } else if (logicalop.equalsIgnoreCase("OR")) {
                return "or";
            }
        }

        return "and";
    }

    private static boolean isUnaryOperator(String operator) {
        return operator.equals("EXISTS") || operator.equals("NOTEXISTS");
    }

    private static boolean isFunctionOperator(String operator) {
        return operator.equals("CONTAINS") || operator.equals("NOTCONTAINS") ||
                operator.equals("STARTSWITH") || operator.equals("NOTSTARTSWITH") ||
                operator.equals("ENDSWITH") || operator.equals("NOTENDSWITH");
    }

    private static String relationalOpXSLTRepresentation(String relationalop) {
        if (relationalop != null) {
            if (relationalop.equalsIgnoreCase("EQ")) {
                return "=";
            } else if (relationalop.equalsIgnoreCase("NEQ")) {
                return "!=";
            }
        }

        return "=";
    }

    static String xslValueOf(String select) {
        if (select == null) {
            return "<xsl:value-of select=\".\"/>\n";
        } else {
            return "<xsl:value-of select=\"" + select + "\"/>\n";
        }
    }

    static String function(Function function) {
        String result = XSLUtils.xslValueOf(null);

        if (function != null && function.getArguments() != null) {
            FunctionType call = function.getType();
            String[] args = function.getArguments();
            // create array of arguments with escaped values
            List<String> arguments = new ArrayList<>();
            for (String arg : args) {
                if (call == FunctionType.FUNCTION_CALL_TOKENIZE ||
                        call == FunctionType.FUNCTION_CALL_SPLIT)
                    arguments.add(XSLUtils.escapeConstant(arg));
                else
                    arguments.add(arg);
            }

            if (call == FunctionType.FUNCTION_CALL_SUBSTRING) {
                result = XSLUtils.xslValueOf(call.getXslFunction() + "(.," + arguments.get(0) + ((arguments.get(1) != null && arguments.get(1).length() > 0) ? "," + arguments.get(1) : "") + ")");
            } else if (call == FunctionType.FUNCTION_CALL_SUBSTRING_AFTER) {
                result = XSLUtils.xslValueOf(call.getXslFunction() + "(.,'" + arguments.get(0) + "')");
            } else if (call == FunctionType.FUNCTION_CALL_SUBSTRING_BEFORE) {
                result = XSLUtils.xslValueOf(call.getXslFunction() + "(.,'" + arguments.get(0) + "')");
            } else if (call == FunctionType.FUNCTION_CALL_SUBSTRING_BETWEEN) {
                result = XSLUtils.xslValueOf("substring-before(substring-after(.,'" + arguments.get(0) + "'), '" + arguments.get(1) + "')");
            } else if (call == FunctionType.FUNCTION_CALL_REPLACE_REGEX) {
                result = XSLUtils.xslValueOf(call.getXslFunction() + "(., '" + arguments.get(0) + "', '" + arguments.get(1) + "')");
            } else if (call == FunctionType.FUNCTION_CALL_REPLACE_STRING) {
                result = XSLUtils.xslValueOf(call.getXslFunction() + "(., '" + arguments.get(0) + "', '" + arguments.get(1) + "')");

            } else if (call == FunctionType.FUNCTION_CALL_SPLIT) {
                // how can you split in xsl ???
                String varname = "split";
                result = "<xsl:variable name=\"" + varname + "\" select=\"tokenize(.,'" + arguments.get(0) + "')\"/>";
                result += "<xsl:value-of select=\"$" + varname + "[" + arguments.get(1) + "]\"/>";
            } else if (call == FunctionType.FUNCTION_CALL_CUSTOM) {
                result = XSLUtils.xslValueOf(arguments.get(0));
            } else {
                result = XSLUtils.xslValueOf(null);
            }

        } else if (function != null && function.getType() == FunctionType.FUNCTION_CALL_TRIM) {
            result = XSLUtils.xslValueOf("normalize-space(.)");
        }
        return result;
    }

    static String xslIf(String test, String content) {
        return "<xsl:if test=\"" + test + "\">" + content + "</xsl:if>\n";
    }

    /**
     * Wrap content in an xsl:if statement only if test is not null or empty.
     *
     * @param test    condition test.
     * @param content "then" part of xsl:if statement.
     * @return
     */
    static String xslIfOptional(String test, String content) {
        if (test != null && test.length() > 0) return XSLUtils.xslIf(test, content);
        else return content;
    }

    /**
     * Wraps content in an xsl:if statement that checks if current selection is the first in its sequence, to limit execution to only the first possible element.
     *
     * @param content "then" part of xsl:if statement.
     * @return
     */
    static String xslIfFirst(String content) {
        return XSLUtils.xslIf("position() = 1", content);
    }

    static String xslWhen(String test, String content) {
        return "<xsl:when test=\"" + test + "\">" + content + "</xsl:when>\n";
    }

    static String xslOtherwise(String content) {
        return "<xsl:otherwise>" + content + "</xsl:otherwise>\n";
    }

    static String xslChoose(String content) {
        return "<xsl:choose>" + content + "</xsl:choose>\n";
    }

    static String escapeConstant(String c) {
        String result = c;
        result = result.replace("'", "''");
        result = result.replace("\\", "\\\\");
        result = StringEscapeUtils.escapeXml(result);
        result = result.replace("*", "\\*");
        result = result.replace("|", "\\|");
        result = result.replace("^", "\\^");
        result = result.replace("$", "\\$");
        result = result.replace("(", "\\(");
        result = result.replace(")", "\\)");
        result = result.replace("[", "\\[");
        result = result.replace("]", "\\]");
        result = result.replace(".", "\\.");
        return result;
    }

    static String xslComment(String comment) {
        return "<xsl:comment>" + comment + "</xsl:comment>\n";
    }

    /**
     * Generates an XML comment if the xsl.generator.addComments parameter is set.
     *
     * @param comment Contents of the comment. Contents are not escaped.
     * @return
     */
    static String comment(String comment) {
        return "<!-- " + XSLUtils.escapeConstant(comment) + " -->\n";
    }

    private static String normaliseXPath(String string, String prefix) {
        String result = string;

        if (prefix != null) {
            // if two xpaths are the same, return "."
            if (string.equals(prefix)) {
                return ".";
                // if string xpath starts with prefix xpath
            } else if (result.indexOf(prefix + "/") == 0) {
                result = result.replaceFirst(prefix + "/", "");
                // otherwise try to find common root and work from there
            } else {
                String[] tokens1 = string.split("/");
                String[] tokens2 = prefix.split("/");

                int commonStartIndex = -1;
                for (int i = 0; i < tokens1.length; i++) {
                    if (tokens2.length > i) {
                        if (tokens1[i].equals(tokens2[i])) {
                            commonStartIndex++;
                        } else break;
                    }
                }

                if (commonStartIndex >= 0) {
                    result = "";
                    for (int i = 0; i < tokens2.length - commonStartIndex - 1; i++) {
                        if (result.length() > 0 && !result.endsWith("/")) {
                            result += "/";
                        }
                        result += "src/main";
                    }

                    for (int i = commonStartIndex + 1; i < tokens1.length; i++) {
                        if (result.length() > 0 && !result.endsWith("/")) {
                            result += "/";
                        }
                        result += tokens1[i];
                    }
                }
            }
        }

        return result;
    }

    static String normaliseXPath(String xpath, List<Clause> condition, String prefix) {
        String normalised = XSLUtils.normaliseXPath(xpath, prefix);

        if (condition != null) {

            String test = "";
            for (Clause clause : condition) {
                test += XSLUtils.clauseTest(clause, xpath);
            }
            if (test.length() > 0) {
                normalised += "[" + test + "]";
            }
        }

        return normalised;
    }

    static String constant(String value) {
        String result = "";
        result += "<xsl:text>";
        String textValue = StringEscapeUtils.escapeXml(value);
        if (textValue.trim().length() == 0) {
            textValue = textValue.replaceAll(" ", "&#160;");
        }
        result += textValue;
        result += "</xsl:text>";

        return result;
    }

    private static String xslVariable(String name, String select) {
        return "<xsl:variable name=\"" + name + "\" select=\"" + select + "\"/>";
    }

    static String parameterValue(String parameterName) {
        return XSLUtils.xslValueOf("$" + parameterName);
    }

    /**
     * Class that manages declaration of parameters
     *
     * @author Fotis Xenikoudakis
     */
    static class Parameters {
        private Map<String, String> parameterDefaults;
        private Set<String> addedParameters = new HashSet<>();
        private StringBuffer parameters = new StringBuffer();

        public String toString() {
            return parameters.toString();
        }

        void reset() {
            parameterDefaults = null;
            addedParameters.clear();
            parameters = new StringBuffer();
        }

        void setDefaults(Map<String, String> defaults) {
            parameterDefaults = defaults;
        }

        public void add(String name) {
            if (!addedParameters.contains(name)) {
                addedParameters.add(name);

                String defaultValue = "";
                if (parameterDefaults != null && parameterDefaults.containsKey(name)) {

                    defaultValue = parameterDefaults.get(name);
                }
                parameters.append("<xsl:param name=\"").append(name).append("\">")
                        .append(defaultValue).append("</xsl:param>");
            }
        }
    }

    /**
     * Class that manages declaration of variables
     *
     * @author Fotis Xenikoudakis
     */
    static class Variables {
        private StringBuffer variables = new StringBuffer();
        private int count = 0;

        public String toString() {
            return variables.toString();
        }

        void reset() {
            variables = new StringBuffer();
            count = 0;
        }

        public int getCount() {
            return this.count;
        }

        public String getNextName(String prefix) {
            String name = "" + (this.count++);
            if (prefix != null) name = prefix + name;
            return name;
        }

        class VariableSet {
            String index;
            String map;

            VariableSet(String map, String index) {
                this.index = index;
                this.map = map;
            }

            VariableSet(String map) {
                this.map = map;
            }

            public String getIndex() {
                return this.index;
            }

            public String getMap() {
                return this.map;
            }

            public String getIndexOfTest() {
                return "index-of($" + this.map + "/item, replace(.,'^\\s*(.+?)\\s*$', '$1')) &gt; 0";
            }

            String getValueOf() {
                return "<xsl:value-of select=\"$" + this.map + "/map[$" + this.index + "]/@value\"/>";
            }

            String getIndexOfVariable() {
                return XSLUtils.xslVariable(index, "index-of($" + map + "/map, replace(.,'^\\s*(.+?)\\s*$', '$1'))");
            }

            String valueMapping(String whenValueExists, String otherwise) {
                String variable = this.getIndexOfVariable();
                String whenTest = "$" + index + " &gt; 0";
                String chooseContent = XSLUtils.xslWhen(whenTest, whenValueExists);
                if (otherwise != null && otherwise.length() > 0) chooseContent += XSLUtils.xslOtherwise(otherwise);

                return variable + XSLUtils.xslChoose(chooseContent);
            }

            String enumeration(String ifValueExists) {
                return XSLUtils.xslIf("index-of($" + this.map + "/item, replace(.,'^\\s*(.+?)\\s*$', '$1')) &gt; 0", ifValueExists);
            }
        }


        /**
         * Creates variables related to a new value mapping section as described by valuemap parameter. Returns index variable name that holds the index value of the
         *
         * @return
         */
        VariableSet addValueMapping(ValueMappings valueMappings) {
            // create index and map variable names
            String map = valueMappings.getKey();
            String index = valueMappings.getIndex();

            variables.append("<xsl:variable name=\"").append(map).append("\">");

            for (ValueMapping vm : valueMappings.getMappings()) {


                variables.append("<map value=\"").append(vm.getKey()).append("\">").append(vm.getValue().trim()).append("</map>");

            }
            variables.append("</xsl:variable>");

            return new VariableSet(map, index);
        }

        VariableSet addEnumeration(List<String> enumerations, String variable) {

            variables.append("<xsl:variable name=\"").append(variable).append("\">");

            for (String e : enumerations) {

                e = StringEscapeUtils.escapeXml(e);
                variables.append("<item>").append(e).append("</item>");
            }
            variables.append("</xsl:variable>");

            return new VariableSet(variable);
        }
    }
}

