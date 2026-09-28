package murach.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import javax.mail.MessagingException;

public class MailUtilBrevo {

    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static String getFromAddress() {
        return env("MAIL_FROM", "");
    }

    public static void sendMail(String to, String from,
            String subject, String body, boolean bodyIsHTML)
            throws MessagingException {

        String apiKey = env("BREVO_API_KEY", "");
        String senderName = env("MAIL_FROM_NAME", "JPA SendMail");

        if (apiKey.isEmpty()) {
            throw new MessagingException("Missing BREVO_API_KEY environment variable.");
        }
        if (from == null || from.trim().isEmpty()) {
            throw new MessagingException("Missing MAIL_FROM environment variable.");
        }

        String contentField = bodyIsHTML ? "htmlContent" : "textContent";
        String payload = "{"
                + "\"sender\":{\"name\":" + jsonString(senderName) + ",\"email\":" + jsonString(from) + "},"
                + "\"to\":[{\"email\":" + jsonString(to) + "}],"
                + "\"subject\":" + jsonString(subject) + ","
                + "\"" + contentField + "\":" + jsonString(body)
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new MessagingException("Brevo API returned HTTP " + statusCode + ": " + response.body());
            }
        } catch (IOException e) {
            throw new MessagingException("Unable to call Brevo API.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MessagingException("Interrupted while calling Brevo API.", e);
        }
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static String jsonString(String value) {
        if (value == null) {
            return "\"\"";
        }

        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                    break;
            }
        }
        escaped.append('"');
        return escaped.toString();
    }
}
