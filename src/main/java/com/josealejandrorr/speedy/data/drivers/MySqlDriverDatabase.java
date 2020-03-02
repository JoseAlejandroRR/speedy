package com.josealejandrorr.speedy.data.drivers;

import com.josealejandrorr.speedy.annotations.ModelEntity;
import com.josealejandrorr.speedy.contracts.data.repositories.DatabaseQuery;
import com.josealejandrorr.speedy.contracts.data.repositories.DatabaseRepository;
import com.josealejandrorr.speedy.contracts.data.repositories.Repository;
import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.data.EntityMapper;
import com.josealejandrorr.speedy.data.entities.EntityFilter;
import com.josealejandrorr.speedy.data.entities.FilterOperator;
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
import java.util.stream.Collectors;

public class MySqlDriverDatabase extends EntityMapper implements DatabaseRepository {

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
        HashMap<String, Object> map = new HashMap<String, Object>();
        ModelEntity metaData = getModelMetaData(entity);
        if(id>0)
        {
            String sql = "SELECT "+metaData.table()+".* FROM "+metaData.table()+" WHERE "+metaData.pkey()+" = ? LIMIT 1";
            Map<String, String> filters = new HashMap<String, String>();

            filters.put(metaData.pkey(), String.valueOf(id));
            ArrayList<HashMap<String, Object>> data = resultSetToArrayList(query(sql, filters));

            if (data.size() > 0) {
                return Optional.of(data.get(0));
            }

        }
        return Optional.ofNullable(null);
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
        String fieldsSql = "";
        String valuesSql = "";

        List<String> fields = new ArrayList<String>();
        List<String> values = new ArrayList<String>();

        Map<String, String> map = convertModelMapToPlainMap(createMapFromObject(entity));

        ModelEntity metaData = getModelMetaData(entity);
        map.remove(metaData.pkey());
        Iterator iter = map.keySet().iterator();

        while(iter.hasNext())
        {
            String k = (String) iter.next();
            if(!metaData.pkey().equals(k)){
                String value = map.get(k).toString();
                fields.add(k);
                values.add("?");
                fieldsSql+= k+",";
                valuesSql+= "?,";
            }
        }

        fieldsSql = String.join(",", fields);
        valuesSql = String.join(",", values);

        String sql = "INSERT INTO "+ metaData.table()+" ("+fieldsSql+") VALUES ("+valuesSql+")";

