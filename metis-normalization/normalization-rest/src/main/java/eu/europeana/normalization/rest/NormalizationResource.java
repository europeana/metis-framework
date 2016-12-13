package eu.europeana.normalization.rest;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.w3c.dom.Document;

import eu.europeana.normalization.NormalizationService;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.model.NormalizedBatchResult;
import eu.europeana.normalization.model.NormalizedRecordResult;
import eu.europeana.normalization.util.XmlUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


@Path("/normalization")
@Api(value = "EDM Record Normalization API for Metis")
@Produces({MediaType.APPLICATION_JSON +"; charset=UTF-8" })
public class NormalizationResource {
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NormalizationResource.class);
	
	  @Context
	  UriInfo uriInfo;
	  @Context
	  Request request;
	  @Context
	  ServletContext servletContext;

	  @POST
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON +"; charset=UTF-8")
	  @Path("normalizeEdmInternal")
	  @ApiOperation(value = "Normalize records in EDM Internal",
	    notes = "Applies a preset list of data cleaning and normalization operations, to the submited records.",
	    response = NormalizedBatchResult.class)
	  public NormalizedBatchResult normalizeEdmInternal(@ApiParam(value="List of EDM records in Strings containing XML", required=true)List<String> records) throws Exception {
		  try {
			  List<NormalizedRecordResult> result=new ArrayList<>();
				for(String edmRec: records) {
					try {
						result.add(processNormalize(edmRec));
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						result.add(new NormalizedRecordResult(e.getMessage(), edmRec));
					}
				}
				return new NormalizedBatchResult(result);
		  } catch (Exception e) {
			  log.error(e.getMessage(), e);
			  throw new InternalServerErrorException(e);
		  }
	  }
		private NormalizedRecordResult processNormalize(String record) {
			try {
				if (record == null) 
					return new NormalizedRecordResult("Missing required parameter 'record'", record);
				Document recordDom = null;
				try {
					recordDom = XmlUtil.parseDom(new StringReader(record)); 
				} catch (Exception e) {
					return new NormalizedRecordResult("Error parsing XML in parameter 'record': " + e.getMessage(), record);
				}
				NormalizationReport report = ((NormalizationService)servletContext.getAttribute("NormalizationService")).normalize(recordDom);
				String writeDomToString = XmlUtil.writeDomToString(recordDom);
				NormalizedRecordResult result=new NormalizedRecordResult(writeDomToString, report);
				return result;
			} catch (Throwable e) {
				log.info(e.getMessage(), e);
				return new NormalizedRecordResult("Unexpected error: " + e.getMessage(), record );
			}
		}

	  
}

