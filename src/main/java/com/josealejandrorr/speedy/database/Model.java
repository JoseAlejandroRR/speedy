package com.josealejandrorr.speedy.database;

import com.josealejandrorr.speedy.annotations.ModelEntity;
import com.josealejandrorr.speedy.utils.Builder;
import com.josealejandrorr.speedy.utils.Logger;
import com.sun.org.apache.xpath.internal.operations.Mod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Deprecated
public abstract class Model {

    private DB db;

    protected String table;

    protected String pkey;

    protected int key;

    private Map<String, String> properties;

    private String sqlRequest = null;

    protected int limit_from = -1;

    protected int limit_many = 0;

    protected String limits = "";

    protected ArrayList<String> tables = new ArrayList<String>() ;

    protected String filters = null;

    protected String orders = null;

    protected String fieldsSelected = null;

    protected boolean hasTimestamps = false;

    protected ArrayList results = new ArrayList();

    public Date created_at;

    public Date updated_at;

    Class<? extends Model> cl = this.getClass();

    public Model()
    {
        db = new DB();
        this.properties = new HashMap<String, String>();
        this.limits = "";
        this.tables.clear();
        this.filters = "";
        this.fieldsSelected = "";
        this.sqlRequest = "";
        this.orders = "";


        Class c = this.getClass();
        Annotation an = c.getAnnotation(ModelEntity.class);
        ModelEntity me = (ModelEntity) an;
        if (me != null){
            this.table = me.table();
            this.pkey = me.pkey();
            hasTimestamps = me.timestamps();
        } else {
            Logger.getLogger().error("@ModelEntity must used in " + c.getName());
        }
    }

    public void setKey(int k)
    {
        this.key = k;
    }


    public boolean create()
    {
        int success = 0;
        String fieldsSql = "";
        String valuesSql = "";
        System.out.println("-- Create() --\n");

        List<String> fields = new ArrayList<String>();
        List<String> values = new ArrayList<String>();

        this.properties.remove(this.pkey);

        Iterator iter = this.properties.keySet().iterator();

        while(iter.hasNext())
        {
            String k = (String) iter.next();
            //System.out.println(k);
            //System.out.println(this.pkey+"="+k);
            if(!this.pkey.equals(k)){
                String value = this.properties.get(k);
                fields.add(k);
                values.add("?");
                fieldsSql+= k+",";
                valuesSql+= "?,";
                //valuesSql+= "@"+value+",";
            }

        }

        fieldsSql = String.join(",", fields);
        valuesSql = String.join(",", values);
        String sql = "INSERT INTO "+this.table+" ("+fieldsSql+") VALUES ("+valuesSql+")";
        //System.out.println(sql);
        success = this.db.nonQuery(sql, this.properties);

        return (success == 1) ? true : false;
    }

    private int update()
    {
        int success = 0;
        String updateSql = "";
        System.out.println("-- update() --");

        this.properties.remove("created_at");

        Iterator itte = this.properties.keySet().iterator();

        while(itte.hasNext())
        {
            String key = (String) itte.next();
            //System.out.println(key+"="+this.properties.get(key));
            //updateSql += key+"=@"+key+",";
            updateSql += key+"= ?, ";
        }

        //this.properties.put(this.pkey, String.valueOf(this.key));
        updateSql = updateSql.substring(0, updateSql.length()-2);
        String sql = "UPDATE "+this.table+" SET "+updateSql+" WHERE "+this.table+"."+this.pkey+" = "+this.key+" LIMIT 1";
        success = this.db.nonQuery(sql, this.properties);
        //System.out.println(sql);
        return success;
    }

