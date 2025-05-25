package proyecto.nexpay.web.service;

import proyecto.nexpay.web.datastructures.DirectedGraph;
import proyecto.nexpay.web.datastructures.SimpleList;
import proyecto.nexpay.web.persistence.UserTransferGraphPersistence;

public class UserTransferGraphManager {
    private UserTransferGraphPersistence userTransferGraphPersistence = new UserTransferGraphPersistence();
    private DirectedGraph<String> graph = new DirectedGraph<>();

    public void registerTransfer(String fromUser, String toUser) {
        graph.addEdge(fromUser, toUser);
    }

    public SimpleList<String> getFrequentTargets(String userId) {
        return graph.getOutgoing(userId);
    }

    public SimpleList<String> getSendersTo(String userId) {
        return graph.getIncoming(userId);
    }

    public DirectedGraph<String> getGraph() {
        return graph;
    }

    public void saveGraph() {
        userTransferGraphPersistence.saveGraph(graph); // o userTransferGraphPersistence
    }

    public void loadGraph() {
        DirectedGraph<String> loaded = userTransferGraphPersistence.loadGraph();
        if (loaded != null) {
            this.graph = loaded;
        }
    }
}

