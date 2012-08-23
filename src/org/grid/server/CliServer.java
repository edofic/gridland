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

package org.grid.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class CliServer {

    private static final int PORT = 5000;

    private static final int VISUALIZATION_PORT = 5001;

    private static Publisher publisher = new Publisher(VISUALIZATION_PORT, 10, 4);

    private static final String RELEASE = "0.9";

    private static Game game;

    private static long stepTime = 0;

    private static int stepCount = 0;

    private static boolean running = false;

    private static PrintWriter log;

    public static void main(String[] args) throws IOException {

        info("Starting game server (release %s)", RELEASE);

        if (args.length < 1) {
            info("Please provide game description file location as an argument.");
            System.exit(1);
        }

        info("Java2D OpenGL acceleration "
                + (("true".equalsIgnoreCase(System
                .getProperty("sun.java2d.opengl"))) ? "enabled"
                : "not enabled"));

        game = Game.loadFromFile(new File(args[0]));

        try {
            log = new PrintWriter(new File(logDate.format(new Date()) + "_" + game.getTitle() + ".log"));

        } catch (Exception e) {
            //nothing to do
        }

        Dispatcher dispatcher = new Dispatcher(PORT, game);

        final int gameSpeed = game.getSpeed();

        (new Thread(new Runnable() {

            @Override
            public void run() {
                int sleep = 1000 / gameSpeed;
                long start, used;
                while (true) {

                    start = System.currentTimeMillis();

                    if (running)
                        game.step();

                    final Field field = game.getField();

                    //publish state to all remote visualizers
                    publisher.publishArena(field);

                    used = System.currentTimeMillis() - start;

                    stepTime += used;
                    stepCount++;

                    if (game.getStep() % 100 == 0 && running) {
                        long stepFPS;

                        stepFPS = (stepCount * 1000) / Math.max(1, stepTime);
                        stepCount = 0;
                        stepTime = 0;

                        info(
                                "Game step: %d (step: %d fps)",
                                game.getStep(), stepFPS);
                    }

                    try {
                        if (used < sleep)
                            Thread.sleep(sleep - used);
                        else {
                            info("Warning: low frame rate");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        })).start();

        (new Thread(dispatcher)).start();

        log("Server ready.");

        //auto-run if arg: run in second place else provide simple interface
        if (args.length >= 2 && args[1].equalsIgnoreCase("run")) {
            System.out.println("auto-run mode");
            running = true;
        } else {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Availible commands: run, stop, exit");
                String cmd = scanner.next();
                if (cmd.equalsIgnoreCase("run")) {
                    running = true;
                    System.out.println("running = " + running);
                } else if (cmd.equalsIgnoreCase("stop")) {
                    running = false;
                    System.out.println("running = " + running);
                } else if (cmd.equalsIgnoreCase("exit")) {
                    System.exit(0);
                } else {
                    System.out.println("invalid command");
                }
            }
        }
    }

    private static DateFormat logDate = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

    private static DateFormat date = new SimpleDateFormat("[hh:mm:ss] ");

    public static void log(String format, Object... objects) {

        try {

            String msg = String.format(format, objects);

            System.out.println(date.format(new Date()) + msg);

            if (log != null) {
                log.println(date.format(new Date()) + msg);
                log.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void info(String format, Object... objects) {
        System.out.println(date.format(new Date()) + String.format(format, objects));
    }
}
