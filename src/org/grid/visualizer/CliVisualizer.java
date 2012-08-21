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

/**
 * User: andraz
 * Date: 8/21/12
 * Time: 9:27 PM
 */
public class CliVisualizer {

    public CliVisualizer(String host, int port) {
        new PushClient(host, port) {
            @Override
            protected void onReceive(Object data) {
                if (data instanceof CompactArena) {
                    render((CompactArena) data);
                }
            }
        };
    }

    private synchronized void render(CompactArena data) {
        int width = data.width;
        int height = data.height;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                System.out.print(data.bodyTiles[y * width + x] > 0 ? 'x' : ' ');
            }
            System.out.println();
        }

        System.out.println("\n");
    }

    public static void main(String[] args) {
        final String host = "localhost";
        final int port = 5001;
        new CliVisualizer(host, port);
    }
}
