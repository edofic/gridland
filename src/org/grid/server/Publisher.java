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
import org.grid.visualizer.CompactArena;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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

    public Publisher(int port) {
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
                            clients.add(new Client(sck));
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
                List<Client> toRemove = new ArrayList<Client>();
                synchronized (clients) {
                    for (Client client : clients) {
                        if (!client.transmit(data)) {
                            toRemove.add(client);
                        }
                    }
                }

                for (Client client : toRemove) {
                    client.close();
                }
                clients.removeAll(toRemove);
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
        sendToAll(CompactArena.fromArena(arena));
    }

    private static class Client {
        private ObjectOutputStream output;
        private Socket socket;

        public Client(Socket sck) {
            this.socket = sck;
            try {
                this.output = new ObjectOutputStream(sck.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean transmit(Object data) {
            try {
                output.writeObject(data);
                return true;
            } catch (IOException e) {
                //failure
                return false;
            }
        }

        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


