package eu.europeana.indexing.fullbean;

import java.util.function.Function;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.solr.entity.AgentImpl;

/**
 * Converts a {@link AgentType} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to a
 * {@link AgentImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class AgentFieldInput implements Function<AgentType, AgentImpl> {

  @Override
  public AgentImpl apply(AgentType agentType) {
    AgentImpl agent = new AgentImpl();
    agent.setAbout(agentType.getAbout());

    agent.setDcDate(FieldInputUtils.createResourceOrLiteralMapFromList(agentType.getDateList()));
    agent.setDcIdentifier(FieldInputUtils.createLiteralMapFromList(agentType.getIdentifierList()));
    agent.setEdmHasMet(FieldInputUtils.createResourceMapFromList(agentType.getHasMetList()));
    agent.setEdmIsRelatedTo(
        FieldInputUtils.createResourceOrLiteralMapFromList(agentType.getIsRelatedToList()));
    agent.setFoafName(FieldInputUtils.createLiteralMapFromList(agentType.getNameList()));
    agent.setRdaGr2BiographicalInformation(FieldInputUtils
        .createResourceOrLiteralMapFromList(agentType.getBiographicalInformationList()));
    agent.setRdaGr2DateOfBirth(
        FieldInputUtils.createLiteralMapFromString(agentType.getDateOfBirth()));
    agent.setRdaGr2DateOfDeath(
        FieldInputUtils.createLiteralMapFromString(agentType.getDateOfDeath()));
    agent.setRdaGr2PlaceOfBirth(
        FieldInputUtils.createResourceOrLiteralMapFromList(agentType.getPlaceOfBirthList()));
    agent.setRdaGr2PlaceOfDeath(
        FieldInputUtils.createResourceOrLiteralMapFromList(agentType.getPlaceOfDeathList()));
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
