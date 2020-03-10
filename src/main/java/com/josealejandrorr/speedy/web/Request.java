package com.josealejandrorr.speedy.web;

import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.files.Files;
import com.josealejandrorr.speedy.utils.Builder;
import com.josealejandrorr.speedy.utils.Logger;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Request {

    public String method;

    public Headers headers;

    public HashMap<String, String> query;

    public HashMap<String, Object> body;

    public String url;

    public String protocol;

    private static String[] filesUploaded;

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_TEXT = "text/plain";
    public static final String CONTENT_TYPE_HTML = "text/html";

    public Request(HttpExchange exchange)
    {
        this.url = exchange.getRequestURI().toString();
        this.method = exchange.getRequestMethod();
        this.headers = exchange.getRequestHeaders();
        this.body = parseBody(exchange, StandardCharsets.UTF_8);
        this.protocol = exchange.getProtocol();

        String[] segments = this.url.split("\\?");
        if(segments.length>1) {
            this.query = Builder.getParamsFromUrl(segments[1]);
        } else {
            this.query = new HashMap<String,  String>();
        }
    }

    private HashMap parseBody(HttpExchange exchange, Charset charset)
    {
        //StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        String bodyRaw = "";


        HashMap<String, Object> body = new HashMap<String, Object>();

        String contentType = CONTENT_TYPE_JSON;
        if (headers.containsKey("Content-type")){
            contentType =  headers.get("Content-type").get(0);
        }

       body = parseTextToBody(contentType, exchange, charset);

        return body;
    }

    private HashMap<String, Object> parseTextToBody(String contentType, HttpExchange exchange, Charset charset)
    {
        HashMap<String, Object> body = new HashMap<String, Object>();
        String line;
        String bodyRaw = "";

        if (contentType.contains("multipart/form-data; boundary")) {
            return bodyFromFormData(exchange);
        }
        try {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), charset))) {
                while ((line = bufferedReader.readLine()) != null) {
                    //stringBuilder.append(line);
                    bodyRaw = bodyRaw+"\n "+line;
                }
            }
        } catch (IOException e) {
            Application.logger.error("Parse Body Request: " + e.getMessage());
            return body;
        }

        if (bodyRaw.trim().length() < 1) {
            return body;
        }

        if (contentType.equals(CONTENT_TYPE_JSON))
        {
            JSONObject json = new JSONObject(bodyRaw);
            body = (HashMap<String, Object>) json.toMap();
        } else if (contentType.contains("application/x-www-form-urlencoded")) {
            String[] vars = bodyRaw.trim().split("&");

            for(String var : vars)
            {
                String[] v = var.split("=");

                if (v.length == 2) {
                    body.put(v[0],v[1]);
                }
            }
        }

        return body;
    }

    private HashMap<String, Object> bodyFromFormData(HttpExchange httpExchange)
    {
        HashMap<String, Object> body = new HashMap<>();
        String fileUrl = null;

        //if (Files.headers== null || Files.requestBody==null) return fileUrl;

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
            Logger.getLogger().debug("ERROR : " + e.getMessage());
            e.printStackTrace();
        }

        int i = 0;
        for(Map.Entry<String, Object> item : map.entrySet())
        {
            System.out.println(item.getKey());
            if (map.get(item.getKey()) instanceof FileInfo) {
                FileInfo fileInfo = (FileInfo) map.get(item.getKey());
                fileUrl = Files.DIR_TEMP + fileInfo.getFilename();
                Files.writeFile(fileUrl,fileInfo.getBytes());
                body.put(item.getKey(), fileInfo.getFilename());
            } else {
                body.put(item.getKey(), item.getValue());
            }

        }

        return body;
    }

}
