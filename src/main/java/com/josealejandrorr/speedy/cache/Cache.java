package com.josealejandrorr.speedy.cache;

import com.josealejandrorr.speedy.files.File;
import com.josealejandrorr.speedy.utils.Builder;
import com.josealejandrorr.speedy.utils.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.*;

public class Cache {

    static final long ONE_MINUTE = 5 * 6000;
    static final String PATH = "./storage/cache/";

    public static Object getString(String key)
    {
        Object result = null;

        if (Cache.hasKey(key)){
            String path = PATH + DatatypeConverter.printBase64Binary(key.getBytes());
            String content = File.get(path);
            if (content != null && content.trim().length()>0){
                String[] json = content.split("\\]\\[");
                String details = json[0].replace("[{","");
                String[] detailsObj = details.split(",");

                String className = "";
                long expired_at = 0;


                for(int i = 0; i < detailsObj.length; i++)
                {
                    String[] item = detailsObj[i].split(":");
                    if(item[0].contains("class"))
                    {
                        className = item[1];
                    }

                    if (item[0].contains("expired_at")) {
                        expired_at = Long.parseLong(item[1].substring(1,item[1].length()-1));

                        if(expired_at > 0) {
                            Date now = new Date();

                            if(now.getTime() > expired_at) {
                                File.delete(path);
                                return null;
                            }
                        }
                    }
                }

                String obj = json[1].replace("\"","");

                Logger.getLogger().debug("JSON ",obj, className.substring(7, className.length()-1));

                switch (className.substring(7, className.length()-1))
                {
                    case "java.lang.String":
                        result = obj;
                        break;

                    case "java.lang.Integer":
                    case "int":
                        result = obj;
                    case "java.util.HashMap":
                        result = Builder.convertJsonToHashMap(obj);
                        break;
                }

            }
        }
        return result;
    }

    public static void add(String key, String content)
    {
        Cache.add(key, content, true);
    }


    public static void add(String key, String content, boolean expired)
    {
        Date now = new Date();
        long expired_at = 0;

        if (expired) {
            Date after = new Date(now.getTime()+(10*ONE_MINUTE));
            expired_at = after.getTime();
        }

        Cache.store(key, content, key.getClass().toString(), expired);
    }

    public static void add(String key, HashMap<String, Object> obj)
    {
        Cache.add(key, obj, true);
    }

    public static void add(String key, HashMap<String, Object> obj, boolean expired)
    {
        String json = Builder.convertHashMapToJson(obj);

        /*for(Map.Entry<String, String> item : obj.entrySet())
        {
            data += String.format("\"%s\":\"%s\",",item.getKey(),item.getValue());
        }

        data = "{" + data.substring(0, data.length()-1) + "}";*/

        if (json.length() > 0) {

            Logger.getLogger().debug("JSON RESULT",json);

            Cache.store(key, json, obj.getClass().toString(), expired);
        }

    }

    //
    public static void add(String keyCache, ArrayList<Map<String, String>> list)
    {
        //System.out.println("OK");
        Date now = new Date();
        Date after = new Date(now.getTime()+(1*ONE_MINUTE));
        String json = "";
        String items = "";
        //System.out.println(list.size());
        for(int i =0; i < list.size(); i++)
        {
            String item = "";
            Iterator itte = list.get(i).keySet().iterator();
            while(itte.hasNext())
            {
                String key = (String) itte.next();
                String val = list.get(i).get(key).replaceAll(",", "k_0m4");
                item += String.format("\"%s\":\"%s\",",key,val);
                //System.out.println(item);

            }
            item = item.substring(0,item.length()-1);
            //System.out.println(items);
            items += "{"+item+"},";

        }
        items = items.substring(0,items.length()-1);
        //json = String.format("{\"created_at\":\"%s\",\"forget_at\":\"%s\",\"items\":\"%s\"",now.getTime(),after.getTime(),items);
        json = String.format("[{\"created_at\":\"%s\",\"update_at\":\"%s\",\"expired_at\":\"%s\",\"class\":\"%s\"][\"%s\"",now.getTime(),now.getTime(),after.getTime(),list.getClass(),items);
        //json = items;
        //System.out.println(json);
        try {
            String path = DatatypeConverter.printBase64Binary(keyCache.getBytes());
            FileOutputStream file = new FileOutputStream(PATH+path+"");
            file.write(json.getBytes());
            //System.out.println("Export ok");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    private static void store(String key, String data,String packageClass, boolean expired)
    {
        Date now = new Date();
        long expired_at = 0;

        if (expired) {
            Date after = new Date(now.getTime()+(10*ONE_MINUTE));
            expired_at = after.getTime();
        }
        Logger.getLogger().debug("WRITE ", data);
        String json =  String.format("[{\"created_at\":\"%s\",\"update_at\":\"%s\",\"expired_at\":\"%s\",\"class\":\"%s\"][\"%s\"",now.getTime(),now.getTime(),expired_at,packageClass,data);
        Logger.getLogger().debug("WRITE JSON ", json);

        String path = PATH + DatatypeConverter.printBase64Binary(key.getBytes());
        if (File.exist(path)) {
            File.delete(path);
        }

        File.save(path, json);
    }

    /*public static void add(String key,ResultSet rs)
    {
        add(key,Helpers.resultSetToArrayList(rs));
    }*/

    public static ArrayList<Map<String, String>> get(String keyCache)
    {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        String file = DatatypeConverter.printBase64Binary(keyCache.getBytes());
        file = PATH+file;
        String content = Cache.loadObject(file);
        //System.out.println(content);
        String[] json = content.split("\\]\\[");
        String details = json[0].replace("[{","");
        String[] detailsObj = details.split(",");

        String className = "";

        for(int i = 0; i < detailsObj.length; i++)
        {
            String[] item = detailsObj[i].split(":");
            if(item[0].contains("class"))
            {
                className = item[1];
                //System.out.println(className);
            }
        }


        String[] items = json[1].split("}");
        for(int i = 0; i < items.length-1; i++)
        {
            //System.out.println(items[i]);
            Map<String, String> row = new HashMap<>();
            String obj = items[i];
            obj = obj.substring(2,obj.length());
            //System.out.println(i);
            //System.out.println(obj);
            //obj = obj.replace("{","").replace("}","");
            String item[] = obj.split(",");
            for(int j = 0; j < item.length; j++)
            {
                String[] data = item[j].replaceAll("k_0m4",",").split("\":\"");
                //System.out.println(data[0]);
                String value = data[1].replace("\"","");
                String key = data[0].substring(1,data[0].length());
                //if(data.length>1) value = data[1];
                row.put(key, value);
                //System.out.println("J = "+j);

                //System.out.println(key+"="+value);
            }
            list.add(row);
        }
        return list;
    }

    public static boolean hasKey(String key)
    {
        String file = DatatypeConverter.printBase64Binary(key.getBytes());
        if (File.exist(PATH + file)) {
            return true;
        }
        /*File f = new File(file);
        if(f.exists() && !f.isDirectory()) {
            // do something
            //System.out.println("EXISTE "+file);
            result = true;
        }*/
        return false;
    }

    private static String loadObject(String file)
    {
        String content = "";
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            for(String line; (line = br.readLine()) != null; ) {
                content = line;
            }
            // line is not visible here.
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return content;
    }
}
