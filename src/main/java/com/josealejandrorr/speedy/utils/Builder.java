package com.josealejandrorr.speedy.utils;

import com.josealejandrorr.speedy.annotations.ModelEntity;
import com.josealejandrorr.speedy.files.File;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Builder {

    private static final String FORMAT_DATE_DEFAULT = "yyyy-MM-dd";
    private static final String FORMAT_DATE_TIMESTAMP = "yyyy-MM-dd HH:mm:ss";

    public static Object createInstance(String className) {
        Object object = null;
        try {
            Class classDefinition = Class.forName(className);
            object = classDefinition.newInstance();
        } catch (InstantiationException e) {
            System.out.println("Error 1: "+e);
        } catch (IllegalAccessException e) {
            System.out.println("Error 2: "+e);
        } catch (ClassNotFoundException e) {
            System.out.println("Error 3: "+e);
        }
        return object;
    }

    public static HashMap getParamsFromUrl(String query)
    {
        String[] params = query.split("&");
        HashMap<String, String> map = new HashMap<String, String>();
        for (String param : params)
        {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    public static String convert(InputStream inputStream, Charset charset) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        String bodyRaw = "";
        Pattern pfields = Pattern.compile("name=\"(.*?)\"");
        Pattern pfiles = Pattern.compile("filename=\"(.*?)\"");

        HashMap<String, String> body = new HashMap<String, String>();

        int i = 0;
        String key = null;
        String value = "";
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            while ((line = bufferedReader.readLine()) != null) {
                //System.out.printf("%d %s \n",i,line);
                stringBuilder.append(line);
                bodyRaw = bodyRaw+"\n "+line;


            }
        }
        String[] raw = bodyRaw.split("--------------------");
        Pattern pValue;
        boolean isFile = false;
        for(String node : raw)
        {
            Matcher m = pfields.matcher(node);
            Matcher m2 = pfiles.matcher(node);
            key = null;
            isFile = false;
            //System.out.printf("RAW %s \n",node);
            while(m.find())
            {
                key = m.group();
                body.put(key,null);
                value = "";
                //System.out.printf("FIELD %s \n",m.group());
            }

            while(m2.find())
            {
                isFile = true;
                //System.out.printf("FILE %s \n",m2.group());
            }
            if(key != null) {
                String split = "-"+key+"-";
                //System.out.printf("SPLIT %s \n",split);
                String[] pd =node.split(key);
                key = key.replaceAll("\"","").replace("name=","");
                if(pd.length>1) {
                    value = pd[1].trim();
                    body.put(key,value);
                    System.out.printf("%s=%s \n",key, value);
                }

            }
            if (isFile) {
                value = value.replace("Content-Type: image/png","");
                File.save(key, value);
            }
            i++;
        }
        File.save("pdf.pdf", stringBuilder.toString());

        return stringBuilder.toString();
    }

    public static ArrayList<Map<String, String>> resultSetToArrayList(ResultSet rs)
    {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            while (rs.next()) {
                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Map<String, String> row = new HashMap<>();
                //System.out.println(rs.getRow());
                for (int i = 1; i <= columns; ++i) {
                    switch(md.getColumnTypeName(i))
                    {
                        case "TIMESTAMP":
                            String date = String.valueOf(rs.getTimestamp(i));
                            date = date.replace(".0","");
                            row.put(md.getColumnName(i).toLowerCase(), date);
                            //System.out.println(md.getColumnName(i)+"="+date);
                            break;
                        default:
                            row.put(md.getColumnName(i).toLowerCase(), rs.getString(i));
                            //System.out.println(data.getString(i));
                            break;
                    }
                    //System.out.println(md.getColumnName(i)+"="+rs.getString(i));
                    //row.put(md.getColumnName(i), rs.getString(i));
                }
                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Map bindProperties(Object obj)
    {
        Map<String, Object> temp = new HashMap<String, Object>();
        Class<?> objClass = obj.getClass();
        Field[] fields  = objClass.getFields();

        Annotation an = objClass.getAnnotation(ModelEntity.class);
        ModelEntity me = (ModelEntity) an;

        for( Field field : fields ){
            //System.out.println(field.getType());
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            boolean addField = false;
            try {
                if(!me.timestamps()) {
                    if(field.getName().equals("created_at") || field.getName().equals("updated_at")) {
                        continue;
                    }
                }

                if(field.toGenericString().contains("public"))
                {

                    String value = String.valueOf(field.get(obj));
                    //System.out.println("value "+value);
                    if(value.equals("null")) value = "";
                    if(field.getType().toString().contains("Date")){
                        //value = dt.format(field.get(obj));
                    }

                    temp.put(field.getName(), field.get(obj));
                }

            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                System.out.println("-- ERROR bindProperties: "+e.toString()+" --\n");
                e.printStackTrace();
            }
        }
        return temp;
    }

    public static <T> void setProperties(Object obj, Map<String, String> data)
    {
        //System.out.println("A");
        Class<?> objClass = obj.getClass();
        Field[] fields  = objClass.getDeclaredFields();

        Iterator itte = data.keySet().iterator();

        for( Field field : fields ){
            try {

                if(data.containsKey(field.getName()))
                {
                    //System.out.println(field.getType().toString());
                    //System.out.println(field.getName()+"="+data.get(field.getName()));
                    field.setAccessible(true);
                    if(field.getType().toString().contains("int") || field.getType().toString().contains("Integer")) field.set(obj, Integer.parseInt(data.get(field.getName())));
                    if(field.getType().toString().contains("Long")) field.set(obj, Long.parseLong(data.get(field.getName())));
                    if(field.getType().toString().contains("String")) field.set(obj, String.valueOf(data.get(field.getName())));
                    if(field.getType().toString().contains("double")) field.set(obj, Double.parseDouble(data.get(field.getName())));
                    if(field.getType().toString().contains("Date")){
                        field.set(obj, Builder.convertStringToDate((data.get(field.getName()))));
                    }
                }

            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                System.out.println("-- ERROR setProperties: "+e.toString()+" --\n");
                e.printStackTrace();
            }
        }
    }

    public static HashMap convertJsonToHashMap(String json)
    {
        Logger.getLogger().debug("convertJsonToHashMap ", json);
        HashMap<String, String> obj = new HashMap<String, String>();

        String[] vars =  json.substring(1, json.length() - 1).split(",");

        for (String child : vars)
        {
            String[] var = child.split(":");

            if (var.length > 1) {
                obj.put(var[0], var[1]);
                Logger.getLogger().debug("VAR ",var[0], var[1]);
            }
        }

        return obj;
    }

    public static String convertHashMapToJson(HashMap<String, Object> obj)
    {
        String json = "";

        for (Map.Entry<String, Object> child : obj.entrySet())
        {

            if (child.getValue() == null) {
                json += String.format("\"%s\":null,", child.getKey(), child.getValue());

            } else if (child.getValue() instanceof  Integer) {
                json += String.format("\"%s\":%s,", child.getKey(), child.getValue());

            } else if (child.getValue() instanceof  Double) {
                json += String.format("\"%s\":%s,", child.getKey(), child.getValue());
            } else if (child.getValue() instanceof  Float) {
                json += String.format("\"%s\":%s,", child.getKey(), child.getValue());
            } else if (child.getValue() instanceof  Long) {
                json += String.format("\"%s\":%s,", child.getKey(), child.getValue());
            } else  if (child.getValue() instanceof  Boolean) {
                json += String.format("\"%s\":%s,", child.getKey(), child.getValue());
            } else {
                if (child.getValue().toString().startsWith("[") || child.getValue().toString().startsWith("{")) {
                    json += String.format("\"%s\":%s,", child.getKey(), child.getValue());
                } else {
                    json += String.format("\"%s\":\"%s\",", child.getKey(), child.getValue());
                }
                //json += String.format("\"%s\":\"%s\",", child.getKey(), child.getValue());
            }
        }

        return "{" + json.substring(0, json.length()-1) + "}";
    }

    public static String getClassName(Object object)
    {
        return object.getClass().toString().substring(6, object.getClass().toString().length());
    }

    public static Date convertStringToDate(String text)
    {
        return convertStringToDate(text,FORMAT_DATE_TIMESTAMP);
    }

    public static Date convertStringToDate(String text, String formatDate)
    {
        SimpleDateFormat dt = new SimpleDateFormat(formatDate);
        Date date = new Date();
        try {
            date = dt.parse(text);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Logger.getLogger().error("Error trying parse '"+ text +"' in Date Objects");
        }
        return date;
    }

}
