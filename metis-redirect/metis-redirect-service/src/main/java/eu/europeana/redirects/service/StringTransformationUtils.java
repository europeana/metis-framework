/*
 * Copyright 2007-2013 The Europeana Foundation
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
package eu.europeana.redirects.service;

import org.apache.commons.lang.StringUtils;

/**
 * Util class for handling StringUtils functionality
 *
 * Created by ymamakis on 1/14/16.
 */
public class StringTransformationUtils {

    /**
     * Apply transformations on a string
     * @param val The value to transform
     * @param transformations The transformations to apply. These expose the functionality of StringUtils functions
     * @return
     */
    public static String applyTransformations(String val, String transformations) {
        if (transformations == null) {
            return val;
        }
        String[] transforms = null;
        if (transformations.contains(").")) {
            transforms = StringUtils.split(transformations, ").");
        } else if (!transformations.contains(").") && transformations.endsWith(")")) {
            transforms = new String[]{transformations};
        }
        for (String transform : transforms) {
            if (!transform.endsWith(")")) {
                transform = transform + ")";
            }
            if (StringUtils.startsWith(transform, "replace")) {
                String[] replacements = StringUtils.split(
                        StringUtils.substringBetween(transform, "(", ")"),
                        ",");
                val = StringUtils.replace(val, replacements[0],
                        replacements[1]);
            }
            if (StringUtils.startsWith(transform, "substringBetween(")) {
                String[] replacements = StringUtils.split(
                        StringUtils.substringBetween(transform, "(", ")"),
                        ",");
                val = StringUtils.substringBetween(val, replacements[0],
                        replacements[1]);
            }
            if (StringUtils.startsWith(transform, "substringBeforeFirst(")) {
                String replacements = StringUtils.substringBetween(
                        transform, "(", ")");
                val = StringUtils.substringBefore(val, replacements);
            }
            if (StringUtils.startsWith(transform, "substringBeforeLast(")) {
                String replacements = StringUtils.substringBetween(
                        transform, "(", ")");
                val = StringUtils.substringBeforeLast(val, replacements);
            }
            if (StringUtils.startsWith(transform, "substringAfterLast(")) {
                String replacements = StringUtils.substringBetween(
                        transform, "(", ")");
                val = StringUtils.substringAfterLast(val, replacements);
            }
            if (StringUtils.startsWith(transform, "substringAfterFirst(")) {
                String replacements = StringUtils.substringBetween(
                        transform, "(", ")");
                val = StringUtils.substringAfter(val, replacements);
            }
            if (StringUtils.startsWith(transform, "concatBefore(")) {
                String replacements = StringUtils.substringBetween(
                        transform, "(", ")");
                val = replacements + val;
            }
            if (StringUtils.startsWith(transform, "concatAfter(")) {
                String replacements = StringUtils.substringBetween(
                        transform, "(", ")");
                val = val + replacements;
            }

        }

        return val;
    }

}
