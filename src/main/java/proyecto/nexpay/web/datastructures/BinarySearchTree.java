package proyecto.nexpay.web.datastructures;

import java.util.Optional;

public class BinarySearchTree<T extends Comparable<T>> {

    private BSTNode<T> root;

    public void insert(T value) {
        root = insertRecursive(root, value);
    }

    private BSTNode<T> insertRecursive(BSTNode<T> node, T value) {
        if (node == null) return new BSTNode<>(value);

        int cmp = value.compareTo(node.getData());

        if (cmp < 0) {
            node.setLeft(insertRecursive(node.getLeft(), value));
        } else if (cmp > 0) {
            node.setRight(insertRecursive(node.getRight(), value));
        } else {
            // Si ya existe, lo reemplazamos
            node.setData(value);
        }

        return node;
    }

    public Optional<T> find(String key) {
        return findRecursive(root, key);
    }

    private Optional<T> findRecursive(BSTNode<T> node, String key) {
        if (node == null) return Optional.empty();

        T data = node.getData();
        if (data instanceof proyecto.nexpay.web.model.UserPoints up) {
            int cmp = key.compareTo(up.getUserId());
            if (cmp == 0) return Optional.of(data);
            else if (cmp < 0) return findRecursive(node.getLeft(), key);
            else return findRecursive(node.getRight(), key);
        }

        return Optional.empty();
    }

    public boolean isEmpty() {
        return root == null;
    }

    public void inOrder(BSTNode<T> node, SimpleList<T> list) {
        if (node == null) return;
        inOrder(node.getLeft(), list);
        list.addLast(node.getData());
        inOrder(node.getRight(), list);
    }

    public BSTNode<T> getRoot() {
        return root;
    }

}
