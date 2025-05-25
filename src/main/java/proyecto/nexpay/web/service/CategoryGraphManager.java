package proyecto.nexpay.web.service;

import proyecto.nexpay.web.datastructures.DirectedGraph;
import proyecto.nexpay.web.datastructures.SimpleList;
import proyecto.nexpay.web.persistence.CategoryGraphPersistence;

public class CategoryGraphManager {
    private CategoryGraphPersistence categoryGraphPersistence = new CategoryGraphPersistence();
    private DirectedGraph<String> graph = new DirectedGraph<>();

    public void registerTransition(String fromCategory, String toCategory) {
        graph.addEdge(fromCategory, toCategory);
    }

    public SimpleList<String> getRelatedCategories(String category) {
        return graph.getOutgoing(category);
    }

    public SimpleList<String> getCategoriesLeadingTo(String category) {
        return graph.getIncoming(category);
    }

    public DirectedGraph<String> getGraph() {
        return graph;
    }

    public void saveGraph() {
        categoryGraphPersistence.saveGraph(graph); // o userTransferGraphPersistence
    }

    public void loadGraph() {
        DirectedGraph<String> loaded = categoryGraphPersistence.loadGraph();
        if (loaded != null) {
            this.graph = loaded;
        }
    }
}

