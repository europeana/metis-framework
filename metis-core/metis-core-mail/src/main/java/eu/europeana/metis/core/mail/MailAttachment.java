package eu.europeana.metis.core.mail;

import java.io.File;

/**
 * File attachment bean
 * Created by ymamakis on 6-1-17.
 */
public class MailAttachment {

    private String name;
    private File attachment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getAttachment() {
        return attachment;
    }

    public void setAttachment(File attachment) {
        this.attachment = attachment;
    }
}
