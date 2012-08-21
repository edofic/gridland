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

import org.grid.arena.Arena;

import java.io.Serializable;

/**
 * Compact format for presentation of arena
 * just with tiles. For purposes of remote visualizer
 * don't try to modify fields and use again since this will
 * cause problems with serialization cache
 * <p/>
 * index i = y*width + x
 * <p/>
 * User: andraz
 * Date: 8/21/12
 * Time: 7:11 PM
 */
public class CompactArena implements Serializable {
    //use factory
    private CompactArena() {
    }

    public int width, height;
    public int[] baseTiles;
    public int[] bodyTiles;

    /**
     * create a compact representation for network
     *
     * @param arena to "compress"
     * @return new compact representation
     */
    public static CompactArena fromArena(Arena arena) {
        int width;
        int height;
        int[] baseTiles;
        int[] bodyTiles;

        synchronized (arena) {
            width = arena.getWidth();
            height = arena.getHeight();
            baseTiles = new int[width * height];
            bodyTiles = new int[width * height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int i = y * width + x;
                    baseTiles[i] = arena.getBaseTile(x, y);
                    bodyTiles[i] = arena.getBodyTile(x, y);
                }
            }
        }

        CompactArena ca = new CompactArena();
        ca.width = width;
        ca.height = height;
        ca.baseTiles = baseTiles;
        ca.bodyTiles = bodyTiles;
        return ca;
    }
}
