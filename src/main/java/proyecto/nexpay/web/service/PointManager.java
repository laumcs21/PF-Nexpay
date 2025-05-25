package proyecto.nexpay.web.service;

import proyecto.nexpay.web.datastructures.*;
import proyecto.nexpay.web.model.UserPoints;

import java.util.Optional;

public class PointManager {

    private BinarySearchTree<UserPoints> pointTree = new BinarySearchTree<>();

    public void addPoints(String userId, int points) {
        UserPoints up = pointTree.find(userId).orElse(new UserPoints(userId, 0));
        up.setPoints(up.getPoints() + points);
        pointTree.insert(up);
    }

    public void removePoints(String userId, int points) {
        pointTree.find(userId).ifPresent(up -> {
            up.setPoints(Math.max(0, up.getPoints() - points));
            pointTree.insert(up); // Reinsertar actualizado
        });
    }

    public int getPoints(String userId) {
        return pointTree.find(userId).map(UserPoints::getPoints).orElse(0);
    }

    public String getRank(String userId) {
        int points = getPoints(userId);
        if (points <= 500) return "Bronce";
        if (points <= 1000) return "Plata";
        if (points <= 5000) return "Oro";
        return "Platino";
    }
}
