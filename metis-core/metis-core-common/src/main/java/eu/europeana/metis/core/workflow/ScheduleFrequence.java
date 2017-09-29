package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-09-25
 */
public enum ScheduleFrequence {
  ONCE, DAILY, WEEKLY, MONTHLY, NULL;

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
