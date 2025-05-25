package proyecto.nexpay.web.datastructures;

import java.io.Serializable;

public class GraphNode<T> implements Serializable {

    private final T value;
    private final SimpleList<T> adjacency;

    public GraphNode(T value) {
        this.value = value;
        this.adjacency = new SimpleList<>();
    }

    public T getValue() {
        return value;
    }

    public SimpleList<T> getAdjacency() {
        return adjacency;
    }
}
