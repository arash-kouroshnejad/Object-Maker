package ir.sharif.math.ap2023.hw7;

import java.util.ArrayList;
import java.util.List;

public class Node <T> {
    private final List<Node<T>> children = new ArrayList<>();
    private Node<T> parent = null;
    private final T data;

    public Node(T data) {
        this.data = data;
    }

    public List<Node<T>> getChildren() {
        return children;
    }

    public Node<T> getParent() {
        return parent;
    }

    public void addChild(Node<T> child) {
        child.parent = this;
        this.children.add(child);
    }

    public T getData() {
        return this.data;
    }
}
