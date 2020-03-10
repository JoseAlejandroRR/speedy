package com.josealejandrorr.speedy;

import com.josealejandrorr.speedy.config.ApplicationConfig;
import com.josealejandrorr.speedy.contracts.http.IServer;
import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.providers.Provider;
import com.josealejandrorr.speedy.providers.ServiceProvider;


import java.util.HashMap;

public class Application  {

    private static Application instance;

    private static ServiceProvider container;

    private static HashMap<String, Provider> providers;

    private static HashMap<String, String> dataEnv = new HashMap<>();

    public static IServer server;

    public static int PORT = 9000;

    public static ILogger logger;

    public static final String PATH_RESOURCES = "./src/main/resources";


    public Application(ServiceProvider container, ApplicationConfig config)
    {
        providers = new HashMap<String, Provider>();

        this.container = container;

        dataEnv = config.data();

        container.setContextDefault();
    }

    public void setServer(IServer server)
    {
        Application.server = server;
    }

    public void setLogger(ILogger logger)
    {
        Application.logger = logger;
    }


    public static ServiceProvider container()
    {
        return container;
    }


    public static String env(String key)
    {
        if (dataEnv.containsKey(key)) {
            return dataEnv.get(key);
        }
        return null;
    }


}