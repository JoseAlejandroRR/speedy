package com.josealejandrorr.speedy.web;

import com.josealejandrorr.speedy.cache.Cache;
import com.josealejandrorr.speedy.utils.Logger;

import java.util.HashMap;

public class Sessions {

    public static void add(String key, String value)
    {
        if (Cookies.has(Server.SESSION_SERVER_NAME)) {

            String sessionID = Cookies.get(Server.SESSION_SERVER_NAME);
            HashMap<String, Object> obj = null;

            if (Cache.hasKey(sessionID)) {
                obj = (HashMap<String, Object>) Cache.getString(sessionID);
            } else {
                obj = new HashMap<String, Object>();
            }


            if (obj != null) {
                obj.put(key, value);
                Cache.add(sessionID, obj, false);
            }
        }
    }

    public static String get(String key)
    {
        if (Cookies.has(Server.SESSION_SERVER_NAME)) {

            String sessionID = Cookies.get(Server.SESSION_SERVER_NAME);

            if (Cache.hasKey(sessionID)) {
                Logger.getLogger().debug("PRIMERO: " + key);
                HashMap<String, String> obj = (HashMap) Cache.getString(sessionID);

                if (obj!= null && obj.get(key) != null) {
                    return obj.get(key);
                }
            }
        }
        return null;
    }

    public static void destoy(String key)
    {
        if (Cookies.has(Server.SESSION_SERVER_NAME)) {

            String sessionID = Cookies.get(Server.SESSION_SERVER_NAME);

            if (Cache.hasKey(sessionID)) {
                HashMap<String, Object> obj = (HashMap) Cache.getString(sessionID);

                if (obj != null && obj.size() > 0) {
                    obj.remove(key);
                    Cache.add(sessionID, obj, false);
                }
            }


        }
    }
}
