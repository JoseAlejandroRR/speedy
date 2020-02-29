package com.josealejandrorr.speedy.data.entities;

import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.annotations.ModelEntity;
import com.josealejandrorr.speedy.contracts.data.repositories.Repository;
import com.josealejandrorr.speedy.providers.ServiceProvider;
import com.josealejandrorr.speedy.utils.Builder;
import com.josealejandrorr.speedy.utils.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

public abstract class Model  {

    private Repository serviceRepository;

    private String fieldIndex;

    private String tableName;

    private long key;

    private boolean hasTimestamps = false;

    public Model()
    {
        serviceRepository = (Repository) Application.container().getProvider(ServiceProvider.SERVICE_REPOSITORY);

        Class c = this.getClass();
        Annotation an = c.getAnnotation(ModelEntity.class);
        ModelEntity me = (ModelEntity) an;
        if (me != null){
            this.tableName = me.table();
            this.fieldIndex = me.pkey();
            hasTimestamps = me.timestamps();

            serviceRepository.registerModel(this, tableName);
        } else {
            Logger.getLogger().error("@ModelEntity must used in " + c.getName());
        }
    }

    public void findById(long id)
    {

        Optional<HashMap<String, Object>> map = serviceRepository.findById(this, id);

        map.ifPresent(this::setInstance);

        System.out.println(this.toString());

    }


    public void save()
    {
        if (key > 0) {
            update();
        } else {
            create();
        }
    }

    public void create()
    {
        serviceRepository.create(this);
    }

    public void update()
    {
        serviceRepository.update(this);
    }

    private void setInstance(HashMap<String, Object> map)
    {
        Class<?> objClass = this.getClass();
        Field[] fields  = objClass.getDeclaredFields();

        for( Field field : fields ){
            try {

                if (map.containsKey(field.getName()))
                {
                    System.out.println(field.getType().toString());
                    System.out.println(field.getName()+"="+map.get(field.getName()));
                    field.setAccessible(true);
                    /*if(field.getType().toString().contains("int") || field.getType().toString().contains("Integer")) field.set(this, Integer.parseInt(data.get(field.getName())));
                    if(field.getType().toString().contains("Long")) field.set(this, Long.parseLong(map.get(field.getName())));
                    if(field.getType().toString().contains("String")) field.set(this, String.valueOf(data.get(field.getName())));
                    if(field.getType().toString().contains("double")) field.set(this, Double.parseDouble(data.get(field.getName())));
                    if(field.getType().toString().contains("Date")){
                        field.set(this, Builder.convertStringToDate((data.get(field.getName()))));
                    }*/
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


    }

}
