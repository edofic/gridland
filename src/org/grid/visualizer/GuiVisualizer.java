/*
*  AgentField - a simple capture-the-flag simulation for distributed intelligence
*  Copyright (C) 2012 Andraz Bajt
*  Copyright (C) 2011 Luka Cehovin <http://vicos.fri.uni-lj.si/lukacu>
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
import org.grid.arena.SwingView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class GuiVisualizer {

    private static final String RELEASE = "0.9";

    private static GameSwingView view = new GameSwingView();

    private static JLabel gameStepDisplay = new JLabel();

    private static final String[] ZOOM_LEVELS_TITLES = new String[]{"tiny", "small", "normal",
            "big", "huge"};

    private static final int[] ZOOM_LEVELS = new int[]{6, 12, 16, 20, 24};

    private static class GameSwingView extends SwingView {

        private static final long serialVersionUID = 1L;

        public GameSwingView() {
            super(12);
        }

        @Override
        public void paint(Graphics g) {

            Arena view = getArena();

            paintBackground(g, view);

            paintObjects(g, view);

            g.setColor(Color.YELLOW);
        }
    }

    public static void main(String[] args) throws IOException {

        info("Starting game visualizer (release %s)", RELEASE);

        if (args.length != 2) {
            info("please supply host and port as parameters to program");
            System.exit(98);
        }

        String host = args[0];
        int port = 0; //0 is there because java compiler doesn't understand System.exit
        try {
            port = Integer.parseInt(args[1]);
        } catch (Exception e) {
            info("invalid port number: " + args[1]);
            System.exit(99);
        }

        info("Java2D OpenGL acceleration "
                + (("true".equalsIgnoreCase(System
                .getProperty("sun.java2d.opengl"))) ? "enabled"
                : "not enabled"));


        new Thread(new PushClient(host, port) {
            @Override
            protected void onReceive(Object data) {
                if (data instanceof CompactTiles) {
                    CompactTiles tiles = (CompactTiles) data;
                    Arena compactArena = new CompactArena(tiles);
                    view.update(compactArena);
                }
            }
        }).start();

        //todo perhaps load some game data in the beggining?
        JFrame window = new JFrame("AgentField - " + "TODO title");

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JScrollPane pane = new JScrollPane(view);

        JPanel left = new JPanel(new BorderLayout());

        left.add(pane, BorderLayout.CENTER);

        JPanel status = new JPanel(new BorderLayout());

        final JComboBox zoom = new JComboBox(ZOOM_LEVELS_TITLES);

        zoom.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int ind = zoom.getSelectedIndex();

                if (ind > -1) {
                    view.setCellSize(ZOOM_LEVELS[ind]);
                    pane.repaint();
                }
            }
        });

        zoom.setSelectedIndex(1);
        zoom.setEditable(false);

        status.add(zoom, BorderLayout.EAST);

        gameStepDisplay.setHorizontalAlignment(JLabel.CENTER);
        status.add(gameStepDisplay, BorderLayout.CENTER);

        left.add(status, BorderLayout.NORTH);

        window.getContentPane().add(left);

        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();

        Rectangle r = ge.getDefaultScreenDevice().getDefaultConfiguration()
                .getBounds();

        window.pack();

        Dimension ws = window.getSize();

        if (r.width - ws.width < 100) {
            ws.width = r.width - 100;
        }

        if (r.height - ws.height < 100) {
            ws.height = r.height - 100;
        }

        window.setSize(ws);

        window.setVisible(true);
    }

    private static DateFormat date = new SimpleDateFormat("[hh:mm:ss] ");

    public static void info(String format, Object... objects) {
        System.out.println(date.format(new Date()) + String.format(format, objects));
    }
}
