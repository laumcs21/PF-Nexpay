package proyecto.nexpay.web.datastructures;

public class DoubleNode<T> {
    private T data;
    private DoubleNode<T> nextNode;
    private DoubleNode<T> previousNode;

    public DoubleNode(T data) {
        this.data = data;
    }

    public DoubleNode(T data, DoubleNode<T> nextNode) {
        this.data = data;
        this.nextNode = nextNode;
    }

    public DoubleNode(T data, DoubleNode<T> nextNode, DoubleNode<T> previousNode) {
        this.data = data;
        this.nextNode = nextNode;
        this.previousNode = previousNode;
    }

    public T getData() {
        return data;
    }

    public DoubleNode<T> getNextNode() {
        return nextNode;
    }

    public void setNextNode(DoubleNode<T> nextNode) {
        this.nextNode = nextNode;
    }

    public void setData(T data) {
        this.data = data;
    }

    public DoubleNode<T> getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(DoubleNode<T> previousNode) {
        this.previousNode = previousNode;
    }
}

