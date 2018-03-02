package eu.europeana.metis.mapping.validation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * An expression of a validation Rule against which automatic flags are generated
 * @see IsEnumerationFunction,IsUriFunction,IsUrlFunction
 * Created by ymamakis on 6/15/16.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = IsUriFunction.class, name = "isUriFunction"),
        @JsonSubTypes.Type(value = IsUrlFunction.class, name = "isUrlFunction"),
        @JsonSubTypes.Type(value = IsEnumerationFunction.class, name = "isEnumerationFunction"),
        @JsonSubTypes.Type(value = IsLanguageFunction.class,name = "isLanguageFunction"),
        @JsonSubTypes.Type(value = IsDateTypeFunction.class,name = "isDateTypeFunction"),
        @JsonSubTypes.Type(value = IsFloatFunction.class,name = "isFloatFunction"),
        @JsonSubTypes.Type(value = IsBooleanFunction.class,name = "isBooleanFunction")
        })
public interface ValidationFunction {

    String getType();
    /**
     * Execute the functionality specified by a rule on a value
     * @param value The value to check against the rule
     * @return Whether validation fails or passes
     */
    boolean execute(String value);
}
