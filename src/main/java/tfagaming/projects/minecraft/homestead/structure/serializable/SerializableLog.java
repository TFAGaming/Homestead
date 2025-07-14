package tfagaming.projects.minecraft.homestead.structure.serializable;

import java.util.UUID;

import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;

public class SerializableLog {
    private UUID logId;
    private String author;
    private String message;
    private boolean read;
    private long sentAt;

    public SerializableLog(String author, String message) {
        this.logId = UUID.randomUUID();
        this.author = author;
        this.message = message;
        this.read = false;
        this.sentAt = System.currentTimeMillis();
    }

    public SerializableLog(UUID logId, String author, String message, long sentAt, boolean read) {
        this.logId = logId;
        this.author = author;
        this.message = message;
        this.read = read;
        this.sentAt = sentAt;
    }

    public UUID getId() {
        return logId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public long getSentAt() {
        return sentAt;
    }

    @Override
    public String toString() {
        return (logId.toString() + "," + author + "," + sentAt + "," + read + "," + message);
    }

    public static SerializableLog fromString(String string) {
        String[] splitted = StringUtils.splitWithLimit(string, ",", 5);

        return new SerializableLog(UUID.fromString(splitted[0]), splitted[1], splitted[4], Long.parseLong(splitted[2]), Boolean.parseBoolean(splitted[3]));
    }
}
