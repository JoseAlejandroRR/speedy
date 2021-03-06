package com.josealejandrorr.speedy.utils;


import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.files.File;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger implements ILogger {

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

    public static final String MODE_DEBUG = "Debug";

    public static final String MODE_INFO = "Info";

    public static final String MODE_ERROR = "Error";

    public static final int DEBUG = 1;

    public static final int INFO = 2;

    private int mode = DEBUG;

    private String modeLevel = MODE_DEBUG;

    private String fileName;

    private Path file;

    private static Logger instance;

    public Logger(int mode, String filename)
    {
        this.mode = mode;

        if(mode == INFO) {
            modeLevel = MODE_INFO;
        }
        this.fileName = filename;
        Path file = Paths.get(fileName);
        instance = this;
    }

    public void debug(String... strings) {

        if(mode != DEBUG) return;
        String line = registerData(strings, MODE_DEBUG);
        showConsole(line, MODE_DEBUG);
        writeFile(line);
    }

    public void info(String... strings) {
        String line = registerData(strings, MODE_INFO);
        showConsole(line, MODE_INFO);
        writeFile(line);
    }

    public void error(String... strings) {
        String line = registerData(strings, MODE_ERROR);
        showConsole(line, MODE_INFO);
        writeFile(line);
    }

    private void showConsole(String line, String mode)
    {
        if(mode != MODE_ERROR) {
            print(line);
        } else {
            error(line);
        }
    }

    private String registerData(String[] strings, String mode)
    {
        String timeStamp = format.format(Calendar.getInstance().getTime());
        String nodes = "";
        //print(mode + " \t ");
        //print(timeStamp+ " \t ");

        for(String str:  strings)
        {
            nodes = nodes + "" + str + " \t";
            if (nodes.trim().length() == 0) {

            } else {
            }
            //print(str + " \t");
        }
        return String.format("%s \t %s \t %s\n", mode, timeStamp, nodes);
        //print("\n");
    }

    private void writeFile(String text)
    {
        File.save(fileName, text, true);
    }

    private void print(String str) {
        System.out.printf(str);
    }

    private void error(String str) {
        System.err.printf(str);
    }

    public static Logger getLogger()
    {
        return instance;
    }
}

