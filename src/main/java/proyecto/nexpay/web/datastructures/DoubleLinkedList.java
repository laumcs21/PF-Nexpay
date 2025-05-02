package proyecto.nexpay.web.datastructures;

import java.util.Iterator;

public class DoubleLinkedList<T> implements Iterable<T>{
    private int size;
    private DoubleNode<T> firstNode;
    private DoubleNode<T> lastNode;

    public DoubleLinkedList() {
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

    public DoubleNode<T> getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(DoubleNode<T> firstNode) {
        this.firstNode = firstNode;
    }

    public DoubleNode<T> getLastNode() {
        return lastNode;
    }

    public void setLastNode(DoubleNode<T> lastNode) {
        this.lastNode = lastNode;
    }

    public void addFirst(T nodeValue) {
        DoubleNode<T> newNode = new DoubleNode<>(nodeValue);

        if (isEmpty()) {
            firstNode = lastNode = newNode;
        } else {
            newNode.setNextNode(firstNode);
            firstNode = newNode;
        }
        size++;
    }

    public void addLast(T nodeValue) {
        DoubleNode<T> newNode = new DoubleNode<>(nodeValue);

        if (isEmpty()) {
            firstNode = lastNode = newNode;
        } else {
            lastNode.setNextNode(newNode);
            newNode.setPreviousNode(lastNode);
            lastNode = newNode;
        }
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private DoubleNode<T> current = firstNode;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                T data = current.getData();
                current = current.getNextNode();
                return data;
            }
        };
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }

        DoubleNode<T> current = firstNode;
        for (int i = 0; i < index; i++) {
            current = current.getNextNode();
        }
        return current.getData();
    }

    public void remove(T value) {
        if (isEmpty()) return;

        DoubleNode<T> current = firstNode;

        while (current != null) {
            if (current.getData().equals(value)) {
                if (current == firstNode) {
                    firstNode = current.getNextNode();
                    if (firstNode != null) {
                        firstNode.setPreviousNode(null);
                    } else {
                        lastNode = null;
                    }
                } else if (current == lastNode) {
                    lastNode = current.getPreviousNode();
                    lastNode.setNextNode(null);
                } else {
                    current.getPreviousNode().setNextNode(current.getNextNode());
                    current.getNextNode().setPreviousNode(current.getPreviousNode());
                }

                size--;
                return;
            }
            current = current.getNextNode();
        }
    }

}

