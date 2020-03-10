package com.josealejandrorr.speedy.web;

import app.models.Device;
import com.josealejandrorr.speedy.database.Model;
import com.josealejandrorr.speedy.utils.Builder;
import com.josealejandrorr.speedy.views.View;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Response {

    private String response;

    public int httpCode = 200;

    public boolean handled = false;

    private HttpExchange httpExchange;

    private static final String CONTENT_TYPE_HTML = "text/html";

    private static final String CONTENT_TYPE_JSON = "application/json";

    private String content_type;

    public Response(HttpExchange exchange)
    {
        this.httpExchange = exchange;
    }

    public Response send(String response)
    {
        return send(response, httpCode);
    }

    public Response send(int httpCode)
    {
        return send("", httpCode);
    }

    public Response send(String response, int httpCode)
    {
        this.httpCode = httpCode;

        if (this.response == null)
        {
            this.response = response;
        }

        if(content_type == null)
        {
            content_type = CONTENT_TYPE_HTML;
        }

        httpExchange.getResponseHeaders().add("Content-Type", content_type);

        handled = true;

        return this;
    }

    public String getResponse()
    {
        return response;
    }

    public void json(HashMap obj)
    {
        json(Builder.convertHashMapToJson(obj), httpCode);
    }

    public void json(HashMap obj, int httpCode)
    {
        json(Builder.convertHashMapToJson(obj), httpCode);
    }

    public void json(Object obj)
    {
        json(obj, httpCode);
    }

    public void json(Object obj, int httpCode)
    {

        String data = "";
        if (obj instanceof ArrayList) {
            data = "[" +((ArrayList) obj).stream().map(item -> jsonFromModel(item)).collect(Collectors.joining(",")).toString() + "]";
        } else {
            data = jsonFromModel(obj);
        }

        json(data, httpCode);
    }

    public void json(String obj, int httpCode)
    {
        //String json = Builder.convertHashMapToJson(obj);

        content_type = CONTENT_TYPE_JSON;

        send(obj, httpCode);
    }

    private String jsonFromModel(Object obj)
    {
        //  Builder.bindProperties(obj);
        if (obj instanceof String) {
            return obj.toString();
        } else if (obj instanceof Map) {
            return Builder.convertHashMapToJson((HashMap) obj);
        }
        HashMap<String, Object> map = new HashMap<String, Object>(
                Builder.bindProperties(obj)
        );
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if(value == null) {
                map.put(key,  null);
            } else if (value.getClass().getSuperclass().getName().contains("speedy.data.entities.Model")) {
                map.put(key,  jsonFromModel(value));
            } else if (value instanceof ArrayList){
                String node = ((ArrayList) value).stream().map(n -> jsonFromModel(n)).collect(Collectors.joining(",")).toString();
                map.put(key, "["+node+"]");
            }
        }

        return Builder.convertHashMapToJson(map);
    }

    public void view(String file, HashMap<String, Object> data)
    {
        String html = View.render(file, data);

        send(html);
    }
}
