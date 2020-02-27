package com.josealejandrorr.speedy;

import com.josealejandrorr.speedy.contracts.http.IServer;
import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.providers.Provider;
import com.josealejandrorr.speedy.providers.ServiceProvider;

import java.util.HashMap;

public class Application  {


    private static Application instance;

    private static ServiceProvider container;

    private static HashMap<String, Provider> providers;

    public IServer server;

    public static int PORT = 9000;

    public ILogger logger;

    public static final String PATH_RESOURCES = "./src/main/resources";


    public Application(IServer server, ServiceProvider containr, ILogger logger)
    {
        providers = new HashMap<String, Provider>();

        this.container = containr;

        this.server =  server;

        this.logger = logger;
    }


    public static ServiceProvider container()
    {
        return container;
    }



}
