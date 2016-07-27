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
package eu.europeana.enrichment.rest;

import eu.europeana.enrichment.service.Enricher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

/**
 * Unit tests for enrichment REST API
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
@SuppressWarnings("unchecked")
@Ignore
public class EnrichmentResourceTest {

	private Enricher enricher;


	/**
	 * Initialization of the Unique Identifier service mockup
	 */
	@Before
	public void mockUp() {
		ApplicationContext applicationContext = ApplicationContextUtils
				.getApplicationContext();
		enricher = applicationContext.getBean(Enricher.class);
		Mockito.reset(enricher);
	}

	/**
	 * Enrichment test
	 */
	@Test
	public void testEnrich() {
//		try {
//
//			doNothing().when(enricher).init("Europeana");
//
//			List<EntityWrapper> wrapResp = new ArrayList<EntityWrapper>();
//			AgentImpl agent = new AgentImpl();
//			agent.setAbout("agent");
//			ObjectMapper objIdMapper = new ObjectMapper();
//
//			SimpleModule sm = new SimpleModule("objId",
//					Version.unknownVersion());
//			sm.addSerializer(new ObjectIdSerializer());
//			objIdMapper.registerModule(sm);
//
//			EntityWrapper wrapper = new EntityWrapper("test", "test","test","test",
//					objIdMapper.writeValueAsString(agent));
//			wrapResp.add(wrapper);
//			InputValueList vals = new InputValueList();
//
//			List<InputValue> valList = new ArrayList<InputValue>();
//			InputValue val = new InputValue();
//			val.setOriginalField("test");
//			val.setValue("test");
//
//			List<EntityClass> clazz = new ArrayList<EntityClass>();
//			clazz.add(EntityClass.AGENT);
//			val.setVocabularies(clazz);
//			valList.add(val);
//			vals.setInputValueList(valList);
//			when(enricher.tagExternal(Mockito.anyList())).thenReturn(wrapResp);
//
//			Form form = new Form();
//			form.param("input", new ObjectMapper().writeValueAsString(vals));
//			form.param("toXml", Boolean.toString(false));
//			Response response = target("/enrich").request(
//					MediaType.APPLICATION_JSON).post(
//					Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED),
//					Response.class);
//
//			EntityWrapperList wrapList = new ObjectMapper().readValue(
//					response.readEntity(String.class), EntityWrapperList.class);
//			List<EntityWrapper> wrapResponse = wrapList.getWrapperList();
//			Assert.assertEquals(1, wrapResponse.size());
//			EntityWrapper entity = wrapResponse.get(0);
//			Assert.assertEquals(entity.getOriginalField(),
//					wrapper.getOriginalField());
//			Assert.assertEquals(entity.getClassName(), wrapper.getClassName());
//			Assert.assertEquals(entity.getContextualEntity(),
//					wrapper.getContextualEntity());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * JsonGeneration Exception test
	 */
	@Test
	public void testEnrichJsonException() {
//		try {
//
//			doNothing().when(enricher).init("Europeana");
//
//			List<EntityWrapper> wrapResp = new ArrayList<EntityWrapper>();
//			AgentImpl agent = new AgentImpl();
//			agent.setAbout("agent");
//			ObjectMapper objIdMapper = new ObjectMapper();
//
//			SimpleModule sm = new SimpleModule("objId",
//					Version.unknownVersion());
//			sm.addSerializer(new ObjectIdSerializer());
//			objIdMapper.registerModule(sm);
//
//			EntityWrapper wrapper = new EntityWrapper("test", "test","test","test",
//					objIdMapper.writeValueAsString(agent));
//			wrapResp.add(wrapper);
//			InputValueList vals = new InputValueList();
//
//			List<InputValue> valList = new ArrayList<InputValue>();
//			InputValue val = new InputValue();
//			val.setOriginalField("test");
//			val.setValue("test");
//
//			List<EntityClass> clazz = new ArrayList<EntityClass>();
//			clazz.add(EntityClass.AGENT);
//			val.setVocabularies(clazz);
//			valList.add(val);
//			vals.setInputValueList(valList);
//			when(enricher.tagExternal(Mockito.anyList())).thenThrow(
//					new JsonGenerationException("testJGE"));
//
//			Form form = new Form();
//			form.param("input", new ObjectMapper().writeValueAsString(vals));
//			form.param("toXml", Boolean.toString(false));
//			Response response = target("/enrich").request(
//					MediaType.APPLICATION_JSON).post(
//					Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED),
//					Response.class);
//			EnrichmentError error = response
//					.readEntity(EnrichmentError.class);
//			Assert.assertEquals("JsonGenerationException: testJGE", error.getDetails());
//			Assert.assertTrue(error.getCause().equalsIgnoreCase(JsonGenerationException.class.getName()));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * JsonMapping Exception test
	 */
	@Test
	public void testEnrichJsonMappingException() {
//		try {
//
//			doNothing().when(enricher).init("Europeana");
//
//			List<EntityWrapper> wrapResp = new ArrayList<EntityWrapper>();
//			AgentImpl agent = new AgentImpl();
//			agent.setAbout("agent");
//			ObjectMapper objIdMapper = new ObjectMapper();
//
//			SimpleModule sm = new SimpleModule("objId",
//					Version.unknownVersion());
//			sm.addSerializer(new ObjectIdSerializer());
//			objIdMapper.registerModule(sm);
//
//			EntityWrapper wrapper = new EntityWrapper("test", "test","test","test",
//					objIdMapper.writeValueAsString(agent));
//			wrapResp.add(wrapper);
//			InputValueList vals = new InputValueList();
//
//			List<InputValue> valList = new ArrayList<InputValue>();
//			InputValue val = new InputValue();
//			val.setOriginalField("test");
//			val.setValue("test");
//
//			List<EntityClass> clazz = new ArrayList<EntityClass>();
//			clazz.add(EntityClass.AGENT);
//			val.setVocabularies(clazz);
//			valList.add(val);
//			vals.setInputValueList(valList);
//			when(enricher.tagExternal(Mockito.anyList())).thenThrow(
//					new JsonMappingException("testJME"));
//
//			Form form = new Form();
//			form.param("input", new ObjectMapper().writeValueAsString(vals));
//			form.param("toXml", Boolean.toString(false));
//			Response response = target("/enrich").request(
//					MediaType.APPLICATION_JSON).post(
//					Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED),
//					Response.class);
//			EnrichmentError error = response
//					.readEntity(EnrichmentError.class);
//			Assert.assertEquals("JsonMappingException: testJME", error.getDetails());
//			Assert.assertTrue(error.getCause().equalsIgnoreCase(JsonMappingException.class.getName()));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * IOException test
	 */
	@Test
	public void testEnrichIOException() {
//		try {
//
//			doNothing().when(enricher).init("Europeana");
//
//			List<EntityWrapper> wrapResp = new ArrayList<EntityWrapper>();
//			AgentImpl agent = new AgentImpl();
//			agent.setAbout("agent");
//			ObjectMapper objIdMapper = new ObjectMapper();
//
//			SimpleModule sm = new SimpleModule("objId",
//					Version.unknownVersion());
//			sm.addSerializer(new ObjectIdSerializer());
//			objIdMapper.registerModule(sm);
//
//			EntityWrapper wrapper = new EntityWrapper("test", "test", "test","test",
//					objIdMapper.writeValueAsString(agent));
//			wrapResp.add(wrapper);
//			InputValueList vals = new InputValueList();
//
//			List<InputValue> valList = new ArrayList<InputValue>();
//			InputValue val = new InputValue();
//			val.setOriginalField("test");
//			val.setValue("test");
//
//			List<EntityClass> clazz = new ArrayList<EntityClass>();
//			clazz.add(EntityClass.AGENT);
//			val.setVocabularies(clazz);
//			valList.add(val);
//			vals.setInputValueList(valList);
//			when(enricher.tagExternal(Mockito.anyList())).thenThrow(
//					new IOException("testIOE"));
//
//			Form form = new Form();
//			form.param("input", new ObjectMapper().writeValueAsString(vals));
//			form.param("toXml", Boolean.toString(false));
//			Response response = target("/enrich").request(
//					MediaType.APPLICATION_JSON).post(
//					Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED),
//					Response.class);
//			EnrichmentError error = response
//					.readEntity(EnrichmentError.class);
//			Assert.assertEquals("IOException: testIOE",
//					error.getDetails());
//			Assert.assertTrue(error.getCause().equalsIgnoreCase(IOException.class.getName()));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * Other exception test
	 */
	@Test
	public void testEnrichUnknownException() {
//		try {
//
//			doNothing().when(enricher).init("Europeana");
//
//			List<EntityWrapper> wrapResp = new ArrayList<EntityWrapper>();
//			AgentImpl agent = new AgentImpl();
//			agent.setAbout("agent");
//			ObjectMapper objIdMapper = new ObjectMapper();
//
//			SimpleModule sm = new SimpleModule("objId",
//					Version.unknownVersion());
//			sm.addSerializer(new ObjectIdSerializer());
//			objIdMapper.registerModule(sm);
//
//			EntityWrapper wrapper = new EntityWrapper("test", "test", "test","test",
//					objIdMapper.writeValueAsString(agent));
//			wrapResp.add(wrapper);
//			InputValueList vals = new InputValueList();
//
//			List<InputValue> valList = new ArrayList<InputValue>();
//			InputValue val = new InputValue();
//			val.setOriginalField("test");
//			val.setValue("test");
//
//			List<EntityClass> clazz = new ArrayList<EntityClass>();
//			clazz.add(EntityClass.AGENT);
//			val.setVocabularies(clazz);
//			valList.add(val);
//			vals.setInputValueList(valList);
//			when(enricher.tagExternal(Mockito.anyList())).thenThrow(
//					new UnknownException("testUnknown"));
//
//			Form form = new Form();
//			form.param("input", new ObjectMapper().writeValueAsString(vals));
//			form.param("toXml", Boolean.toString(false));
//			Response response = target("/enrich").request(
//					MediaType.APPLICATION_JSON).post(
//					Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED),
//					Response.class);
//			EnrichmentError error = response
//					.readEntity(EnrichmentError.class);
//			Assert.assertEquals("UnknownException: testUnknown",
//					error.getDetails());
//			Assert.assertTrue(error.getCause().equalsIgnoreCase(UnknownException.class.getName()));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}
