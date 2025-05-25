package proyecto.nexpay.web.datastructures;

public class AVLTree<T extends Comparable<T>> {
    private AVLNode<T> root;

    public void insert(T data) {
        root = insert(root, data);
    }

    public T find(T data) {
        return find(root, data);
    }

    private T find(AVLNode<T> node, T data) {
        if (node == null) return null;
        int cmp = data.compareTo(node.data);
        if (cmp == 0) return node.data;
        else if (cmp < 0) return find(node.left, data);
        else return find(node.right, data);
    }

    private AVLNode<T> insert(AVLNode<T> node, T data) {
        if (node == null) return new AVLNode<>(data);

        int cmp = data.compareTo(node.data);
        if (cmp < 0) node.left = insert(node.left, data);
        else if (cmp > 0) node.right = insert(node.right, data);
        else node.data = data; // reemplaza si ya existe

        node.height = 1 + Math.max(height(node.left), height(node.right));

        int balance = getBalance(node);

        // Rotaciones
        if (balance > 1 && data.compareTo(node.left.data) < 0)
            return rotateRight(node);

        if (balance < -1 && data.compareTo(node.right.data) > 0)
            return rotateLeft(node);

        if (balance > 1 && data.compareTo(node.left.data) > 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        if (balance < -1 && data.compareTo(node.right.data) < 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    private AVLNode<T> rotateRight(AVLNode<T> y) {
        AVLNode<T> x = y.left;
        AVLNode<T> T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = 1 + Math.max(height(y.left), height(y.right));
        x.height = 1 + Math.max(height(x.left), height(x.right));

        return x;
    }

    private AVLNode<T> rotateLeft(AVLNode<T> x) {
        AVLNode<T> y = x.right;
        AVLNode<T> T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = 1 + Math.max(height(x.left), height(x.right));
        y.height = 1 + Math.max(height(y.left), height(y.right));

        return y;
    }

    private int height(AVLNode<T> node) {
        return node == null ? 0 : node.height;
    }

    private int getBalance(AVLNode<T> node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }
}
