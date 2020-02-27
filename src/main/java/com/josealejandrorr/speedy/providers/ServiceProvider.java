package com.josealejandrorr.speedy.providers;

import com.josealejandrorr.speedy.contracts.providers.ILogger;
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
            if (!provider.getValue().getClass().toString().contains("class")) return;
            String className = provider.getValue().toString().split(" ")[1];
            instance.create(Builder.createInstance(className));
            registeProvider(provider.getKey(), instance);
        }

    }

    public Object getProvider(String key)
    {
        logger.debug("Calling Provider: " + key);
        if (providers.containsKey(key)) {
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

}
