package com.josealejandrorr.speedy.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DB{

    private String db = "inventario";
    private String user = "root";
    private String pass = "";
    private String url = "jdbc:mysql://localhost/"+db+"?generateSimpleParameterMetadata=true&zeroDateTimeBehavior=convertToNull";

    public static Connection connection;

    public boolean isConnected = false;

    protected PreparedStatement request;

    protected String requestSQL;

    protected Map<String,String> parameters = null;

    protected ResultSet data;

    protected int affected_rows;

    public static boolean debugMode = false;
    public static boolean cacheMode = false;

    public ArrayList requests = new ArrayList();

    public DB()
    {

    }

    protected boolean openConnection()
    {
        boolean result = false;
        try{
            //System.out.printf("Connecting %s %s %s\n",Conexion.user, Conexion.pass, Conexion.url);
           // Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(Conexion.url,Conexion.user,Conexion.pass);
            if(this.connection!=null)
            {
                result = true;
                this.isConnected = true;
                //this.showConsole("-- Conexion Establecida --\n");
            } else {
                showConsole("-- Error en la Conexion --\n");
            }
        } catch(SQLException e){
            //System.out.println(e);
            System.out.printf("-- ERROR:1 %s\n", e.getMessage());
        } /*catch(ClassNotFoundException e){
            //System.out.println(e);
            System.out.printf("-- DRIVE NOT FOUND ERROR: %s\n", e.getMessage());
        } */
        catch(Exception e){
            //System.out.println(e);
            System.out.printf("-- ERROR:2 %s\n", e.getMessage());
        }
        return result;
    }

    private void showConsole(String txt)
    {
        if(Conexion.modeDebug) System.out.printf("%s\n", txt);
    }

    protected void closeConnection()
    {
        /*try {
            this.connection.close();
        } catch (SQLException e) {
            this.showConsole("Error Close Connection: " + e.getMessage());
            e.printStackTrace();
        }*/
        this.isConnected = false;
        this.connection = null;
        //this.showConsole("-- Conexion Cerrada --\n");
    }

    private ResultSet requestReader()
    {
        ResultSet data = null;
        try{
            //Helpers.startClock("Search in DB");
            data = this.request.executeQuery();
            //Helpers.endClock();
            //this.showConsole("-- SQL: "+this.requestSQL+" --");
        } catch(SQLException error)
        {
            this.showConsole("-- ERROR requestReader: "+error.getMessage()+" --");
        }
        return data;
    }

    private int requestNonQuery()
    {
        int affected = 0;
        try{
            affected = this.request.executeUpdate();
        } catch(SQLException error)
        {
            this.showConsole("-- ERROR: "+error.getMessage()+" --\n");
        }
        return affected;
    }

    public boolean executeRequest(String sql, Map<String, String> params)
    {
        boolean success = false;

        if(sql.length()<1) return success;

        try{
            if(this.isConnected==false) this.openConnection();

            //parameters = new HashMap<String, String>();
            parameters = params;
            request = this.connection.prepareStatement(sql);

            Iterator itte = this.parameters.keySet().iterator();
            ParameterMetaData pmd = this.request.getParameterMetaData();
            int n = 1;
            while(itte.hasNext())
            {
                String key = (String) itte.next();
                sql = sql.replaceFirst("[?]", "'"+this.parameters.get(key)+"'");
                //System.out.println(key+"="+this.parameters.get(key));
                request.setString(n, parameters.get(key));

                //System.out.println(pmd.getParameterClassName(n));
                n++;
            }
            this.requestSQL = sql;
            //this.showConsole("-- Start Query --\n");
            this.showConsole("-- "+sql+" --\n");
            //this.showConsole("-- End Query --\n");
            String evaluate = sql.toLowerCase();
            //return true;
            if(evaluate.contains("select")){
                this.data = this.requestReader();

            }
            if(evaluate.contains("insert ") || evaluate.contains("update ") || evaluate.contains("delete "))
            {
                this.affected_rows = this.requestNonQuery();
            }
            success = true;
            this.closeConnection();

        } catch(Exception error)
        {
            this.showConsole("-- ERROR: "+error.getMessage()+" --\n");
        }

        return success;
    }

	/*public boolean loadRequest(String sql)
	{
		boolean load = true;
		Map<String, String> rq = new HashMap<String,String>();
		rq.put("query", sql);
		rq.put("checksum", "");
		rq.put("hashCode", String.valueOf(sql.hashCode()));
		//System.out.println(sql.hashCode()+" = "+this.requests.size());
		try {
			String seltemp = sql;
			String[] sq = sql.split("FROM");
			String sqlCk = "SELECT SUM(birthday) FROM "+sq[1];
			this.requestSQL = sqlCk;
			request = this.connection.prepareStatement(sqlCk);
			//this.showConsole("-- SQL CK: SELECT SUM(birthday) FROM "+sq[1]+" --");
			ResultSet data = this.requestReader();
			while(data.next()){
				System.out.println("CHECKSUM ="+data.getInt(1));
				rq.put("checksum", String.valueOf(data.getInt(1)));
			}
			Iterator it = this.requests.iterator();
			while(it.hasNext())
			{
				Map<String, String> obj = (Map<String,String>) it.next();
				System.out.println(obj.get("checksum")+" = "+rq.get("checksum"));
				if(obj.get("hashCode").equals(rq.get("hashCode"))){
					if(obj.get("checksum").equals(rq.get("checksum"))){
						load = false;
						//System.out.println("CONSULTA "+sql.hashCode());
					}
				}
			}
			this.requestSQL = sql;
			request = this.connection.prepareStatement(sql);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(load){
			this.requests.add(rq);
			Cache cache = new Cache();
			//cache.add(key, rs, obj);
			System.out.println("SI Cargo");
		} else {
			System.out.println("NO Cargo");
		}

		return load;
	}*/



    public ResultSet query(String sql, Map<String, String> params)
    {
        this.showConsole("-- Query() --\n");
        this.executeRequest(sql, params);
        return data;
    }

    public ResultSet query(String sql)
    {
        Map<String, String> params = new HashMap<String,String>();
        this.showConsole("-- Query() --\n");
        this.executeRequest(sql, params);
        return data;
    }

    public int nonQuery(String sql, Map<String, String> params)
    {
        //System.out.println("nonQuery:" +sql);
        this.executeRequest(sql, params);
        return this.affected_rows;
    }

    public void addParams(String key, String value)
    {
        this.parameters.put(key, value);
    }

    public void addParams(Map<String, String> data)
    {
        Iterator iter = data.keySet().iterator();
		/*for(int i = 0; i < data.size(); i++)
		{
			this.addParams(i, data.get(i));
		}*/
        while(iter.hasNext())
        {
            String key = (String) iter.next();
            this.addParams(key, data.get(iter.next()));
        }
    }

    public ResultSet table(String table, Map<String, String> params)
    {
        this.query("SELECT * FROM "+table, params);
        return this.data;
    }

    public String getKey(String sql, Map<String, String> params)
    {
        //System.out.println("getKEY: "+sql);
        Map<String, String> obj = new HashMap<String, String>();
        this.executeRequest(sql, obj);
        try {
            while(this.data.next())
            {
                ///System.out.println("KEY = "+data.getInt(1));
                return String.valueOf(data.getInt(1));
            }
        } catch (SQLException e) {
            this.showConsole("ERROR: "+e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }
}
