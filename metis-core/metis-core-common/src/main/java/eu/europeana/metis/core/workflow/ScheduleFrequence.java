package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-09-25
 */
public enum ScheduleFrequence {
  ONCE, DAILY, WEEKLY, MONTHLY, NULL;

  /**
   * During json deserialization the name used on the corresponding field is looked up in the list of enum value field names.
   * @param name the value in the json field
   * @return the {@link ScheduleFrequence} value corresponding to the json field value
   */
  @JsonCreator
  public static ScheduleFrequence getScheduleFrequenceFromEnumName(String name){
    for (ScheduleFrequence scheduleFrequence: ScheduleFrequence.values()) {
      if(scheduleFrequence.name().equalsIgnoreCase(name)){
        return scheduleFrequence;
      }
    }
    return NULL;
  }
}
