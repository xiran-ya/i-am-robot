package dev.xiran.i_am_robot.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class VirtualMachine implements Runnable {
    private static final VirtualMachine INSTANCE = new VirtualMachine();
    private boolean running = false;
    private File script;
    private String[] instructions;

    @Override
    public void run() {
        running = true;
//        try (FileReader reader = new FileReader(script)) {
//            char[] buffer = new char[200];
//            int c = reader.read();
//            int i = 0;
//            int j = 0;
//            while () {
//                if (c == -1 || (char)c == '\n') {
//                    instructions[j] = new String(buffer);
//                    j++;
//                } else {
//                    buffer[i] = (char)c;
//                    i++;
//                }
//                c = reader.read();
//            }
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }



    public static VirtualMachine getInstance() {
        return INSTANCE;
    }

    public boolean isRunning() {
        return running;
    }

    public void setScript(File script) {
        this.script = script;
    }
}
