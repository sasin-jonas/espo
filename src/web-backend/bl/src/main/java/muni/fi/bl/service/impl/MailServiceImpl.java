package muni.fi.bl.service.impl;

import muni.fi.bl.config.MailConfigProperties;
import muni.fi.bl.service.MailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender emailSender;
    private final MailConfigProperties mailConfigProperties;

    public MailServiceImpl(JavaMailSender emailSender,
                           MailConfigProperties mailConfigProperties) {
        this.emailSender = emailSender;
        this.mailConfigProperties = mailConfigProperties;
    }

    @Override
    public void send(String subject, String body, List<String> recipients) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailConfigProperties.getUsername());
        message.setTo(recipients.toArray(new String[recipients.size()]));
        message.setSubject(subject);
        message.setText(body);
        emailSender.send(message);
    }
}
