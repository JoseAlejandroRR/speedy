package com.josealejandrorr.speedy.files;

import com.josealejandrorr.speedy.utils.Logger;
import com.josealejandrorr.speedy.web.Analysis;
import com.josealejandrorr.speedy.web.FileInfo;
import com.sun.net.httpserver.HttpExchange;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class File {

    public static final String DIR_TEMP = "storage/temp/";
    public static final String DIR_UPLOADS = "./storage/uploads/";

    private static String[] filesUploaded;

    public static boolean save(String filePath, String content)
    {
        return File.save(filePath, content, false);
    }

    public static boolean save(String filePath, String content, boolean rewrite)
    {
        if(content.trim().length()==0) return false;

        if(rewrite) {
            return File.rewriteFile(filePath, content);
        } else {
            return File.writeFile(filePath, content);
        }
    }

    private static boolean writeFile(String path, String content)
    {
        return File.writeFile(path, content.getBytes());
    }

    private static boolean writeFile(String path, byte[] data)
    {
        //byte data[] = content.getBytes();
        Path p = Paths.get(path);

        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(p, CREATE))) {
            out.write(data, 0, data.length);
            out.flush();
            out.close();
            return true;
        } catch (IOException x) {
            System.err.println(x);
        }
        return false;
    }

    private static boolean rewriteFile(String path, String content)
    {
        byte data[] = content.getBytes();
        Path p = Paths.get(path);

        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(p, CREATE, APPEND))) {
            out.write(data, 0, data.length);
            return true;
        } catch (IOException x) {
            System.err.println(x);
        }
        return false;
    }

    public static String get(String path)
    {
        String content = "";
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            for(String line; (line = br.readLine()) != null; ) {
                content = line;
            }
            // line is not visible here.
        } catch (IOException e) {
            Logger.getLogger().debug("Error Loading File: " + path + " by: " + e.getMessage());
        }
        return content;
    }

    public static void getFiles(HttpExchange httpExchange)
    {
        String fileUrl = null;

        //if (File.headers== null || File.requestBody==null) return fileUrl;

        //获取ContentType
        String contentType = httpExchange.getRequestHeaders().get("Content-type").toString().replace("[", "")
                .replace("]", "");

        //获取content长度
        int length = Integer.parseInt(httpExchange.getRequestHeaders().get("Content-length").toString().replace("[", "")
                .replace("]", ""));

        Map<String, Object> map = null;
        try {
            map = Analysis.parse(httpExchange.getRequestBody(),
                    contentType, length);
        } catch (IOException e) {
            Logger.getLogger().debug("ERROR1 : " + e.getMessage());
            e.printStackTrace();
        }

        if (map.size() > 0) {
            File.filesUploaded = new String[map.size()];
        }
        int i = 0;
        for(Map.Entry<String, Object> item : map.entrySet())
        {
            if (String.valueOf(item.getKey()).trim().length() == 0) continue;
            FileInfo fileInfo = (FileInfo) map.get(item.getKey());
            fileUrl = File.DIR_TEMP + fileInfo.getFilename();
            Logger.getLogger().debug("FILE",fileInfo.toString());
            File.writeFile(fileUrl,fileInfo.getBytes());
            Logger.getLogger().debug("RARO ",String.valueOf(map.size()), item.getKey(), fileInfo.getFieldname());
            File.filesUploaded[i] = fileUrl;
        }

    }

    public static void clearTempDir()
    {
        if(File.filesUploaded == null) return;
        for (String url : File.filesUploaded)
        {
            File.delete(url);
        }
        File.filesUploaded = null;
    }

    public static boolean move(String source, String dest)
    {
        Path sourceFile = FileSystems.getDefault().getPath("",source);
        Path destFile = FileSystems.getDefault().getPath("",dest);
        return File.move(sourceFile, destFile);
    }

    public static boolean move(Path source, Path dest)
    {
        try {
            Files.move(source, dest);
            return true;
        } catch (IOException e) {
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
        return File.delete(file);
    }

    public static boolean delete(Path path)
    {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
