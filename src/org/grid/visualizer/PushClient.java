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

package org.grid.visualizer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * User: andraz
 * Date: 8/20/12
 * Time: 4:55 PM
 */
public class PushClient {
    private ObjectInputStream input;
    private String id = (Math.random() + "").substring(2, 8) + " ";

    public PushClient(String host, int port) {
        try {
            Socket sck = new Socket(host, port);
            sck.setTcpNoDelay(true);
            this.input = new ObjectInputStream(sck.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        onReceive(input.readObject());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    protected void onReceive(Object data) {
        System.out.println(id + data);
    }
}
