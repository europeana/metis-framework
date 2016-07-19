package eu.europeana.metis.mapping.model;

/**
 * Type of Function and its XSL equivalent
 * Created by gmamakis on 8-4-16.
 */
public enum FunctionType {

    /**
     * Substring
     */
    FUNCTION_CALL_SUBSTRING {
        @Override
        public String getXslFunction() {
            return "substring";
        }
    },
    /**
     * Substring after
     */
    FUNCTION_CALL_SUBSTRING_AFTER {
        @Override
        public String getXslFunction() {
            return "substring-after";
        }
    },
    /**
     * Substring before
     */
    FUNCTION_CALL_SUBSTRING_BEFORE {
        @Override
        public String getXslFunction() {
            return "sunstring-before";
        }
    },
    /**
     * Substring between
     */
    FUNCTION_CALL_SUBSTRING_BETWEEN {
        @Override
        public String getXslFunction() {
            return "substring-between";
        }
    },
    /**
     * String-replace
     */
    FUNCTION_CALL_REPLACE_STRING {
        @Override
        public String getXslFunction() {
            return "translate";
        }
    },
    /**
     * Replace by regex
     */
    FUNCTION_CALL_REPLACE_REGEX {
        @Override
        public String getXslFunction() {
            return "replace";
        }
    },
    /**
     * Trim
     */
    FUNCTION_CALL_TRIM {
        @Override
        public String getXslFunction() {
            return "trim";
        }
    },
    /**
     * Split
     */
    FUNCTION_CALL_SPLIT {
        @Override
        public String getXslFunction() {
            return "split";
        }
    },
    /**
     * Custom function
     */
    FUNCTION_CALL_CUSTOM {
        @Override
        public String getXslFunction() {
            return "custom";
        }
    },
    /**
     * Tokenize function
     */
    FUNCTION_CALL_TOKENIZE {
        @Override
        public String getXslFunction() {
            return "tokenize";
        }
    },
    /**
     * Value mapping function (intentionally give null as it will not be used)
     */
    FUNCTION_CALL_VALUE{
        @Override
        public String getXslFunction() {
            return null;
        }
    };

    /**
     * Get the XSL function for a given function type
     * @return The XSL representation of the function type
     */
    public abstract String getXslFunction();
}
