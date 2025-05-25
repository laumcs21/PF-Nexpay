package proyecto.nexpay.web.datastructures;

import java.io.Serializable;

public class DirectedGraph<T> implements Serializable {

    private final SimpleList<GraphNode<T>> nodes = new SimpleList<>();

    public void addNode(T value) {
        if (findNode(value) == null) {
            nodes.addLast(new GraphNode<>(value));
        }
    }

    public void addEdge(T from, T to) {
        GraphNode<T> fromNode = findNode(from);
        GraphNode<T> toNode = findNode(to);

        if (fromNode == null) {
            fromNode = new GraphNode<>(from);
            nodes.addLast(fromNode);
        }

        if (toNode == null) {
            toNode = new GraphNode<>(to);
            nodes.addLast(toNode);
        }

        if (!fromNode.getAdjacency().contains(to)) {
            fromNode.getAdjacency().addLast(to);
        }
    }

    public SimpleList<T> getOutgoing(T value) {
        GraphNode<T> node = findNode(value);
        if (node != null) {
            return node.getAdjacency();
        }
        return new SimpleList<>();
    }

    public SimpleList<T> getIncoming(T value) {
        SimpleList<T> incoming = new SimpleList<>();
        for (int i = 0; i < nodes.size(); i++) {
            GraphNode<T> current = nodes.get(i);
            SimpleList<T> adj = current.getAdjacency();
            for (int j = 0; j < adj.size(); j++) {
                if (adj.get(j).equals(value)) {
                    incoming.addLast(current.getValue());
                }
            }
        }
        return incoming;
    }

    private GraphNode<T> findNode(T value) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getValue().equals(value)) {
                return nodes.get(i);
            }
        }
        return null;
    }

    public SimpleList<GraphNode<T>> getAllNodes() {
        return nodes;
    }

    public boolean contains(T value) {
        return findNode(value) != null;
    }

    public int size() {
        return nodes.size();
    }
}
