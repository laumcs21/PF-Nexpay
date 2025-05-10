package proyecto.nexpay.web.datastructures;

public class CircularLinkedList<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    public CircularLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (size == 0) {
            head = tail = newNode;
            head.next = tail;
            tail.next = head;
        } else {
            tail.next = newNode;
            tail = newNode;
            tail.next = head;
        }
        size++;
    }

    public T remove() {
        if (size == 0) {
            return null;
        }
        T data = head.data;
        if (size == 1) {
            head = tail = null;
        } else {
            head = head.next;
            tail.next = head;
        }
        size--;
        return data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public T peek() {
        return head != null ? head.data : null;
    }

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }
}

