package proyecto.nexpay.web.datastructures;

public class AVLNode<T extends Comparable<T>> {
    public T data;
    public AVLNode<T> left;
    public AVLNode<T> right;
    public int height;

    public AVLNode(T data) {
        this.data = data;
        this.height = 1;
    }
}
