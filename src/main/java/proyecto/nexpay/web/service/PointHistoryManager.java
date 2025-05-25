package proyecto.nexpay.web.service;

import proyecto.nexpay.web.datastructures.BinarySearchTree;
import proyecto.nexpay.web.datastructures.SimpleList;
import proyecto.nexpay.web.model.PointHistoryEntry;
import proyecto.nexpay.web.persistence.PointHistoryPersistence;

public class PointHistoryManager {

    private final BinarySearchTree<PointHistoryEntry> historyTree = new BinarySearchTree<>();
    private final PointHistoryPersistence persistence = PointHistoryPersistence.getInstance();


    public void register(String userId, String description, int points) {
        PointHistoryEntry entry = new PointHistoryEntry(userId, description, points);
        historyTree.insert(entry);
    }

    public SimpleList<PointHistoryEntry> getHistoryForUser(String userId) {
        SimpleList<PointHistoryEntry> allEntries = new SimpleList<>();
        historyTree.inOrder(historyTree.getRoot(), allEntries); // debes implementar esto
        SimpleList<PointHistoryEntry> userEntries = new SimpleList<>();

        for (int i = 0; i < allEntries.size(); i++) {
            PointHistoryEntry entry = allEntries.get(i);
            if (entry.getUserId().equals(userId)) {
                userEntries.addLast(entry);
            }
        }

        return userEntries;
    }

    public void saveHistory() {
        SimpleList<PointHistoryEntry> list = new SimpleList<>();
        historyTree.inOrder(historyTree.getRoot(), list);
        persistence.saveAllPointHistory(list);
    }

    public void loadHistory() {
        SimpleList<PointHistoryEntry> saved = persistence.loadPointHistory();
        for (int i = 0; i < saved.size(); i++) {
            historyTree.insert(saved.get(i));
        }
    }
}
