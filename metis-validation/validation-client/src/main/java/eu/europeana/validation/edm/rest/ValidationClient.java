package eu.europeana.validation.edm.rest;

import eu.europeana.validation.edm.model.Schema;
import eu.europeana.validation.edm.model.ValidationResult;
import eu.europeana.validation.edm.model.ValidationResultList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.FormParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;


/**
 * Created by ymamakis on 12/24/15.
 */
public class ValidationClient {
    private Client client =  ClientBuilder.newBuilder().build();
    private Config config = new Config();
    public ValidationResult validateSingle(String targetSchema, String xmlPath) throws Exception{
    System.out.println(config.getValidationPath());
        WebTarget target  = client.target(config.getValidationPath()).path(targetSchema);

        Form form =new Form();
        form.param("record", FileUtils.readFileToString(new File(xmlPath)));
        return target.request().post(Entity.form(form)).readEntity(ValidationResult.class);
    }

    public ValidationResultList validateBatch(String targetSchema, String zipFilePath) throws Exception{
        Client client =  ClientBuilder.newBuilder().register(MultiPartFeature.class).register(ValidationResultList.class).build();
        WebTarget target  = client.target(config.getValidationPath()).path("batch/"+targetSchema);
        FormDataMultiPart part = new FormDataMultiPart();

        part.field("file", new FileInputStream(zipFilePath), MediaType.TEXT_PLAIN_TYPE);

        return target.request().post(Entity.entity(part, MediaType.MULTIPART_FORM_DATA_TYPE)).readEntity(ValidationResultList.class);
    }

    public void createSchema(String name, String path, String schematronPath, String version,  File zipFile) throws Exception{
        WebTarget target  = client.target(config.getValidationPath()).path("/schema/"+name).queryParam("schemaPath",path).queryParam("schematronPath",schematronPath);
        FormDataMultiPart form = new FormDataMultiPart();
        form.field("file",new FileInputStream(zipFile),MediaType.MULTIPART_FORM_DATA_TYPE);
        Response response = target.request().post(Entity.entity(form, MediaType.MULTIPART_FORM_DATA_TYPE));
        if(response.getStatus()==200){
            return;
        } else {
            throw new Exception();
        }
    }

    public void deleteSchema(String name, String version){
        WebTarget target  = client.target(config.getValidationPath()).path("/schema/"+name);
        Response response = target.request().delete();
        if(response.getStatus()==200){
            return;
        }
    }

    public void updateSchema(String name, String path, String schematronPath, String version, File zipFile) throws Exception{
        WebTarget target  = client.target(config.getValidationPath()).path("/schema/"+name).queryParam("schemaPath",path).queryParam("schematronPath",schematronPath);
        FormDataMultiPart form = new FormDataMultiPart();
        form.field("file",new FileInputStream(zipFile),MediaType.MULTIPART_FORM_DATA_TYPE);
        Response response = target.request().put(Entity.entity(form, MediaType.MULTIPART_FORM_DATA_TYPE));
        if(response.getStatus()==200){
            return;
        } else {
            throw new Exception();
        }
    }

    public Schema getSchema(String name, String version){
        WebTarget target  = client.target(config.getValidationPath()).path("/schema/"+name);
        Response response = target.request().get();
        if(response.getStatus()==200){
            return response.readEntity(Schema.class);
        }
        return null;
    }

    public List<Schema> getAllSchemas(){
        WebTarget target  = client.target(config.getValidationPath()).path("/schemas");
        Response response = target.request().get();
        if(response.getStatus()==200){
            return response.readEntity(List.class);
        }
        return null;
    }

    public byte[] getZip(String name, String version) throws IOException {
        WebTarget target  = client.target(config.getValidationPath()).path("/schema/download/"+name);
        Response response = target.request().get();
        byte[] b = new byte[response.getLength()];
        IOUtils.readFully(response.readEntity(InputStream.class),b);
        if(response.getStatus()==200){
            return b;
        }
        return null;
    }

}
