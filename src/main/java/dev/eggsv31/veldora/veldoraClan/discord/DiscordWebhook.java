package dev.eggsv31.veldora.veldoraClan.discord;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DiscordWebhook {
    private final String webhookUrl;
    private String title;
    private String description;
    private String thumbnail;
    private String color;
    private String footerText;
    private String footerIcon;
    private String authorName;
    private String authorUrl;
    private String authorIcon;
    private Map<String, String> fields = new HashMap<>();
    private boolean inline;
    private boolean timestamp;

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setColor(String color) {
        if (!color.startsWith("0x")) {
            color = "0x" + color.replace("#", "");
        }
        this.color = color;
    }

    public void setFooter(String footerText, String footerIcon) {
        this.footerText = footerText;
        this.footerIcon = footerIcon;
    }

    public void setAuthor(String authorName, String authorUrl, String authorIcon) {
        this.authorName = authorName;
        this.authorUrl = authorUrl;
        this.authorIcon = authorIcon;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }

    public void setTimestamp(boolean timestamp) {
        this.timestamp = timestamp;
    }

    public boolean send() {
        try {
            StringBuilder fieldBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                fieldBuilder.append("{\"name\":\"").append(entry.getKey()).append("\",")
                        .append("\"value\":\"").append(entry.getValue()).append("\",")
                        .append("\"inline\":").append(inline).append("},");
            }
            if (!fields.isEmpty()) {
                fieldBuilder.setLength(fieldBuilder.length() - 1);
            }

            StringBuilder payloadBuilder = new StringBuilder("{")
                    .append("\"embeds\":[{");

            if (title != null) {
                payloadBuilder.append("\"title\":\"").append(title).append("\",");
            }

            if (description != null) {
                payloadBuilder.append("\"description\":\"").append(description).append("\",");
            }

            if (color != null) {
                payloadBuilder.append("\"color\":").append(Integer.decode(color)).append(",");
            }

            if (thumbnail != null) {
                payloadBuilder.append("\"thumbnail\":{\"url\":\"").append(thumbnail).append("\"},");
            }

            if (fields != null && !fields.isEmpty()) {
                payloadBuilder.append("\"fields\":[").append(fieldBuilder).append("],");
            }

            if (footerText != null || footerIcon != null) {
                payloadBuilder.append("\"footer\":{\"text\":\"").append(footerText).append("\",")
                        .append("\"icon_url\":\"").append(footerIcon).append("\"},");
            }

            if (authorName != null || authorUrl != null || authorIcon != null) {
                payloadBuilder.append("\"author\":{\"name\":\"").append(authorName).append("\",")
                        .append("\"url\":\"").append(authorUrl).append("\",")
                        .append("\"icon_url\":\"").append(authorIcon).append("\"},");
            }

            if (timestamp) {
                payloadBuilder.append("\"timestamp\":\"")
                        .append(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                                .format(new java.util.Date()))
                        .append("\",");
            }

            if (payloadBuilder.charAt(payloadBuilder.length() - 1) == ',') {
                payloadBuilder.setLength(payloadBuilder.length() - 1);
            }

            payloadBuilder.append("}]}");

            String payload = payloadBuilder.toString();

            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }

            int responseCode = connection.getResponseCode();
            return responseCode == 204;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