    public int update(Map<String, String> params)
    {
        int success = 0;
        String updateSql = "";
        System.out.println("-- update() --");

        this.properties.remove("created_at");

        Iterator itte = params.keySet().iterator();

        while(itte.hasNext())
        {
            String key = (String) itte.next();
            //System.out.println(key+"="+params.get(key));
            updateSql += key+"= ?, ";
        }
        updateSql = updateSql.substring(0, updateSql.length()-2);
        this.makeSql();
        String sql = String.format("UPDATE %s SET %s %s %s %s",this.table,updateSql,this.filters,this.orders,this.limits);
        //String sql = "UPDATE "+this.table+" SET "+updateSql+" WHERE "+this.table+"."+this.pkey+" = "+this.key+" LIMIT 1";
        //System.out.printf("SQL: %s\n", sql);
        success = this.db.nonQuery(sql, params);
        this.sqlRequest = "";
        destroySql();
        return success;
    }

    public int delete()
    {
        int success = 0;
        String deleteSql = "";
        System.out.println("-- delete() --");

		/*this.properties.remove("created_at");

		Iterator itte = ids.iterator();

		while(itte.hasNext())
		{
			int key = (int) itte.next();
			System.out.println(key+"="+key);
			deleteSql += String.format("id = %d AND ",key);
		}
		deleteSql = deleteSql.substring(0, deleteSql.length()-4);*/
        this.makeSql();
        String sql = String.format("DELETE FROM %s %s %s %s %s",this.table,deleteSql,this.filters,this.orders,this.limits);
        //String sql = "UPDATE "+this.table+" SET "+updateSql+" WHERE "+this.table+"."+this.pkey+" = "+this.key+" LIMIT 1";
        if(sql.trim().equals(String.format("DELETE FROM %s", this.table))){
            return 0;
        }
        //System.out.printf("SQL: %s\n", sql);
        success = this.db.nonQuery(sql, this.properties);
        this.sqlRequest = "";
        destroySql();
        return success;
    }

    public void save()
    {
        this.bindProperties(this);
        //System.out.println("SAVE WITH ID = "+this.key);
        if(this.key>0)
        {
            update();
        } else {
            create();
            last();
        }
    }

