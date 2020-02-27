package com.josealejandrorr.speedy.database;

import com.josealejandrorr.speedy.cache.Cache;
import com.josealejandrorr.speedy.utils.Builder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;


public class CacheDB {

    private static DB db = new DB();

    public static ArrayList requests = new ArrayList();
    public static String requestSql = null;

    public static ArrayList loadRequest(Class clazz,String sql)
    {
        boolean load = true;
        boolean ckDiff = false;
        ArrayList<Map<String, String>> cacheReq = new ArrayList();
        ArrayList cacheData = new ArrayList();
        ArrayList results = new ArrayList();
        Map<String, String> rq = new HashMap<String,String>();
        rq.put("query", sql);
        rq.put("checksum", "");
        rq.put("hashCode", String.valueOf(sql.hashCode()));
        Model obj = null;
        try {
            obj = (Model) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            String seltemp = sql;
            String[] sq = sql.split("FROM");
            String sqlCk = "SELECT SUM("+obj.table+".updated_at) as up FROM "+sq[1];
            CacheDB.requestSql = sql;
            //System.out.println("-- SQL CK:"+sqlCk+" --");
            //Helpers.startClock("Checking Checksum");
            ResultSet data = CacheDB.db.query(sqlCk);
            while(data.next()){
                //System.out.println("CHECKSUM ="+data.getLong(1));
                rq.put("checksum", String.valueOf(data.getLong(1)));
            }
            //Helpers.endClock();
            //Helpers.startClock("Checking If Cache Exist");
            if(Cache.hasKey("db:"+rq.get("hashCode"))){
                cacheReq = Cache.get("db:"+rq.get("hashCode"));
                for(int i = 0; i < cacheReq.size(); i++)
                {
                    //System.out.println(rq.get("checksum")+" = "+cacheReq.get(i).get("checksum"));
                    if(rq.get("checksum").equals(cacheReq.get(i).get("checksum"))){
                        load = false;
                        //System.out.println("CONSULTA "+sql.hashCode());
                    } else {
                        ckDiff = true;
                        cacheReq.get(i).put("checksum",rq.get("checksum"));
                        //System.out.println("Actualizando cache con "+rq.get("hashCode"));
                        //System.out.println("SQ:"+rq.get("query"));
						/*if(Conexion.modeNotifitations){
							NotificationDB notify = NotificationDB.find(rq.get("hashCode"));
							//System.out.println("UPDATE "+notify.getSql());
							if(notify!=null){
								//System.out.println("UPDATE2 "+rq.get("hashCode"));
								notify.updateChecksum(rq.get("checksum"));
							}
						}*/
                    }
                }
            }
            //Helpers.endClock();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(load){
            //CacheDB.requests.add(rq);
            //Helpers.startClock("Search in DB");
            ResultSet rs = CacheDB.db.query(sql);
            //Helpers.endClock();
            if(!ckDiff){
                cacheReq.add(rq);
            } else {
                cacheReq.remove(0);
                cacheReq.add(rq);
            }
            results = Builder.resultSetToArrayList(rs);
            Cache.add("db:"+rq.get("hashCode"),cacheReq);
            Cache.add("db:"+rq.get("hashCode")+":data",results);
            //System.out.println("Creando cache con "+rq.get("hashCode"));
        } else {
            //Helpers.startClock("Search Last Cache");
            //System.out.println("BUSCANDO = "+rq.get("checksum"));
            ArrayList<Map<String, String>> list = Cache.get("db:"+rq.get("hashCode")+":data");
            results = list;
            //Helpers.endClock();
        }
        //System.out.println("-----------------------------------");
        return results;
    }

}
