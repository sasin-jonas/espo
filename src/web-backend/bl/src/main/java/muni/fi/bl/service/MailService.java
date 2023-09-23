package muni.fi.bl.service;

import java.util.List;

public interface MailService {

    void send(String subject, String message, List<String> recipients);
}
