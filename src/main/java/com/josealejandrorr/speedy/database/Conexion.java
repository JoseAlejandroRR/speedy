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
