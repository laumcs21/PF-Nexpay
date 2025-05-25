package proyecto.nexpay.web.datastructures;

import java.io.Serializable;

public class Node<T> implements Serializable {
    private T data;
    private Node<T> nextNode;

    public Node(T data) {
        this.data = data;
    }

    public Node(T data, Node<T> nextNode) {
        this.data = data;
        this.nextNode = nextNode;
    }

    public T getData() {
        return data;
    }

    public Node<T> getNextNode() {
        return nextNode;
    }

    public void setNextNode(Node<T> nextNode) {
        this.nextNode = nextNode;
    }

    public void setData(T data) {
        this.data = data;
    }
}

