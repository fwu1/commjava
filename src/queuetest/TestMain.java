package queuetest;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author John Yeary
 * @version 1.0
 */
public class TestMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Queue<Message> queue = new ConcurrentLinkedQueue<Message>();

        Producer p = new Producer(queue);
        Consumer c = new Consumer(queue);

        Thread t1 = new Thread(p);
        Thread t2 = new Thread(c);


        t1.start();
        t2.start();
    }
}