package com.josealejandrorr.speedy.providers;

import app.services.ProductService;
import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.annotations.AutoLoad;
import com.josealejandrorr.speedy.annotations.Service;
import com.josealejandrorr.speedy.annotations.SmartClass;
import com.josealejandrorr.speedy.contracts.data.repositories.DatabaseRepository;
import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.data.drivers.MySqlDriverDatabase;
import com.josealejandrorr.speedy.utils.Builder;
import com.josealejandrorr.speedy.utils.Logger;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.*;

public class ServiceProvider {

    public static HashMap<String, Provider> providers;

    protected ILogger logger;

    public ServiceProvider()
    {
        providers = new HashMap<String, Provider>();
    }

    public void setLogger(ILogger logger)
    {
        this.logger = logger;
    }

    public void registerProviders(Map<String, Object> instances)
    {
        for (Map.Entry<String, Object> provider : instances.entrySet())
        {
            Provider instance = new Provider();
            if (provider.getValue() instanceof Class) {
                String className = provider.getValue().toString().split(" ")[1];
                instance.create(Builder.createInstance(className));
            } else {
                instance.create(provider.getValue());
            }
            registeProvider(provider.getKey(), instance);
        }

    }

    public Object getProvider(String key)
    {
        if (providers.containsKey(key)) {
            if (providers.get(key).getInstance() == null) {
                logger.error("Provider '" + key +"' is NULL");
                return null;
            }
            logger.debug("PROVIDER ",providers.get(key).getInstance().toString());
            return providers.get(key).getInstance();
        } else {
            logger.debug("Provider not found: " + key);
        }
        return null;
    }

    private void registeProvider(String key, Provider instance)
    {
        logger.debug("Register Provider: " + key);
        providers.put(key, instance);
    }

    public boolean existInstance(String key)
    {
        return providers.containsKey(key);
    }

    public void setContextDefault()
    {
        Reflections reflections = new Reflections("");

        Set<Class<?>> klass = reflections.getTypesAnnotatedWith(SmartClass.class);
        klass.stream().filter(c -> !c.isAnnotation()).forEach(c -> {
            System.out.println("CLASS " + c.getName());
            Object obj = Builder.createInstance(c.getName());
            Provider pr = new Provider();
            pr.create(obj);
            registeProvider(c.getName(), pr);
        });

        providers.values().stream().map(p -> p.getInstance()).forEach(c -> {

            Field[] fields = c.getClass().getDeclaredFields();

            Arrays.stream(fields).forEach(f ->{
                AutoLoad autoload = f.getAnnotation(AutoLoad.class);
                if (autoload != null) {
                    Object obj = null;
                    if (f.getType().isInterface()) {
                        Optional<Provider> optPr = ServiceProvider.providers.values().stream().filter(p -> {
                            return Arrays.stream(p.getInstance().getClass().getInterfaces())
                                    .filter(o -> o.getName().equals(f.getType().getName())).count() > 0;
                        }).findFirst();

                        if (optPr.isPresent()) {
                            obj = optPr.get().getInstance();
                        }
                    } else {
                        obj = Application.container().getProvider(f.getType().getName());
                    }
                    if (obj != null) {
                        try {
                            f.setAccessible(true);
                            f.set(c,obj);
                        } catch (IllegalArgumentException | IllegalAccessException e){
                            logger.error("Error injecting "+this.getClass().getName()+" by: " +e.getMessage());
                        }
                    }

                }
            });
        });


        setDatabaseDriverRepository();
    }

    public Object getInstanceByClass(Class<?> klazz)
    {
        Object obj = null;
        System.out.println("SEARCH   " +klazz.getName()+ " need load "+ klazz.getTypeName());
        if (klazz.isInterface()) {
            Optional<Provider> optPr = ServiceProvider.providers.values().stream().filter(p -> {
                return Arrays.stream(p.getInstance().getClass().getInterfaces())
                        .filter(o -> o.getName().equals(klazz.getName())).count() > 0;
            }).findFirst();

            if (optPr.isPresent()) {
                System.out.println("SEARCH for  " +klazz.getName()+ " need load "+ klazz.getTypeName()+ " using " + klazz.getClass().getName());
                obj = optPr.get().getInstance();
            }
        } else {
            obj = Application.container().getProvider(klazz.getName());
        }

        return obj;
    }

    private void setDatabaseDriverRepository()
    {
        DatabaseRepository orm = null;
        if (Application.env("database.driver") != null) {
            switch (Application.env("database.driver").toLowerCase())
            {
                case "mysql":
                    orm = new MySqlDriverDatabase(logger);
                    break;
                default:
                    logger.error("Database Driver unknown:"  + Application.env("driver"));
                    break;
            }
            Provider providerORM = new Provider();
            providerORM.create(orm);
            registeProvider(DATABASE_DRIVER_REPOSITORY, providerORM);
        } else {
            logger.error("Driver for Database not was configurated");
        }
    }


    public final static String SERVICE_REPOSITORY = "service_repository";
    public final static String DATABASE_DRIVER_REPOSITORY = "database_driver";
    public final static String LOGGER = "logger";

}
