package org.grid.visualizer;

import org.grid.arena.Arena;

import java.awt.*;

/**
 * Reconstruction from CompactTiles
 * User: andraz
 * Date: 8/21/12
 * Time: 11:05 PM
 */
public class CompactArena implements Arena {
    private static final int LOWER_BYTES = 0xffff;

    private CompactTiles tiles;

    public CompactArena(CompactTiles tiles) {
        this.tiles = tiles;
    }

    private int encode(int x, int y) {
        if (x < 0 || x >= tiles.width || y < 0 || y >= tiles.height) {
            throw new IndexOutOfBoundsException(String.format("Width: %d, height %d, x %d, y %d",
                    tiles.width, tiles.height, x, y));
        }
        return y * tiles.width + x;
    }

    @Override
    public int getWidth() {
        return tiles.width;
    }

    @Override
    public int getHeight() {
        return tiles.height;
    }

    @Override
    public int getBaseTile(int x, int y) {
        return (tiles.tiles[encode(x, y)] >> 16);
    }

    @Override
    public int getBodyTile(int x, int y) {
        return (tiles.tiles[encode(x, y)] & LOWER_BYTES);
    }

    @Override
    public float getBodyOffsetX(int x, int y) {
        return tiles.bodyXoffset[encode(x, y)];
    }

    @Override
    public float getBodyOffsetY(int x, int y) {
        return tiles.bodyYoffset[encode(x, y)];
    }

    @Override
    public Color getBodyColor(int x, int y) {
        int c = tiles.bodyColor[encode(x, y)];
        return c != 0 ? new Color(c) : null;
    }
}
