package com.josealejandrorr.speedy.data;

import com.josealejandrorr.speedy.annotations.ModelEntity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public abstract class EntityMapper {


    protected Map<String, Object> createMapFromObject(Object obj)
    {
        Map<String, Object> map = new HashMap<String, Object>();
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
                } else if(field.getType().toString().toLowerCase().contains("integer"))
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
                if(addField)
                {
                    map.put(field.getName(), field.get(obj));
                }

            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                System.out.println("-- ERROR bindProperties: "+e.toString()+" --\n");
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                System.out.println("-- ERROR bindProperties: "+e.toString()+" --\n");
                e.printStackTrace();
            };

        }

        return map;
    }

    protected Map<String, String> convertModelMapToPlainMap(Map<String, Object> obj)
    {
        HashMap<String, String> map = new HashMap<>();

        obj.entrySet().stream().forEach(item ->{
            map.put(item.getKey(),  String.valueOf(item.getValue()));
        });

        return map;
    }

    protected ModelEntity getModelMetaData(Object model)
    {
        Class<?> obj = model.getClass();
        Annotation an = obj.getAnnotation(ModelEntity.class);
        return (ModelEntity) an;
    }


}
