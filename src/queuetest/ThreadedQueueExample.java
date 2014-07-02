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
 * $Id: ThreadedQueueExample.java 347 2011-05-23 12:22:29Z jyeary $
 */
package queuetest;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author John Yeary
 * @version 1.0
 */
public class ThreadedQueueExample {

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
