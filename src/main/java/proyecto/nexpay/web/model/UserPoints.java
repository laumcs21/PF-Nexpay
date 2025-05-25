package proyecto.nexpay.web.model;

public class UserPoints implements Comparable<UserPoints> {
    private String userId;
    private int points;

    public UserPoints(String userId, int points) {
        this.userId = userId;
        this.points = points;
    }

    public String getUserId() {
        return userId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public int compareTo(UserPoints other) {
        return this.userId.compareTo(other.userId);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UserPoints && ((UserPoints) obj).userId.equals(this.userId);
    }
}
