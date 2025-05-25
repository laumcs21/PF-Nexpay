package proyecto.nexpay.web.datastructures;

import java.io.Serializable;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Spliterators;
import java.util.Spliterator;

public class SimpleList<T> implements Iterable<T>, Serializable {

    private int size;
    private Node<T> firstNode;
    private Node<T> lastNode;

    public SimpleList() {
        this.size = 0;
        this.firstNode = null;
        this.lastNode = null;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Node<T> getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(Node<T> firstNode) {
        this.firstNode = firstNode;
    }

    public Node<T> getLastNode() {
        return lastNode;
    }

    public void setLastNode(Node<T> lastNode) {
        this.lastNode = lastNode;
    }

    public void addFirst(T data) {
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            firstNode = newNode;
        } else {
            newNode.setNextNode(firstNode);
            firstNode = newNode;
        }
        size++;
    }

    public void addLast(T data) {
        Node<T> newNode = new Node<>(data);
        if (size == 0) {
            firstNode = lastNode = newNode;
        } else {
            lastNode.setNextNode(newNode);
            lastNode = newNode;
        }
        size++;
    }

    public void add(int position, T data) {
        if (position < 0 || position > size) {
            throw new IndexOutOfBoundsException("Invalid position.");
        }
        Node<T> newNode = new Node<>(data);
        if (size == 0 || position == 0) {
            newNode.setNextNode(firstNode);
            firstNode = newNode;
            if (size == 0) {
                lastNode = newNode;
            }
        } else {
            Node<T> temp = firstNode;
            int counter = 0;
            while (counter < position - 1) {
                temp = temp.getNextNode();
                counter++;
            }
            newNode.setNextNode(temp.getNextNode());
            temp.setNextNode(newNode);
            if (newNode.getNextNode() == null) {
                lastNode = newNode;
            }
        }
        size++;
    }

    public T getNodeValue(int position) {
        if (position < 0 || position >= size) {
            throw new IndexOutOfBoundsException("Invalid position.");
        }
        Node<T> temp = firstNode;
        int counter = 0;
        while (counter < position) {
            temp = temp.getNextNode();
            counter++;
        }
        return temp.getData();
    }

    public Node<T> getNode(int position) {
        if (position < 0 || position >= size) {
            throw new IndexOutOfBoundsException("Invalid position.");
        }
        Node<T> temp = firstNode;
        int counter = 0;
        while (counter < position) {
            temp = temp.getNextNode();
            counter++;
        }
        return temp;
    }

    public int getNodePosition(T data) {
        int i = 0;
        for (Node<T> temp = firstNode; temp != null; temp = temp.getNextNode()) {
            if (temp.getData().equals(data)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public boolean isValidIndex(int index) {
        return index >= 0 && index < size;
    }

    public void removeLast() {
        if (firstNode == null) {
            return;
        }

        if (firstNode.getNextNode() == null) {
            firstNode = null;
            lastNode = null;
        } else {
            Node<T> current = firstNode;
            while (current.getNextNode().getNextNode() != null) {
                current = current.getNextNode();
            }
            current.setNextNode(null);
            lastNode = current;
        }

        size--;
    }

    public boolean remove(T data) {
        if (isEmpty()) {
            return false;
        }

        if (firstNode.getData().equals(data)) {
            firstNode = firstNode.getNextNode();
            size--;
            if (firstNode == null) {
                lastNode = null;
            }
            return true;
        }

        Node<T> current = firstNode;
        while (current.getNextNode() != null) {
            if (current.getNextNode().getData().equals(data)) {
                current.setNextNode(current.getNextNode().getNextNode());
                size--;
                if (current.getNextNode() == null) {
                    lastNode = current;
                }
                return true;
            }
            current = current.getNextNode();
        }

        return false;
    }


    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = firstNode;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                T dato = current.getData();
                current = current.getNextNode();
                return dato;
            }
        };
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Invalid index.");
        }
        Node<T> current = firstNode;
        int counter = 0;
        while (counter < index) {
            current = current.getNextNode();
            counter++;
        }
        return current.getData();
    }


    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.ORDERED), false);
    }

    public int size() {
        return size;
    }

    public boolean contains(T value) {
        for (int i = 0; i < size(); i++) {
            if (get(i).equals(value)) {
                return true;
            }
        }
        return false;
    }

}
