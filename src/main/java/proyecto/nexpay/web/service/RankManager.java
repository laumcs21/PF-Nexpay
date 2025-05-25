package proyecto.nexpay.web.service;

import proyecto.nexpay.web.datastructures.AVLTree;
import proyecto.nexpay.web.model.UserPoints;

public class RankManager {

    private AVLTree<UserPoints> avlTree = new AVLTree<>();

    public void insertOrUpdate(String userId, int points) {
        avlTree.insert(new UserPoints(userId, points));
    }

    public String getRank(int points) {
        if (points <= 500) return "Bronce";
        if (points <= 1000) return "Plata";
        if (points <= 5000) return "Oro";
        return "Platino";
    }

    public String getRank(String userId, int userPoints) {
        return getRank(userPoints);
    }
}
