package com.josealejandrorr.speedy.files;

import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.utils.Logger;

import java.io.*;
import java.nio.file.*;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class Files {

    public static final String DIR_TEMP = "storage/temp/";
    public static final String DIR_UPLOADS = "./storage/uploads/";

    private static String[] filesUploaded;

    public static boolean save(String filePath, String content)
    {
        return Files.save(filePath, content, false);
    }

    public static boolean save(String filePath, String content, boolean rewrite)
    {
        if(content.trim().length()==0) return false;

        if(rewrite) {
            return Files.rewriteFile(filePath, content);
        } else {
            return Files.writeFile(filePath, content);
        }
    }

    private static boolean writeFile(String path, String content)
    {
        return Files.writeFile(path, content.getBytes());
    }

    public static boolean writeFile(String path, byte[] data)
    {
        //byte data[] = content.getBytes();
        Path p = Paths.get(path);

        try (OutputStream out = new BufferedOutputStream(
                java.nio.file.Files.newOutputStream(p, CREATE))) {
            out.write(data, 0, data.length);
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            Application.logger.error("Error Writing File: '" + path + "' by: " + e.getMessage());
        }
        return false;
    }

    private static boolean rewriteFile(String path, String content)
    {
        byte data[] = content.getBytes();
        Path p = Paths.get(path);

        try (OutputStream out = new BufferedOutputStream(
                java.nio.file.Files.newOutputStream(p, CREATE, APPEND))) {
            out.write(data, 0, data.length);
            return true;
        } catch (IOException e) {
            Application.logger.error(e.getMessage());
        }
        return false;
    }

    public static File getTempFile(String path)
    {
       return get(Files.DIR_TEMP + path);
    }

    public static File get(String path)
    {
        java.io.File f = new java.io.File(path);
        if(f.exists() && !f.isDirectory()) {
            return f;
        }
        return null;
    }

    public static String getTextFromFile(String path)
    {
        String content = "";
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            for(String line; (line = br.readLine()) != null; ) {
                content = line;
            }
            // line is not visible here.
        } catch (IOException e) {
            Application.logger.error("Error Loading Files: '" + path + "' by: " + e.getMessage());
        }
        return content;
    }

    public static void clearTempDir()
    {
        if(Files.filesUploaded == null) return;
        for (String url : Files.filesUploaded)
        {
            Files.delete(url);
        }
        Files.filesUploaded = null;
    }

    public static boolean move(String source, String dest)
    {
        Path sourceFile = FileSystems.getDefault().getPath("",source);
        Path destFile = FileSystems.getDefault().getPath("",dest);
        return Files.move(sourceFile, destFile);
    }

    public static boolean move(Path source, Path dest)
    {
        try {
            java.nio.file.Files.move(source, dest);
            return true;
        } catch (IOException e) {
            Application.logger.error("Error Moving File: from '" + source + "' to '"+dest+"' by: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean exist(String path)
    {
        java.io.File f = new java.io.File(path);
        if(f.exists() && !f.isDirectory()) {

            return true;
        }
        return false;
    }


    public static boolean delete(String path)
    {
        Path file = FileSystems.getDefault().getPath("",path);
        return Files.delete(file);
    }

    public static boolean delete(Path path)
    {
        try {
            return java.nio.file.Files.deleteIfExists(path);
        } catch (IOException e) {
            Application.logger.error("Error Deleting File: '" + path + "' by: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

}
