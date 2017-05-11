package eu.europeana.metis.core.mail;

import eu.europeana.metis.ui.mongo.domain.UserDTO;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * Mail Service for Metis
 * Created by ymamakis on 6-1-17.
 */
@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VelocityEngine engine;

    private String from;

    public MailService(String from){
        this.from= from;
    }

    /**
     * Send an email on behalf of Metis
     * @param vmTemplate Specify the template to be used according to the appropriate operation
     * @param user The user the email should be sent to
     * @param attachments Any mail attachments to be sent (can be null)
     */
    public void send(final String vmTemplate, final UserDTO user, final MailAttachment... attachments){
        final MimeMessagePreparator preparator = new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                message.setTo(user.getUser().getEmail());
                message.setFrom(from);
                Map<String,Object> model = new HashMap<>();
                model.put("user", user);
                String text = VelocityEngineUtils.mergeTemplateIntoString(
                        engine, vmTemplate, "UTF-8", model);
                message.setText(text, true);
                if(attachments!=null && attachments.length>0){
                    for (MailAttachment attachment:attachments){
                        message.addAttachment(attachment.getName(),attachment.getAttachment());
                    }
                }
            }
        };
        mailSender.send(preparator);
    }
}
