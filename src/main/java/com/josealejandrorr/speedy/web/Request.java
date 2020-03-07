package com.josealejandrorr.speedy.web;

import com.josealejandrorr.speedy.utils.Builder;
import com.josealejandrorr.speedy.utils.Logger;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Request {

    public String method;

    public Headers headers;

    public HashMap<String, String> query;

    public HashMap<String, String> body;

    public String url;

    public String protocol;

    public Request(HttpExchange exchange)
    {
        this.url = exchange.getRequestURI().toString();
        this.method = exchange.getRequestMethod();
        this.headers = exchange.getRequestHeaders();
        this.body = parseBody(exchange.getRequestBody(), StandardCharsets.UTF_8);
        this.protocol = exchange.getProtocol();

        String[] segments = this.url.split("\\?");
        if(segments.length>1) {
            this.query = Builder.getParamsFromUrl(segments[1]);
        } else {
            this.query = new HashMap<String,  String>();
        }
    }

    private HashMap parseBody(InputStream inputStream, Charset charset)
    {
        //StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        String bodyRaw = "";
        Pattern pfields = Pattern.compile("name=\"(.*?)\"");
        Pattern pfiles = Pattern.compile("filename=\"(.*?)\"");

        HashMap<String, String> body = new HashMap<String, String>();

        int i = 0;
        String key = null;
        String value = "";
        try {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
                while ((line = bufferedReader.readLine()) != null) {
                    //stringBuilder.append(line);
                    bodyRaw = bodyRaw+"\n "+line;
                }
            }
        } catch (IOException e) {
            Logger.getLogger().error("Parse Body Request: " + e.getMessage());
            return body;
        }
        body = Builder.convertJsonToHashMap(bodyRaw);
        System.out.println("==============");
        System.out.println(bodyRaw.trim());
        System.out.println("==============");
        String[] raw = bodyRaw.split("--------------------");
        Pattern pValue;
        boolean isFile = false;
        for(String node : raw)
        {
            Matcher m = pfields.matcher(node);
            Matcher m2 = pfiles.matcher(node);
            key = null;
            isFile = false;

            while(m.find())
            {
                key = m.group();
                //body.put(key,null);
                value = "";
            }

            while(m2.find())
            {
                isFile = true;
            }

            if(key != null) {
                String[] pd =node.split(key);
                key = key.replaceAll("\"","").replace("name=","");
                if(pd.length > 1 && isFile == false) {
                    value = pd[1].trim();
                    body.put(key,value);
                    Logger.getLogger().debug("FIELD",key,value);
                }

            }
        }

        return body;
    }
}
