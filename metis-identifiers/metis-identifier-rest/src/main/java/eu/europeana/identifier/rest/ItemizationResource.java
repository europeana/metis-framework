package eu.europeana.identifier.rest;

import eu.europeana.identifier.rest.exceptions.IdentifierException;
import eu.europeana.identifier.service.ItemizationService;
import eu.europeana.identifier.service.exceptions.DeduplicationException;
import eu.europeana.itemization.Request;
import eu.europeana.itemization.RequestResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jibx.runtime.JiBXException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * Itemization REST API
 * Created by ymamakis on 2/9/16.
 */
@Path("/itemize")
@Api("/itemize")
public class ItemizationResource {

    public ItemizationService service = new ItemizationService();

    /**
     * Itemize based on a URL of a file. The file will be downloaded unzipped and itemized
     *
     * @param url The url to download
     * @return A list of itemized EDM records
     * @throws IOException
     * @throws DeduplicationException
     * @throws JiBXException
     */
    @POST
    @Path("/url")
    @ApiOperation(value = "Itemize a remote tar.gz file", response = RequestResult.class)
    public Response itemizeByUrl(@ApiParam("url") @FormParam("url") String url) throws IdentifierException {
        RequestResult res = new RequestResult();
        try {
            res.setItemizedRecords(service.itemize(new URL(url)));
        } catch (IOException | DeduplicationException | JiBXException e) {
            throw new IdentifierException(e.getMessage());
        }
        return Response.ok().entity(res).build();
    }

    /**
     * Itemize a list of Records
     *
     * @param request The list of Records to itemize
     * @return A list of itemized EDM records
     * @throws IOException
     * @throws DeduplicationException
     * @throws JiBXException
     */
    @POST
    @Path("/records")
    @ApiOperation(value = "Itemize a list of records", response = RequestResult.class)
    public Response itemizeRecords(@ApiParam("records") @FormParam("records") Request request) throws IdentifierException {
        RequestResult res = new RequestResult();
        try {
            res.setItemizedRecords(service.itemize(request.getRecords()));
        } catch (DeduplicationException | JiBXException e) {
            throw new IdentifierException(e.getMessage());
        }
        return Response.ok().entity(res).build();
    }

    /**
     * Itemize a tgz file
     *
     * @param zipFile         A tgz file
     * @param fileDisposition The headers of the tgz file
     * @return A list of itemized EDM records
     * @throws IOException
     * @throws DeduplicationException
     * @throws JiBXException
     */
    @POST
    @Path("/file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Itemize a file of records", response = RequestResult.class)
    public Response itemizeFile(@ApiParam("file") @FormDataParam("file") InputStream zipFile,
                                @ApiParam("file") @FormDataParam("file") FormDataContentDisposition fileDisposition) throws IdentifierException {
        try {
            String fileName = "/tmp/" + fileDisposition.getName() + "/" + new Date().getTime();
            File f = new File(fileName + ".tgz");
            FileUtils.copyInputStreamToFile(zipFile, f);
            RequestResult res = new RequestResult();
            res.setItemizedRecords(service.itemize(f));
            return Response.ok().entity(res).build();
        } catch (IOException | DeduplicationException | JiBXException e) {
            throw new IdentifierException(e.getMessage());
        }
    }

}
