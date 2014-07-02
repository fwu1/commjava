/*
 * Copyright 2011 Blue Lotus Software, LLC.
 * Copyright 2011 John Yeary
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: Producer.java 347 2011-05-23 12:22:29Z jyeary $
 */
package queuetest;

import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author John Yeary
 * @version 1.0
 */
public class Producer implements Runnable {

    private static int ctr;
    private final Queue<Message> messageQueue;
    private final Random r;

    public Producer(Queue<Message> messageQueue) {
        this.messageQueue = messageQueue;
        r = new Random();
    }

    @Override
    public void run() {
        while (true) {
            produce();
            int wait = r.nextInt(1000);
            try {
                Thread.sleep(wait);
            } catch (InterruptedException ex) {
                Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void produce() {
        Message m = new Message(++ctr, "Example message.");
        messageQueue.offer(m);
        synchronized (messageQueue) {
            messageQueue.notifyAll();
        }
        System.out.println("Producer: " + m);
    }
}
