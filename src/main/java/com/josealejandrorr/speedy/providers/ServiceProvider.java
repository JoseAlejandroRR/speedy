package com.josealejandrorr.speedy.providers;

import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.contracts.data.repositories.DatabaseRepository;
import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.data.drivers.MySqlDriverDatabase;
import com.josealejandrorr.speedy.utils.Builder;
import com.josealejandrorr.speedy.utils.Logger;

import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {

    private static HashMap<String, Provider> providers;

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
            System.out.println(provider.getValue().getClass());
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
        logger.debug("Calling Provider: " + key);
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

    public void setContextDefault()
    {
        DatabaseRepository orm = null;
        if (Application.env("database.driver") != null) {
            switch (Application.env("database.driver").toLowerCase())
            {
                case "mysql":
                    orm = new MySqlDriverDatabase(logger);
                    break;
                default:
                    logger.error("Database Driver unkown:"  + Application.env("driver"));
                    break;
            }
            Provider providerORM = new Provider();
            providerORM.create(orm);
            registeProvider(DATABASE_DRIVER_REPOSITORY, providerORM);
        } else {
            logger.debug("Driver for Database wasnt configurated");
        }

    }


    /*public enum Providers {
        SERVICES_PROVIDERS,
        LOGGER
    }*/

    public final static String SERVICE_REPOSITORY = "service_repository";
    public final static String DATABASE_DRIVER_REPOSITORY = "database_driver";

}