    public Model find(int id)
    {
        if(id>0)
        {
            String sql = "SELECT "+this.table+".* FROM "+this.table+" WHERE "+this.pkey+" = ? LIMIT 1";
            Map<String, String> filters = new HashMap<String, String>();
            Map<String, String> obj = new HashMap<String, String>();

            filters.put(this.pkey, String.valueOf(id));
            ResultSet data = this.db.query(sql, filters);
            try {
                int n = 0;
                while(data.next())
                {
                    Annotation an = this.getClass().getAnnotation(ModelEntity.class);
                    ModelEntity me = (ModelEntity) an;
                    this.key = data.getInt(1);
                    obj = createObjectProperties(data);

                    Builder.setProperties(this,obj);
                    if (me.timestamps()){
                        setTimestamps(this,obj);
                    }
                    //System.out.println("SALIENDO FINE");
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return this;
    }

	/*public int delete()
	{
		String sql = String.format("DELETE FROM %s WHERE %s = '%d'",this.table,this.pkey,this.key);
		return this.db.nonQuery(sql, this.properties);
	}*/

    public int destroy(int n)
    {
        String sql = String.format("DELETE FROM %s WHERE %s = '%d'",this.table,this.pkey,n);
        return this.db.nonQuery(sql, this.properties);
    }

    public void destroy(int n[])
    {
        for(int i = 0; i < n.length; i++)
        {
            this.destroy(n[i]);
        }
    }

    public void startQuery()
    {
        //System.out.println("--- "+this.sqlRequest+" ---<");
        if(this.sqlRequest.equals(""))
        {
            this.limits = "";
            this.tables.clear();
            this.filters = "";
            this.fieldsSelected = "";
            //this.orders = "ORDER BY "+this.table+"."+this.pkey+" ASC";
            this.sqlRequest = "";
            this.orders = String.format("ORDER BY %s.%s ASC",this.table,this.pkey);
            this.sqlRequest = String.format("SELECT * FROM %s WHERE %s",this.table,this.orders);
        }
    }

    public Model where(String field, String condition, String value)
    {
        startQuery();
        String filter = formatFilter(field,condition,value,false);
        String con = "";
        if(!this.filters.equals(""))
        {
            con = " AND ";
        }
        this.filters += con+filter;
        //System.out.printf("%s %s %s", field,condition,value);
        return this;
    }

    public Model where(String filters[][])
    {
        startQuery();
        for(int i = 0; i < filters.length; i++)
        {
            this.where(filters[i][0],filters[i][1],filters[i][2]);
        }
        return this;
    }

    public Model whereRaw(String filter)
    {
        startQuery();
        if(filter=="") return this;
        String con = "";
        if(!this.filters.equals(""))
        {
            con = " AND ";
        }
        this.filters = con+filter;
        return this;
    }

    public Model orWhere(String field, String condition, String value)
    {
        startQuery();
        String filter = formatFilter(field,condition,value,false);
        String con = "";
        if(!this.filters.equals(""))
        {
            con = " OR ";
        }
        this.filters += con+filter;
        return this;
    }

    public Model join(String table,String field, String condition, String value)
    {
        startQuery();
        String con = "";
        String join = formatFilter(field,condition,value,true);
        //System.out.println("Existe con "+this.filters);
        if(!this.filters.equals(""))
        {
            con = " AND ";
        }
        this.filters = this.filters+con+join;
        boolean have = false;
        for(String tb : this.tables)
        {
            if(!tb.equals(table)){
                this.tables.add(table);
                System.out.println("Agrego");
            } else {
                System.out.println("NO Agrego");
            }
            have = true;
        }
        if(have==false) this.tables.add(table);
		/*if(!this.table.contains(table))
		{
			this.tables += ", "+table;
		}*/
        return this;
    }

    public ArrayList get()
    {
        ArrayList lista = new ArrayList();
        this.results = this.getResult();
        lista = this.setResult(this.getClass());

        return lista;
    }

    public ArrayList setResult(Class clazz)
    {
        ArrayList lista = new ArrayList();
        ArrayList data = this.results;
        //ResultSet data = this.results;
        Iterator it = data.iterator();

        if (data.size() < 1) {
           return lista;
        }

        Annotation an = clazz.getAnnotation(ModelEntity.class);
        ModelEntity me = (ModelEntity) an;

        while(it.hasNext())
        {
            Map<String, String> item = (Map<String, String>)it.next();
            try {
                Model obj = (Model) clazz.newInstance();
                obj.key = Integer.parseInt(item.get("id"));
                //this.key = data.getInt(1);
                Builder.setProperties(obj,item);
                if(me.timestamps()) {
                    setTimestamps(obj,item);
                }
                //System.out.printf("KEY =  %s,\n",obj.key);
                //System.out.print(data.getInt(1));
                lista.add(obj);
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return lista;
    }


    public ArrayList getResult()
    {
        //ResultSet data = null;
        if(!Conexion.modeCache)
        {
            this.results = Builder.resultSetToArrayList(this.db.query(this.makeSql(),this.properties));

        } else {
            this.results = CacheDB.loadRequest(this.getClass(), this.makeSql());
        }

        this.sqlRequest = "";
        destroySql();
        return this.results;
    }

    public ResultSet getQueryRaw(String sql)
    {
        //ResultSet data = null;
        return this.db.query(sql,this.properties);
    }

    public ArrayList get(String fields[])
    {
        for(int i = 0; i < fields.length; i++)
        {
            select(fields[i]);
        }
        return get();
    }

    public ArrayList hasMany(Class clazz,String keyForeign)
    {
        ArrayList results = new ArrayList();
        //clazz = Class.forName(this.getClass().getName());
        Model obj = null;
        try {
            obj = (Model) clazz.newInstance();
            //System.out.printf("TABLE %s KEY %s\n",obj.table,obj.pkey);
            //String sqlq = "SELECT "+joinT+".* FROM "+this.table+", "+joinT+" WHERE "+joinT+"."+keyForeign+" = "+this.table+"."+this.pkey+" AND "+this.table+"."+this.pkey+" = "+this.key+"";
            this.join(obj.table,obj.table+"."+keyForeign, "=", String.valueOf(this.table+"."+this.pkey)).select(obj.table+".*").where(this.table+"."+this.pkey,"=", String.valueOf(this.key));
            this.results = this.getResult();
            results = this.setResult(clazz);
            //this.sqlRequest = "";
            destroySql();

        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //setProperties(obj,createObjectProperties(data));
        return results;
    }

    public Model hasOne(Class clazz, String keyForeign)
    {
        //System.out.printf("SUPERCLASS2 %s,\n",this.getClass().getName());
        //System.out.printf("Saliendo con %s %s\n", this.table,this.key);
        Model obj = null;
        try {
            obj = (Model) clazz.newInstance();
            Annotation an = obj.getClass().getAnnotation(ModelEntity.class);
            ModelEntity me  = (ModelEntity) an;
            //System.out.printf("TABLE %s KEY %s\n",obj.table,obj.pkey);
            this.join(obj.table,obj.table+"."+keyForeign, "=", String.valueOf(this.table+"."+this.pkey)).select(obj.table+".*").where(this.table+"."+this.pkey,"=", String.valueOf(this.key)).take(1);
            this.results = this.getResult();
            //ResultSet data = this.results;
            if(this.results.size()>0){
                Map<String, String> dt = (Map<String, String>)this.results.get(0);
                obj.key = Integer.parseInt(dt.get("id"));
                Builder.setProperties(obj, dt);

                if (me.timestamps()) {
                    setTimestamps(obj,dt);
                }
            } else {
                obj = null;
            }
            //this.sqlRequest = "";
            destroySql();
        } catch (InstantiationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        //this = this.get().get(0);
        return obj;
    }

    public Model belongsTo(Class clazz, String keyForeign)
    {
        //System.out.printf("--: %d\n",this.key);
        Model obj = null;
        ResultSet data = null;
        try {
            obj = (Model) clazz.newInstance();
            this.join(obj.table,obj.table+"."+obj.pkey, "=", String.valueOf(this.table+"."+keyForeign)).select(obj.table+".*").where(this.table+"."+this.pkey,"=", String.valueOf(this.key)).take(1);
            this.results = this.getResult();
            //data = this.results;
        } catch (InstantiationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if(this.results.size()>0){
            Annotation an = clazz.getAnnotation(ModelEntity.class);
            ModelEntity me = (ModelEntity) an;
            Map<String, String> dt = (Map<String, String>)this.results.get(0);
            obj.key = Integer.parseInt(dt.get("id"));
            Builder.setProperties(obj, dt);
            if (me.timestamps()){
                setTimestamps(obj, dt);
            }
        } else {
            obj = null;
        }
        //this.sqlRequest = "";
        destroySql();
        return obj;
    }

    public Model select(String field)
    {
        String con = "";
        if(!this.fieldsSelected.equals(""))
        {
            con = ", ";
        }
        this.fieldsSelected += con+field;
        return this;
    }

    public Model select(String fields[])
    {
        for(int i = 0; i < fields.length; i++)
        {
            select(fields[i]);
        }
        return this;
    }

    public Model take(int from)
    {
        if(from < 0) return this;
        this.limit_many = from;
        parserLimit();
        return this;
    }

    public Model skip(int skip)
    {
        if(skip < 0) return this;
        this.limit_from = skip;
        parserLimit();
        return this;
    }

    public Model orderBy(String field, String order)
    {
        if(this.orders.contains(String.format("ORDER BY %s.%s ASC",this.table,this.pkey))){
            this.orders = "ORDER BY "+field+" "+order;
        } else {
            this.orders += ", "+field+" "+order;
        }
        return this;
    }

    public Model orderBy(String field)
    {
        orderBy(field,"ASC");
        return this;
    }

    public ArrayList<Map<String, String>> all()
    {
        if(this.filters.equals(""))
        {
            return Builder.resultSetToArrayList(this.db.table(this.table, null));
        } else {
            return Builder.resultSetToArrayList(this.db.table(this.table, null));
        }

    }

    public Model first()
    {

        if(this.filters.equals(""))
        {
            Double value =  firstKey();

            int f = value.intValue();
            return this.find(f);
        } else {
            //System.out.println("fist2");
            this.limits = "LIMIT 1";
            this.sqlRequest += " LIMIT 1";
            this.results = this.getResult();
            if(this.results.size()>0){
                Map<String, String> dt = (Map<String, String>)this.results.get(0);
                this.key = Integer.parseInt(dt.get("id"));
                Builder.setProperties(this, dt);
                //setTimestamps(this,dt);
            }
        }
        return this;
    }

    public Model last()
    {
        if(this.filters.equals(""))
        {
            Double value = lastKey();
            int f = value.intValue();
            return this.find(f);
        } else {
            this.limits = "LIMIT 1";
            this.orders = " ORDER BY "+this.pkey+" DESC";
            this.results = this.getResult();
            if(this.results.size()>0){
                Map<String, String> dt = (Map<String, String>)this.results.get(0);
                this.key = Integer.parseInt(dt.get("id"));
                Builder.setProperties(this, dt);
                //setTimestamps(this,dt);
            }
        }
        return this;
    }

    public double firstKey()
    {
        //System.out.println("SELECT "+this.pkey+" FROM " + this.table+" ORDER BY "+this.pkey+" ASC LIMIT 1");
        return loadValue("SELECT "+this.pkey+" FROM " + this.table+" ORDER BY "+this.pkey+" ASC LIMIT 1");
    }

    public double lastKey()
    {
        return loadValue("SELECT " + this.pkey + " FROM " + this.table + " ORDER BY " + this.pkey + " DESC LIMIT 1");
    }
    public double min(String field)
    {
        return loadValue("SELECT min(" + field + ")" + " FROM " + this.table);
    }

    public double max(String field)
    {
        return loadValue("SELECT max(" + field + ")" + " FROM " + this.table);
    }

    public double avg(String field)
    {
        return loadValue("SELECT avg(" + field + ")" + " FROM " + this.table);
    }

    public double sum(String field)
    {
        return loadValue("SELECT sum(" + field + ")" + " FROM " + this.table);
    }

    public double count(String field, int limit)
    {
        if (limit > 0)
        {
            return loadValue("SELECT count(" + field + ")" + " FROM " + this.table + " "+this.filters+" LIMIT " + limit);
        }

        return loadValue("SELECT count(" + field + ")" + " FROM " + this.table+ " "+this.filters);
    }
    public double count(String field)
    {
        return count(field,0);
    }

    private double loadValue(String sql)
    {
        //System.out.println("ERROR AQUI2: "+sql);
        String value = this.db.getKey(sql,this.properties);
        double result = -1;
        if(!value.equals(""))
        {
            try {

                result = Double.parseDouble(value);
            } catch(Exception error)
            {
                //return 0;
            }
        }
        return result;
    }

    private void parserLimit()
    {
        String limit = "";
        if(this.limit_many >= 0)
        {
            if(this.limit_from > 0)
            {
                limit = String.format(" LIMIT %d, %d",this.limit_from,this.limit_many);
            } else {
                limit = String.format(" LIMIT %d",this.limit_many);
            }
        }
        this.limits = limit;
    }

    private String formatFilter(String field, String condition, String value, boolean isField)
    {
        String filter = "";
        String comiInit = "";
        String comiEnd = "";
        if(!isField)
        {
            comiInit = "'";
            comiEnd = "'";
        }
        switch(condition.toLowerCase())
        {
            case "=":
                filter += String.format("%s=%s%s%s",field,comiInit,value,comiEnd);
                break;
            case "!=":
                filter += String.format("%s!=%s%s%s",field,comiInit,value,comiEnd);
                break;
            case "<":
                filter += String.format("%s<%s%s%s",field,comiInit,value,comiEnd);
                break;
            case "<=":
                filter += String.format("%s<=%s%s%s",field,comiInit,value,comiEnd);
                break;
            case ">":
                filter += String.format("%s>%s%s%s",field,comiInit,value,comiEnd);
                break;
            case ">=":
                filter += String.format("%s>=%s%s%s",field,comiInit,value,comiEnd);
                break;
            case "like":
                filter += String.format("%s LIKE %s%%%s%%%s",field,comiInit,value,comiEnd);
                break;
        }
        return filter;
    }

    public void destroySql()
    {
        this.limit_from = -1;
        this.limit_many = 0;
        this.limits = "";
        this.filters = "";
        this.orders = "";
        this.fieldsSelected = "";
        this.tables.clear();
    }

    private String makeSql()
    {
        String tabless = "";

        if(this.fieldsSelected.equals(""))
        {
            this.fieldsSelected = "*";
        }

        if(this.filters.equals(""))
        {
            this.filters = "";
        } else {
            this.filters = "WHERE "+this.filters;
        }

        for(String tb : this.tables)
        {
            tabless = tabless+","+tb;
        }

		/*if(this.tables.equals(""))
		{
			tabless = "";
		}
		*/
        tabless = this.table+tabless;
        if(this.limits.equals(""))
        {
            this.limits = "";
        }
        String sql = String.format("SELECT %s FROM %s %s %s %s",this.fieldsSelected,tabless,this.filters,this.orders,this.limits);
        //System.out.println("SQL: "+sql+"\n");
        return sql;
    }

    public String getSql()
    {
        String sql = this.makeSql();
        this.destroySql();
        this.sqlRequest = "";
        return sql;
    }

    private <T> void bindProperties(Object obj)
    {
        Map<String, Object> temp = new HashMap<String, Object>();
        Class<?> objClass = obj.getClass();
        Field[] fields  = objClass.getDeclaredFields();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for( Field field : fields ){
            //System.out.println(field.getType());
            boolean addField = false;
            try {
                field.setAccessible(true);
                if(field.getType().toString().contains("int"))
                {
                    addField = true;
                } else if(field.getType().toString().toLowerCase().contains("string"))
                {
                    addField = true;
                } else if(field.getType().toString().toLowerCase().contains("double"))
                {
                    addField = true;
                } else if(field.getType().toString().toLowerCase().contains("decimal"))
                {
                    addField = true;
                } else if(field.getType().toString().toLowerCase().contains("float"))
                {
                    addField = true;
                } else if(field.getType().toString().toLowerCase().contains("date"))
                {
                    addField = true;
                } else if(field.getType().toString().toLowerCase().contains("boolean"))
                {
                    addField = true;
                }
				/*System.out.println("Name "+field.getName());
				System.out.println("Public "+field.PUBLIC);
				System.out.println("Declared "+field.toGenericString());
				System.out.println("--------------------");*/
                //field.toGenericString().contains("public")
                if(addField)
                {

                    String value = String.valueOf(field.get(obj));
                    if(value.equals("null")) value = "";
                    if(field.getType().toString().contains("Date")){
                        if(field.get(obj)!=null){
                            value = dt.format(field.get(obj));
                        }
                    }
                    //System.out.println(field.getType());
                    //System.out.printf("PARAMETER:  %s => %s \n",field.getName(),value);
                    this.properties.put(field.getName(), value);
                } else {
                    //System.out.println("NO");
                }

            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                System.out.println("-- ERROR bindProperties: "+e.toString()+" --\n");
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                System.out.println("-- ERROR bindProperties: "+e.toString()+" --\n");
                e.printStackTrace();
            }
            this.properties.remove("timestamps");
			/*Iterator itte = this.properties.keySet().iterator();
			while(itte.hasNext())
			{
				String key = (String) itte.next();
				System.out.println(key+"="+this.properties.get(key));
			}*/
        }

        if(hasTimestamps){
            Date now = new Date();
            this.properties.put("created_at", dt.format(now));
            this.properties.put("updated_at",dt.format(now));
        }
    }

    private void setTimestamps(Model model, Map<String, String> map)
    {
        if (map.containsKey("created_at")) {
            model.created_at = Builder.convertStringToDate(map.get("created_at"));
        }
        if (map.containsKey("updated_at")) {
            model.updated_at = Builder.convertStringToDate(map.get("updated_at"));
        }
    }

    public void loadData(ResultSet data)
    {
        Builder.setProperties(this,createObjectProperties(data));
    }

    public void loadData(Map<String, String> data)
    {
        Builder.setProperties(this,data);
    }

    private Map<String, String> createObjectProperties(ResultSet data)
    {
        Map<String, String> obj = new HashMap<String, String>();
        try {
            ResultSetMetaData rsmd = data.getMetaData();
            //System.out.println("Total "+rsmd.getColumnCount());

            //while(data.next()){

            for(int i = 1; i <= rsmd.getColumnCount(); i++)
            {
                switch(rsmd.getColumnTypeName(i))
                {
                    case "TIMESTAMP":
                        String date = String.valueOf(data.getTimestamp(i));
                        obj.put(rsmd.getColumnName(i).toLowerCase(), date);
                        //System.out.println(rsmd.getColumnName(i)+"="+data.getTimestamp(i));
                        break;
                    default:
                        obj.put(rsmd.getColumnName(i).toLowerCase(), data.getString(i));
                        //System.out.println(data.getString(i));
                        break;
                }
            }

            //}
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return obj;
    }

    /*private <T> void setProperties(Object obj, Map<String, String> data)
    {
        //System.out.println("A");
        Class<?> objClass = obj.getClass();
        Field[] fields  = objClass.getDeclaredFields();

        Iterator itte = data.keySet().iterator();
        while(itte.hasNext())
        {
            String k = (String) itte.next();
            //System.out.println("itte: "+data.get(k));
            //System.out.println("itte: "+k);
        }

        for( Field field : fields ){

            try {
                //System.out.println("="+field.getName());
                //System.out.println(field.getName()+"="+data.get(field.getName()));
                if(field.getName().equals("id")) {
                    //this.key = Integer.parseInt(data.get("id"));
                    //System.out.println("SALIENDO CON "+data.get("id")+"\n");
                }
                if(data.containsKey(field.getName()))
                {
                    //System.out.println(field.getType().toString());
                    //System.out.println(field.getName()+"="+data.get(field.getName()));
                    field.setAccessible(true);
                    if(field.getType().toString().contains("int") || field.getType().toString().contains("Integer")) field.set(obj, Integer.parseInt(data.get(field.getName())));
                    if(field.getType().toString().contains("String")) field.set(obj, String.valueOf(data.get(field.getName())));
                    if(field.getType().toString().contains("double")) field.set(obj, Double.parseDouble(data.get(field.getName())));
                    if(field.getType().toString().contains("Date")){
                        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
                        try {

                            field.set(obj, dt.parse(data.get(field.getName())));
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            System.out.println("ERROR PARSE: "+field.getName());
                            e.printStackTrace();
                        }
                    }
                    //if(field.getType().toString().contains("")) field.set(obj, );
                    //if(field.getType().toString().contains("")) field.set(obj, );
                    switch(field.getType().toString())
                    {

                    }
                }

            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                System.out.println("-- ERROR setProperties: "+e.toString()+" --\n");
                e.printStackTrace();
            }
        }

        Annotation an = objClass.getAnnotation(ModelEntity.class);
        ModelEntity me = (ModelEntity) an;
        if(me.timestamps())
        {
            try {
                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                ((Model) obj).created_at = dt.parse(data.get("created_at"));
                ((Model) obj).updated_at = dt.parse(data.get("updated_at"));
            } catch (ParseException e) {
                System.out.println("ERROR TIMESTAMPS: "+e.getMessage());
                System.out.println("ERROR TIMESTAMPS: "+data.get("created_at"));
                // TODO Auto-generated catch block
                e.printStackTrace();

            }
        }
    }*/
}

