package com.josealejandrorr.speedy.data.entities;

import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.annotations.ModelEntity;
import com.josealejandrorr.speedy.contracts.data.repositories.DatabaseQuery;
import com.josealejandrorr.speedy.contracts.data.repositories.DatabaseRepository;
import com.josealejandrorr.speedy.contracts.data.repositories.Repository;
import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.providers.ServiceProvider;
import com.josealejandrorr.speedy.utils.Builder;
import com.josealejandrorr.speedy.utils.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Model  {

    private DatabaseRepository serviceRepository;

    private String fieldIndex;

    private String tableName;

    private ArrayList<EntityFilter> filters;

    private ArrayList<String> tables;

    private ArrayList<String> selectFields;

    private ILogger logger;

    private long limitFrom = 0;

    private long limitTo = -1;

    private long key;

    private boolean hasTimestamps = false;

    public Date created_at;

    public Date updated_at;

    public Model()
    {
        logger = Logger.getLogger();
        filters = new ArrayList<>();
        serviceRepository = (DatabaseRepository) Application.container().getProvider(ServiceProvider.DATABASE_DRIVER_REPOSITORY);
        if (serviceRepository == null) {
            logger.error("Database Repository doesnt exist in the Application Container");
        }

        Class c = this.getClass();
        Annotation an = c.getAnnotation(ModelEntity.class);
        ModelEntity me = (ModelEntity) an;
        if (me != null){
            this.tableName = me.table();
            this.fieldIndex = me.pkey();
            hasTimestamps = me.timestamps();

        } else {
            Logger.getLogger().error("@ModelEntity must used in " + c.getName());
        }
        resetQuery();
    }

    private void resetQuery()
    {
        tables = new ArrayList<String>();
        tables.add(tableName);

        filters = new ArrayList<>();
        selectFields = new ArrayList<>();

        limitFrom = 0;
        limitTo = -1;
    }

    public Model select(String field)
    {
        if (!selectFields.contains(field))
        {
            selectFields.add(field);
        }
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

    public Model findById(long id)
    {

        Optional<HashMap<String, Object>> map = serviceRepository.findById(this, id);

        map.ifPresent(this::setInstance);

        System.out.println(this.toString());

        return this;
    }


    public void save()
    {
        if (key > 0) {
            update();
        } else {
            create();
            last();
        }
    }

    public boolean create()
    {
        return serviceRepository.create(this);
    }

    public boolean update()
    {
        return serviceRepository.update(this, key);
    }

    public boolean delete()
    {
        return serviceRepository.delete(this);
    }

    public Model last()
    {
        if(this.filters.isEmpty())
        {
            Optional<HashMap<String, Object>> map = serviceRepository.lastOne(this);
            map.ifPresent(this::setInstance);
            return this;
        } else {

        }
        return this;
    }

    public ArrayList get()
    {
        ArrayList<Model> items = new ArrayList<>();
        if(this.filters.isEmpty())
        {
            return items;
        }

        DatabaseQuery query = new DatabaseQuery();

        query.limitFrom = limitFrom;
        query.limitTo = limitTo;
        query.fieldSelecteds = selectFields;
        query.filters = filters;
        query.tables = tables;

        Optional<ArrayList<HashMap>> results = serviceRepository.search(this, query);

        if (results.isPresent()) {
            items  = (ArrayList<Model>) results.get().stream().map(m -> {
                Model obj = (Model) Builder.createInstance(this.getClass().getName());
                obj.setInstance(m);
                return obj;
            }).collect(Collectors.toList());
        }
        return items;
    }

    public Model where(String field, String operator, String value)
    {
        EntityFilter filter = new EntityFilter(field, operator, value, FilterOperator.AND) ;
        filters.add(filter);
        return this;
    }

    public Model orWhere(String field, String operator, String value)
    {
        EntityFilter filter = new EntityFilter(field, operator, value, FilterOperator.OR) ;
        filters.add(filter);
        return this;
    }

    public Model take(int from)
    {
        if(from < 0) return this;
        this.limitTo = from;
        return this;
    }

    public Model skip(int skip)
    {
        if(skip < 0) return this;
        this.limitFrom = skip;
        return this;
    }

    public Model join(String table, String field, String condition, String value)
    {
        EntityFilter filter = new EntityFilter(field, condition, value, FilterOperator.AND, true);
        filters.add(filter);
        if (!tables.contains(table)) {
            tables.add(table);
        }
        return this;
    }

    public ArrayList hasMany(Class clazz,String keyForeign)
    {
        ArrayList<Model> items = new ArrayList<>();
        Model obj = null;
        obj = (Model) Builder.createInstance(clazz.getName());

        if (obj == null) return items;

        this.join(obj.tableName,obj.tableName+"."+keyForeign, "=", String.valueOf(this.tableName+"."+this.fieldIndex))
                .select(obj.tableName+".*")
                .where(this.tableName+"."+this.fieldIndex,"=", String.valueOf(this.key));

        DatabaseQuery query = new DatabaseQuery();

        query.fieldSelecteds = selectFields;
        query.filters = filters;
        query.tables = tables;

        Optional<ArrayList<HashMap>> results = serviceRepository.search(this, query);

        if (results.isPresent()) {
            items  = (ArrayList<Model>) results.get().stream().map(m -> {
                Model child = (Model) Builder.createInstance(clazz.getName());
                child.setInstance(m);
                return child;
            }).collect(Collectors.toList());
        }
        resetQuery();
        return items;
    }

    public Model hasOne(Class clazz,String keyForeign)
    {
        Model obj = (Model) Builder.createInstance(clazz.getName());

        this.join(obj.tableName,obj.tableName+"."+keyForeign, "=", String.valueOf(this.tableName+"."+this.fieldIndex))
                .select(obj.tableName+".*")
                .where(this.tableName+"."+this.fieldIndex,"=", String.valueOf(this.key))
                .take(1);

        DatabaseQuery query = new DatabaseQuery();

        query.fieldSelecteds = selectFields;
        query.filters = filters;
        query.tables = tables;
        query.limitTo = limitTo;

        Optional<ArrayList<HashMap>> results = serviceRepository.search(this, query);
        resetQuery();

        if (results.isPresent()) {
            if (results.get().size() > 0) {
                obj.setInstance(results.get().get(0));
                return obj;
            }
        }

        return null;
    }

    public Model belongsTo(Class clazz,String keyForeign)
    {
        Model obj = (Model) Builder.createInstance(clazz.getName());

        this.join(obj.tableName,obj.tableName+"."+obj.fieldIndex, "=", String.valueOf(this.tableName+"."+keyForeign))
                .select(obj.tableName+".*")
                .where(this.tableName+"."+this.fieldIndex,"=", String.valueOf(this.key))
                .take(1);

        DatabaseQuery query = new DatabaseQuery();

        query.fieldSelecteds = selectFields;
        query.filters = filters;
        query.tables = tables;
        query.limitTo = limitTo;

        Optional<ArrayList<HashMap>> results = serviceRepository.search(this, query);
        resetQuery();

        if (results.isPresent()) {
            if (results.get().size() > 0) {
                obj.setInstance(results.get().get(0));
                return obj;
            }
        }

        return null;
    }


    private void setInstance(HashMap<String, Object> map)
    {
        Class<?> objClass = this.getClass();
        Field[] fields  = objClass.getDeclaredFields();

        for( Field field : fields ){
            try {

                if (map.containsKey(field.getName()))
                {
                    //System.out.println(field.getType().toString());
                    //System.out.println(field.getName()+"="+map.get(field.getName()));
                    field.setAccessible(true);
                    field.set(this, map.get(field.getName()));

                    if (field.getName().equals(fieldIndex)) {
                        key = Long.parseLong(map.get(field.getName()).toString());
                    }
                }

            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                System.out.println("-- ERROR setProperties: "+e.toString()+" --\n");
                e.printStackTrace();
            }
        }

        if (hasTimestamps) {
            setTimestamps(this, map);
        }

    }

    private void setTimestamps(Model model, Map<String, Object> map)
    {
        if (map.containsKey("created_at")) {
            model.created_at = (Date) map.get("created_at");
        }
        if (map.containsKey("updated_at")) {
            model.updated_at = (Date) map.get("updated_at");
        }
    }

}
