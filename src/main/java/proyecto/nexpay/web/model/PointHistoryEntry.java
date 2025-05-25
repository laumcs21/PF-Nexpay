package proyecto.nexpay.web.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PointHistoryEntry implements Serializable, Comparable<PointHistoryEntry> {

    private String userId;
    private String description;
    private int points;
    private LocalDateTime date;

    public PointHistoryEntry(String userId, String description, int points) {
        this.userId = userId;
        this.description = description;
        this.points = points;
        this.date = LocalDateTime.now();
    }

    public String getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public int getPoints() {
        return points;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public int compareTo(PointHistoryEntry other) {
        return this.date.compareTo(other.date);
    }
}
