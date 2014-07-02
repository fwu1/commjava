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
 * $Id: Consumer.java 349 2011-05-23 15:25:32Z jyeary $
 */
package queuetest;

import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author John Yeary
 * @version 1.0
 */
public class Consumer implements Runnable {

    private final Queue<Message> queue;

    public Consumer(Queue<Message> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
    	boolean exit=false;
        while (!exit) {
        	exit=consume();
            try {
                synchronized (queue) {
                    queue.wait();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Consumer exited");
    }

    private boolean consume() {
        while (!queue.isEmpty()) {
            Message m = queue.poll();
            if (m != null) {
                System.out.printf("Consumer %d: %s\n",m.id, m.toString());
                if(m.id>1)
                	return true;
            }
        }
        return false;
    }
}
