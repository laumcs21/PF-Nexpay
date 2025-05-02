package proyecto.nexpay.web.datastructures;

import java.util.Iterator;

public class PriorityQueue<T extends Comparable<T>> implements Iterable<T> {

    private Node<T> head;
    private Node<T> tail;
    private int size;
    private boolean shutdown = false;


    public Node<T> getHead() {
        return head;
    }

    public void setHead(Node<T> head) {
        this.head = head;
    }

    public Node<T> getTail() {
        return tail;
    }

    public void setTail(Node<T> tail) {
        this.tail = tail;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public synchronized void enqueue(T data) {
        Node<T> newNode = new Node<>(data);

        if (head == null) {
            head = tail = newNode;
        } else if (data.compareTo(head.getData()) < 0) {
            newNode.setNextNode(head);
            head = newNode;
        } else {
            Node<T> current = head;
            while (current.getNextNode() != null && data.compareTo(current.getNextNode().getData()) >= 0) {
                current = current.getNextNode();
            }

            newNode.setNextNode(current.getNextNode());
            current.setNextNode(newNode);

            if (newNode.getNextNode() == null) {
                tail = newNode;
            }
        }

        size++;
        notifyAll();
    }

    public synchronized T dequeue() {
        while (isEmpty() && !shutdown) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        if (isEmpty())
            return null;

        T data = head.getData();
        head = head.getNextNode();
        if (head == null)
            tail = null;
        size--;
        return data;
    }

    public synchronized boolean isEmpty() {
        return size == 0;
    }

    public synchronized void shutdown() {
        shutdown = true;
        notifyAll();
    }

    public synchronized boolean isShutdown() {
        return shutdown;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = head;

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
}
