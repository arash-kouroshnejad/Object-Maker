package ir.sharif.math.ap2023.hw7;


import java.util.ArrayDeque;
import java.util.Queue;

@SuppressWarnings("all")
public class WaitQueue {
    public final Queue<Node> queue = new ArrayDeque<>(); // after reflection nothing is private anymore

    public void addToQueue(Node node) {
        queue.add(node);
    }

    public Queue getQueue() {return queue;}
}
