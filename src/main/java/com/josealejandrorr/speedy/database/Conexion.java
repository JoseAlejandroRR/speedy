package com.josealejandrorr.speedy.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.bind.DatatypeConverter;


public class Conexion {

    public static String host = "";
    public static String db = "";
    public static String user = "";
    public static String pass = "";
    public static Integer port = 3306;
    public static String url = "jdbc:mysql2://"+Conexion.host+":"+Conexion.port+"/"+Conexion.db+"?generateSimpleParameterMetadata=true&zeroDateTimeBehavior=convertToNull";
    public static boolean modeDebug = false;
    public static boolean modeCache = false;
    public static boolean modeStats = false;
    public static boolean modeNotifitations = false;
    public static boolean modeAsynchrony = false;

    public static ArrayList notifications = new ArrayList();
    public static int refreshReq = -1;

    private static boolean isLoad = false;


    public Conexion()
    {
        if(!isLoad) loadVars();
    }

    private void loadVars()
    {
        List<Map<String, String>> list = new ArrayList<>();
        String file = "eloquent.config";
        String content = "";
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            for(String line; (line = br.readLine()) != null; ) {
                String item[] = line.split("=");
                if(item.length>1){
                    switch(item[0])
                    {
                        case "host":
                            Conexion.host = item[1];
                            break;
                        case "user":
                            Conexion.user = item[1];
                            break;
                        case "pass":
                            Conexion.pass = item[1];
                            break;
                        case "db":
                            Conexion.db = item[1];
                            break;
                        case "port":
                            Conexion.port = Integer.parseInt(item[1]);
                            break;
                    }
                }
                content = line;
                Conexion.url = "jdbc:mysql://"+Conexion.host+":"+Conexion.port+"/"+Conexion.db+"?generateSimpleParameterMetadata=true&zeroDateTimeBehavior=convertToNull";

            }
            //System.out.println(Conexion.url);
            //System.out.printf("Conetando %s %s\n",Conexion.user, Conexion.pass);
            isLoad = true;
            // line is not visible here.
        } catch (IOException e) {
            System.out.println("Error al Leer el Archivo de Configuracion");
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
        //return content;
    }

    public void setModeCache(boolean _asyn, int _time)
    {
        Conexion.modeCache = true;
        String PATH = "storage/cache/";
        File file = new File(String.format(PATH));
        Conexion.modeAsynchrony = _asyn;
        // Reading directory contents
        File[] files = file.listFiles();

        for (int i = 0; i < files.length; i++) {
            String fl = String.valueOf(files[i]);
            fl = fl.substring(6,fl.length());
            //String[] dt = fl.split("cache\/");
            String key = new String(DatatypeConverter.parseBase64Binary(fl));
            if(key.contains("db:")){
                System.out.println("CARGANDO "+key);
            }
        }
        if(Conexion.modeAsynchrony)
        {
            new Timer().scheduleAtFixedRate(new TimerTask(){
                @Override
                public void run(){
                    if(Conexion.refreshReq<0){
                        Conexion.refreshReq++;
                        return;
                    }
                    Iterator it = Conexion.notifications.iterator();
                    //while(it.hasNext()){
                    NotificationDB notify = (NotificationDB)Conexion.notifications.get(Conexion.refreshReq);
                    //System.out.println("UPDATE "+notify.getName());
                    if(notify!=null){
                        System.out.println("UPDATE "+notify.getName());
                        notify.updateData();
                        //notify.updateChecksum(rq.get("checksum"));
                    }
                    //}

                    //System.out.println("Peticion :"+Conexion.refreshReq);
                    Conexion.refreshReq++;
                    if(Conexion.refreshReq==Conexion.notifications.size()) Conexion.refreshReq = 0;
                }
            },0,1000 * _time);
        }
        //;
    }

    public void setModeCache(boolean _asyn)
    {
        setModeCache(_asyn,10);
    }

    public void setModeCache()
    {
        setModeCache(false,10);
    }

}
