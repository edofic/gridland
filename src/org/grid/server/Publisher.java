/*
 *  AgentField - a simple capture-the-flag simulation for distributed intelligence
 *  Copyright (C) 2012 Andraz Bajt
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.grid.server;

import org.grid.arena.Arena;
import org.grid.visualizer.CompactTiles;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple web server to publish arena state
 * to remote visualizers
 * User: andraz
 * Date: 8/20/12
 * Time: 3:30 PM
 */
public class Publisher {
    private ServerSocket socket;
    private final List<Client> clients;
    private final ExecutorService executor;

    public Publisher(int port, final int interval, final int bufferSize) {
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.clients = new ArrayList<Client>();
        executor = Executors.newSingleThreadExecutor();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket sck = socket.accept();
                        sck.setTcpNoDelay(true);
                        synchronized (clients) {
                            clients.add(new Client(sck, interval, bufferSize));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void sendToAll(final Object data) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                synchronized (clients) {
                    final Iterator<Client> iterator = clients.iterator();
                    while (iterator.hasNext()) {
                        Client client = iterator.next();
                        if (!client.transmit(data)) {
                            iterator.remove();
                        }
                    }
                }

            }
        });
    }

    /**
     * Publishes arena to all clients
     * creates a stripped down version in process
     *
     * @param arena to publish
     */
    public void publishArena(Arena arena) {
        sendToAll(CompactTiles.fromArena(arena));
    }

    private static class Client {
        private ObjectOutputStream output;
        private final ConcurrentLinkedQueue<Object> queue = new ConcurrentLinkedQueue<Object>();
        private boolean alive = true;
        private final int bufferSize;

        public Client(Socket sck, final int interval, int bufferSize) {
            try {
                this.output = new ObjectOutputStream(sck.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.bufferSize = bufferSize;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Object data = queue.poll();
                        if (data != null) {
                            try {
                                output.writeObject(data);
                            } catch (IOException e) {
                                alive = false;
                                break;
                            }
                        } else {
                            try {
                                Thread.sleep(interval);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            }).start();
        }

        public boolean transmit(Object data) {
            if (queue.size() < bufferSize) {
                queue.add(data);
            }

            return alive;
        }
    }
}


