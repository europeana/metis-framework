/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved by the
 * European Commission; You may not use this work except in compliance with the Licence.
 * 
 * You may obtain a copy of the Licence at: http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, without warranties or conditions of any kind, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.europeana.indexing.fullbean;

import java.io.IOException;
import java.net.MalformedURLException;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.solr.entity.AgentImpl;

/**
 * Constructor of Agent Fields.
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */

final class AgentFieldInput {

  /**
   * Create new Agent MongoDB Entity from JiBX Agent Entity
   * 
   * @param agentType
   * @return
   * @throws IOException
   * @throws MalformedURLException
   */
  AgentImpl createNewAgent(AgentType agentType) throws IOException {
    AgentImpl agent = new AgentImpl();
    agent.setAbout(agentType.getAbout());

    agent.setDcDate(FieldInputUtils.createResourceOrLiteralMapFromList(agentType.getDateList()));
    agent.setDcIdentifier(FieldInputUtils.createLiteralMapFromList(agentType.getIdentifierList()));
    agent.setEdmHasMet(FieldInputUtils.createResourceMapFromList(agentType.getHasMetList()));
    agent.setEdmIsRelatedTo(
        FieldInputUtils.createResourceOrLiteralMapFromList(agentType.getIsRelatedToList()));
    agent.setFoafName(FieldInputUtils.createLiteralMapFromList(agentType.getNameList()));
    agent.setRdaGr2BiographicalInformation(
        FieldInputUtils.createLiteralMapFromString(agentType.getBiographicalInformation()));
    agent.setRdaGr2DateOfBirth(
        FieldInputUtils.createLiteralMapFromString(agentType.getDateOfBirth()));
    agent.setRdaGr2DateOfDeath(
        FieldInputUtils.createLiteralMapFromString(agentType.getDateOfDeath()));
    agent.setRdaGr2PlaceOfBirth(
        FieldInputUtils.createResourceOrLiteralMapFromString(agentType.getPlaceOfBirth()));
    agent.setRdaGr2PlaceOfDeath(
        FieldInputUtils.createResourceOrLiteralMapFromString(agentType.getPlaceOfDeath()));
    agent.setRdaGr2DateOfEstablishment(
        FieldInputUtils.createLiteralMapFromString(agentType.getDateOfEstablishment()));
    agent.setRdaGr2DateOfTermination(
        FieldInputUtils.createLiteralMapFromString(agentType.getDateOfTermination()));
    agent.setRdaGr2Gender(FieldInputUtils.createLiteralMapFromString(agentType.getGender()));
    agent.setRdaGr2ProfessionOrOccupation(FieldInputUtils
        .createResourceOrLiteralMapFromList(agentType.getProfessionOrOccupationList()));
    agent.setNote(FieldInputUtils.createLiteralMapFromList(agentType.getNoteList()));
    agent.setPrefLabel(FieldInputUtils.createLiteralMapFromList(agentType.getPrefLabelList()));
    agent.setAltLabel(FieldInputUtils.createLiteralMapFromList(agentType.getAltLabelList()));
    agent.setBegin(FieldInputUtils.createLiteralMapFromString(agentType.getBegin()));
    agent.setEnd(FieldInputUtils.createLiteralMapFromString(agentType.getEnd()));
    agent.setOwlSameAs(FieldInputUtils.resourceListToArray(agentType.getSameAList()));
    return agent;
  }
}
