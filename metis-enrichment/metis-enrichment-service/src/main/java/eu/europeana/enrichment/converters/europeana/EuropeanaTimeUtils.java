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
package eu.europeana.enrichment.converters.europeana;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.europeana.enrichment.tagger.rules.PairOfStrings;

/**
 * Extract dates. 
 *  
 * @author Borys Omelayenko
 * 
 */
public class EuropeanaTimeUtils
{
    private static Pattern scanIntervalPattern = Pattern.compile("^First date: (\\d\\d)\\.(\\d\\d)\\.(\\d\\d\\d\\d) ?- ?Last date: (\\d\\d)\\.(\\d\\d)\\.(\\d\\d\\d\\d)$");
    private static Pattern betweenIntervalPattern = Pattern.compile("^Between (\\d\\d\\d\\d) and (\\d\\d\\d\\d) (.*)$");
    private static Pattern fiStartPattern = Pattern.compile("^First date: (\\d\\d)\\.(\\d\\d)\\.(\\d\\d\\d\\d)$");
    private static Pattern yearToYearPeriodPattern = Pattern.compile("^(\\d\\d\\d\\d) ?- ?(\\d\\d\\d\\d)$");
    private static Pattern yearToYearBracketsPeriodPattern = Pattern.compile("^\\[(\\d\\d\\d\\d), (\\d\\d\\d\\d)\\]$");
    private static Pattern dateToDatePeriodPattern = Pattern.compile("^(\\d\\d\\d\\d)-01-01/(\\d\\d\\d\\d)-(\\d\\d)-31$");
    private static Pattern exactDatePattern = Pattern.compile("^(\\d\\d\\d\\d)/(\\d\\d)/(\\d\\d)$");
    private static Pattern braketedYearPattern = Pattern.compile("^\\[(\\d\\d\\d\\d)\\]$");
    private static Pattern datePattern = Pattern.compile("^(\\d\\d\\d\\d)(-\\d\\d-\\d\\d)?( 00:00:00)?$");

    public static PairOfStrings splitToStartAndEnd(String label) {
        Matcher m ;

        try {
            m = scanIntervalPattern.matcher(label);
            if (m.find()) {
                String startYear = m.group(3);
                String endYear = m.group(6);
                return new PairOfStrings(startYear, endYear);
            }

            m = betweenIntervalPattern.matcher(label);
            if (m.find()) {
                String startYear = m.group(1);
                String endYear = m.group(2);
                return new PairOfStrings(startYear, endYear);
            }

            m = fiStartPattern.matcher(label);
            if (m.find()) {
                String startYear = m.group(3);
                String endYear = m.group(3);
                return new PairOfStrings(startYear, endYear);
            }

            m = exactDatePattern.matcher(label);
            if (m.find()) {
                String startYear = m.group(1);
                String endYear = m.group(1);
                return new PairOfStrings(startYear, endYear);
            }

            m = yearToYearPeriodPattern.matcher(label);
            if (m.find()) {
                String startYear = m.group(1);
                String endYear = m.group(2);
                return new PairOfStrings(startYear, endYear);
            }

            m = yearToYearBracketsPeriodPattern.matcher(label);
            if (m.find()) {
                String startYear = m.group(1);
                String endYear = m.group(2);
                return new PairOfStrings(startYear, endYear);
            }

            m = dateToDatePeriodPattern.matcher(label);
            if (m.find()) {
                String startYear = m.group(1);
                String endYear = m.group(2);
                return new PairOfStrings(startYear, endYear);
            }

            m = braketedYearPattern.matcher(label);
            if (m.find()) {
                String startYear = m.group(1);
                String endYear = m.group(1);
                return new PairOfStrings(startYear, endYear);
            }

            m = datePattern.matcher(label);
            if (m.find()) {
                String startYear = m.group(1);
                String endYear = m.group(1);
                return new PairOfStrings(startYear, endYear);
            }
        } catch (Exception e) {
            throw new RuntimeException("On string: " + label, e);
        }

        return new PairOfStrings(label, label);
    }

}
