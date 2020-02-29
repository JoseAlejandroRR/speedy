package com.josealejandrorr.speedy.data.drivers;

import com.josealejandrorr.speedy.annotations.ModelEntity;
import com.josealejandrorr.speedy.contracts.data.repositories.Repository;
import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.data.EntityMapper;
import com.josealejandrorr.speedy.database.Conexion;
import com.josealejandrorr.speedy.data.entities.Model;
import com.josealejandrorr.speedy.utils.Builder;
import com.josealejandrorr.speedy.utils.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class MySqlDriverDatabase extends EntityMapper implements Repository {

    private String fieldIndex;

    private long key;

    private String tableName;

    private HashMap<String, Object> properties;

    private HashMap<String, String> collectionsMapper = new HashMap<>();

    private Connection connection;

    public boolean isConnected = false;

    protected PreparedStatement request;

    protected String requestSQL;

    protected Map<String,String> parameters = null;

    protected ResultSet resultSet;

    protected int affected_rows;

    public static boolean debugMode = false;

    public static boolean cacheMode = false;

    private ILogger logger;

    public MySqlDriverDatabase(ILogger logger)
    {
        this.logger = logger;
    }


    @Override
    public Optional<HashMap<String, Object>> findById(Model entity, long id) {

        String table = collectionsMapper.get(entity.getClass().getName());
        if (table == null) {
            logger.error("Table not defined for:" + entity.getClass().getName());
        }

        HashMap<String, Object> map = new HashMap<String, Object>();
        ModelEntity metaData = getModelMetaData(entity);
        if(id>0)
        {
            String sql = "SELECT "+table+".* FROM "+metaData.table()+" WHERE "+metaData.pkey()+" = ? LIMIT 1";
            Map<String, String> filters = new HashMap<String, String>();
            Map<String, String> obj = new HashMap<String, String>();

            filters.put(metaData.pkey(), String.valueOf(id));
            ArrayList<HashMap<String, Object>> data = resultSetToArrayList(query(sql, filters));

            if (data.size() > 0) {
                return Optional.of(data.get(0));
            }

            /*data.stream().findFirst().ifPresent(e -> {
                Annotation an = this.getClass().getAnnotation(ModelEntity.class);
                ((ModelEntity me = (ModelEntity) an;
                //this.key = (long) Long.parseLong(e.get(entity.in));
                Builder.setProperties(this, e);


            });*/

        }
        return Optional.of(null);
    }

    private  ArrayList<HashMap<String, Object>> resultSetToArrayList(ResultSet rs)
    {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        try {
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            while (rs.next()) {
                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                HashMap<String, Object> row = new HashMap<>();
                System.out.println(rs.getRow());
                for (int i = 1; i <= columns; ++i) {
                    System.out.println("-> "+md.getColumnTypeName(i));
                    System.out.println("class "+md.getColumnClassName(i));

                    switch(md.getColumnClassName(i))
                    {
                        case "TIMESTAMPS":
                            String date = String.valueOf(rs.getTimestamp(i));
                            date = date.replace(".0","");
                            row.put(md.getColumnName(i).toLowerCase(), date);
                            //System.out.println(md.getColumnName(i)+"="+date);
                            break;
                        case "java.lang.String":
                            row.put(md.getColumnName(i).toLowerCase(), rs.getString(i));
                            break;
                        case "java.lang.Integer":
                            row.put(md.getColumnName(i).toLowerCase(), rs.getInt(i));
                            break;
                        case "java.lang.Double":
                            row.put(md.getColumnName(i).toLowerCase(), rs.getDouble(i));
                            break;
                        case "java.lang.Boolean":
                            row.put(md.getColumnName(i).toLowerCase(), rs.getBoolean(i));
                            break;
                        default:
                            row.put(md.getColumnName(i).toLowerCase(), rs.getString(i));
                            //System.out.println(data.getString(i));
                            break;
                    }
                }
                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void setTimestamps(Repository model, Map<String, String> map)
    {
        /*if (map.containsKey("created_at")) {
            model.created_at = Builder.convertStringToDate(map.get("created_at"));
        }
        if (map.containsKey("updated_at")) {
            model.updated_at = Builder.convertStringToDate(map.get("updated_at"));
        }*/
    }

    @Override
    public boolean create(Model entity) {
        int success = 0;
        String fieldsSql = "";
        String valuesSql = "";

        List<String> fields = new ArrayList<String>();
        List<String> values = new ArrayList<String>();

        this.properties.remove(fieldIndex);

        Iterator iter = this.properties.keySet().iterator();

        while(iter.hasNext())
        {
            String k = (String) iter.next();
            if(!fieldIndex.equals(k)){
                String value = this.properties.get(k).toString();
                fields.add(k);
                values.add("?");
                fieldsSql+= k+",";
                valuesSql+= "?,";
            }
        }

        fieldsSql = String.join(",", fields);
        valuesSql = String.join(",", values);

        String sql = "INSERT INTO "+tableName+" ("+fieldsSql+") VALUES ("+valuesSql+")";

        Map<String, String> map = convertModelMapToPlainMap(createMapFromObject(this));

        return executeRequest(sql, map);
    }

    @Override
    public boolean update(Model entity) {
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
        String sql = "UPDATE "+this.tableName+" SET "+updateSql+" WHERE "+this.tableName+"."+this.fieldIndex+" = "+this.key+" LIMIT 1";

        Map<String, String> map = convertModelMapToPlainMap(createMapFromObject(this));
        return executeRequest(sql, map);
    }

    @Override
    public boolean delete(Model entity) {
        return false;
    }

    @Override
    public long count(Model entity) {
        return 0;
    }

    @Override
    public void registerModel(Model entity, String nameCollection) {
        if (!collectionsMapper.containsValue(nameCollection)) {
            System.out.println(entity.getClass().getName());
            collectionsMapper.put(entity.getClass().getName(), nameCollection);
        }
    }


    protected boolean openConnection()
    {
        boolean result = false;
        try{
            connection = DriverManager.getConnection(Conexion.url,Conexion.user,Conexion.pass);
            if(connection!=null)
            {
                result = true;
                this.isConnected = true;
            } else {
                logger.error("-- Error en la Conexion --");
            }
        } catch(SQLException e){
            //System.out.println(e);
            logger.error("-- ERROR:1 %s", e.getMessage());
        }
        return result;
    }


    private ResultSet requestReader()
    {
        ResultSet data = null;
        try{
            data = this.request.executeQuery();
        } catch(SQLException error)
        {
            logger.error("-- ERROR requestReader: "+error.getMessage()+" --");
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
            logger.error("-- ERROR: "+error.getMessage()+" --");
        }
        return affected;
    }

    public boolean executeRequest(String sql, Map<String, String> params)
    {
        boolean success = false;

        if(sql.length()<1) return success;

        try{
            if(!this.isConnected) this.openConnection();

            parameters = params;
            request = this.connection.prepareStatement(sql);

            Iterator itte = this.parameters.keySet().iterator();
            ParameterMetaData pmd = this.request.getParameterMetaData();
            int n = 1;
            while(itte.hasNext())
            {
                String key = (String) itte.next();
                sql = sql.replaceFirst("[?]", "'"+this.parameters.get(key)+"'");

                request.setString(n, parameters.get(key));

                n++;
            }

            this.requestSQL = sql;
            //this.showConsole("-- Start Query --\n");
            logger.debug("-- "+sql+" --\n");
            //this.showConsole("-- End Query --\n");
            String evaluate = sql.toLowerCase();
            //return true;
            if(evaluate.startsWith("select")){
                resultSet = this.requestReader();
            }
            if(evaluate.startsWith("insert ") || evaluate.startsWith("update ") || evaluate.startsWith("delete "))
            {
                this.affected_rows = this.requestNonQuery();
            }
            success = true;
            this.closeConnection();

        } catch(Exception error)
        {
            logger.error("-- ERROR: "+error.getMessage()+" --\n");
        }

        return success;
    }

    public ResultSet query(String sql)
    {
        Map<String, String> params = new HashMap<String,String>();
        return this.query(sql, params);
    }

    public ResultSet query(String sql, Map<String, String> params)
    {
        logger.debug("-- Query() --");
        this.executeRequest(sql, params);
        return resultSet;
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
    }

}
