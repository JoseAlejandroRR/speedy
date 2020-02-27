package com.josealejandrorr.speedy.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Cookies {

    private static Headers headersReq;

    private static Headers headersRes;

    private static List<String> cookiesList;

    private static HashMap<String, String> cookies;

    private static final String URI_PATH = "/";

    private static final String COOKIE_DATE_EXPIRATED = "Thu, 01 Jan 1970 00:00:00 GMT";

    public static void add(String key, String value)
    {
        Cookies.add(key, value, Cookies.URI_PATH);
    }

    public static void add(String key, String value, String path)
    {
        List<String> values = new ArrayList<>();
        values.add(key + "=" + value + "; version=1; Path=" + path +"; HttpOnly");
        Cookies.headersRes.put("Set-Cookie", values);
        Cookies.cookiesList.add(key);
        Cookies.cookies.put(key, value);
    }

    public static String get(String key)
    {
        if (Cookies.has(key)) {
            return Cookies.cookies.get(key);
        }
        return null;
    }

    public static boolean has(String key)
    {
        if (Cookies.cookies.get(key) != null) {
            return true;
        }
        return false;
    }

    public static void destroy(String key)
    {
        List<String> values = new ArrayList<>();
        values.add(key + "=unknown" +  "; version=1; Path=" + Cookies.URI_PATH +"; HttpOnly; expires=" + Cookies.COOKIE_DATE_EXPIRATED);
        Cookies.headersRes.put("Set-Cookie", values);
        Cookies.cookiesList.remove(key);
        Cookies.cookies.remove(key);
    }

    public static void setHeaders(HttpExchange exchange)
    {
        Cookies.headersReq = exchange.getRequestHeaders();
        Cookies.headersRes = exchange.getResponseHeaders();
        Cookies.cookies = new HashMap<String, String>();

        if (headersReq.get("Cookie") != null) {
            Cookies.cookiesList = headersReq.get("Cookie");
            for (String cookie : Cookies.cookiesList) {
                String[] ck = cookie.split("=");
                if (ck.length>1) {
                    Cookies.cookies.put(ck[0],ck[1]);
                }
            }
        } else {
            Cookies.cookiesList = new ArrayList<>();;
        }
    }
}