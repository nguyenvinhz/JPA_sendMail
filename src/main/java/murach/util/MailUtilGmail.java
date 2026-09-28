package murach.util;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class MailUtilGmail {

    public static String getFromAddress() {
        String mailFrom = env("MAIL_FROM", null);
        if (mailFrom != null) {
            return mailFrom;
        }
        return env("MAIL_USER", "no-reply@example.com");
    }

    public static void sendMail(String to, String from,
            String subject, String body, boolean bodyIsHTML)
            throws MessagingException {

        // 1 - get a mail session
        String host = env("MAIL_HOST", "smtp.gmail.com");
        int port = Integer.parseInt(env("MAIL_PORT", "587"));
        String username = env("MAIL_USER", null);
        String password = env("MAIL_PASSWORD", null);

        if (username == null || password == null) {
            throw new MessagingException("Missing MAIL_USER or MAIL_PASSWORD environment variable.");
        }

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        Session session = Session.getDefaultInstance(props);
        session.setDebug(Boolean.parseBoolean(env("MAIL_DEBUG", "false")));

        // 2 - create a message
        Message message = new MimeMessage(session);
        message.setSubject(subject);
        if (bodyIsHTML) {
            message.setContent(body, "text/html");
        } else {
            message.setText(body);
        }

        // 3 - address the message
        Address fromAddress = new InternetAddress(from);
        Address toAddress = new InternetAddress(to);
        message.setFrom(fromAddress);
        message.setRecipient(Message.RecipientType.TO, toAddress);

        // 4 - send the message
        Transport transport = session.getTransport("smtp");
        transport.connect(host, port, username, password);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
