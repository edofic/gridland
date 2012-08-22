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

import java.awt.*;
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
public class CompactTiles implements Serializable {
    //use factory
    private CompactTiles() {
    }

    public int width, height;
    public int[] tiles;
    public int[] bodyColor;
    public float[] bodyXoffset;
    public float[] bodyYoffset;

    /**
     * create a compact representation for network
     *
     * @param arena to "compress"
     * @return new compact representation
     */
    public static CompactTiles fromArena(Arena arena) {
        int width;
        int height;
        int[] tiles;
        int[] bodyColor;
        float[] bodyXoffset;
        float[] bodyYoffset;

        synchronized (arena) {
            width = arena.getWidth();
            height = arena.getHeight();
            tiles = new int[width * height];
            bodyColor = new int[width * height];
            bodyXoffset = new float[width * height];
            bodyYoffset = new float[width * height];

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int i = y * width + x;
                    int base = arena.getBaseTile(x, y);
                    int body = arena.getBodyTile(x, y);
                    tiles[i] = (base << 16) | body;
                    Color c = arena.getBodyColor(x, y);
                    bodyColor[i] = c != null ? c.getRGB() : 0;
                    bodyXoffset[i] = arena.getBodyOffsetX(x, y);
                    bodyYoffset[i] = arena.getBodyOffsetY(x, y);
                }
            }
        }

        CompactTiles ca = new CompactTiles();
        ca.width = width;
        ca.height = height;
        ca.tiles = tiles;
        ca.bodyColor = bodyColor;
        ca.bodyXoffset = bodyXoffset;
        ca.bodyYoffset = bodyYoffset;
        return ca;
    }
}