        return executeRequest(sql, map);
    }

    @Override
    public boolean update(Model entity, long id) {

        String updateSql = "";

        Map<String, String> map = convertModelMapToPlainMap(createMapFromObject(entity));

        ModelEntity metaData = getModelMetaData(entity);

        Iterator itter = map.keySet().iterator();

        while(itter.hasNext())
        {
            String key = (String) itter.next();
            updateSql += key+"= ?, ";
        }

        updateSql = updateSql.substring(0, updateSql.length()-2);
        String sql = "UPDATE "+metaData.table()+" SET "+updateSql+" WHERE "+metaData.table()+"."+metaData.pkey()+" = "+ id +" LIMIT 1";

        return executeRequest(sql, map);
    }

    @Override
    public Optional search(Model entity, DatabaseQuery query)
    {
        //String filtersCondition = "WHERE " + formatFilter(query.filters.get(0).field, query.filters.get(0).operator, query.filters.get(0).value, false);
        String filtersCondition = "WHERE" ;
        //query.filters.remove(0);
        int fi = 0;
        for(EntityFilter f: query.filters){
            String conditional = (f.conditional == FilterOperator.AND)? "AND" : "OR";
            if (fi == 0) conditional = "";
            //String value = (f.isField)? f.value : "'"+f.value+"'";
            String s =  String.format(" %s %s", conditional, formatFilter(f.field, f.operator, f.value, f.isField));
            //return s;
            filtersCondition = filtersCondition + s;
            fi++;
        }
        Optional<ArrayList> fsl = Optional.ofNullable(query.fieldSelecteds);

        String selectedFields = "*";
        if (fsl.isPresent()) {
            if (fsl.get().size() > 0) {
                selectedFields = query.fieldSelecteds.stream().collect(Collectors.joining(","));
            }
        }

        String tables = "";
        Optional<ArrayList> tbls = Optional.ofNullable(query.tables);
        if (tbls.isPresent()) {
            if (tbls.get().size() > 0) {
                tables = query.tables.stream().collect(Collectors.joining(","));
            }
        }
        HashMap<String, String> params = new HashMap<>();

        String sql = String.format("SELECT %s FROM %s %s %s %s",
                selectedFields,
                tables,
                filtersCondition,
                "",
                parserLimit(query.limitFrom, query.limitTo));

        // executeRequest(sql, params);
        ArrayList<HashMap<String, Object>> data = resultSetToArrayList(query(sql, params));

        return Optional.ofNullable(data);
    }

    @Override
    public Optional hasOne(Model entityParent, Model entityChild) {
        return Optional.empty();
    }

    @Override
    public Optional hasMany(Model entityParent, Model entityChild) {
        return Optional.empty();
    }

    private String parserLimit(long from, long to)
    {
        String limit = "";
        if(to >= 0)
        {
            if(from >= 0)
            {
                limit = String.format("LIMIT %d, %d",from, to);
            } else {
                limit = String.format("LIMIT %d", to);
            }
        }
        return limit;
    }

    @Override
    public boolean delete(Model entity)
    {
        Map<String, String> map = convertModelMapToPlainMap(createMapFromObject(entity));
        ModelEntity metaData = getModelMetaData(entity);

        String sql = "DELETE FROM "+metaData.table()+" WHERE "+metaData.pkey()+" = ? LIMIT 1";
        Map<String, String> filters = new HashMap<String, String>();

        filters.put(metaData.pkey(), String.valueOf(map.get(metaData.pkey())));

        return executeRequest(sql, filters);
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
            logger.error("-- ERROR:1 "+ e.getMessage());
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

    @Override
    public Optional firstOne(Model entity) {
        return getFirst(entity, true);
    }

    @Override
    public Optional lastOne(Model entity) {
        return getFirst(entity, false);
    }

    @Override
    public Optional first(Model entity, HashMap<String, Object> conditions)
    {
        return Optional.ofNullable(null);
    }

    public Optional last(Model entity, HashMap<String, Object> conditions)
    {
        return Optional.ofNullable(null);
    }

    private Optional getFirst(Model entity, boolean first)
    {
        ModelEntity metaData = getModelMetaData(entity);
        String orderBy = (first)? "ASC" : "DESC";

        String sql = "SELECT "+metaData.table()+".* FROM "+metaData.table()+" ORDER BY "+ metaData.pkey()+" "+ orderBy +" LIMIT 1";

        ArrayList<HashMap<String, Object>> data = resultSetToArrayList(query(sql));

        if (data.size() > 0) {
            return Optional.of(data.get(0));
        }
        return Optional.ofNullable(null);
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

                    switch(md.getColumnTypeName(i))
                    {
                        case "TIMESTAMP":
                            String date = String.valueOf(rs.getTimestamp(i));
                            date = date.replace(".0","");
                            row.put(md.getColumnName(i).toLowerCase(), date);
                            //System.out.println(md.getColumnName(i)+"="+date);
                            break;
                        case "java.lang.String":
                        case "VARCHAR":
                            row.put(md.getColumnName(i).toLowerCase(), rs.getString(i));
                            break;
                        case "java.lang.Integer":
                        case "INT":
                        case "BIT":
                            row.put(md.getColumnName(i).toLowerCase(), rs.getInt(i));
                            break;
                        case "java.lang.Double":
                        case "DOUBLE":
                            row.put(md.getColumnName(i).toLowerCase(), rs.getDouble(i));
                            break;
                        case "java.lang.Long":
                        case "BIGINT":
                            row.put(md.getColumnName(i).toLowerCase(), rs.getLong(i));
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
                filter += String.format("%s LIKE '%s'",field,value);
                break;
        }
        return filter;
    }


}
