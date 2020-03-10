package com.josealejandrorr.speedy.web;

import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.contracts.http.IMiddleware;
import com.josealejandrorr.speedy.contracts.http.IRequestHandler;
import com.josealejandrorr.speedy.contracts.http.IServer;
import com.josealejandrorr.speedy.contracts.http.IWebServer;
import com.josealejandrorr.speedy.providers.Provider;
import com.josealejandrorr.speedy.utils.Builder;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server implements IServer, IWebServer
{

    public static int PORT;

    private static HttpServer httpServer = null;

    protected ILogger logger;

    private static Server instance = null;

    private static ArrayList<Route> routesMap;

    public static ArrayList<String> routes;

    private static HashMap<String, IMiddleware> middlewares;

    private static IRequestHandler handler404;

    public RouterHandler routerHandler;

    public boolean isRunning = false;

    public static final String SESSION_SERVER_NAME = "SpeedySessionId";


    public Server(ILogger logger)
    {
        this.logger = logger;
        init();
    }

    public Server(ILogger logger, IRequestHandler page404)
    {
        this.logger = logger;
        handler404 = page404;
        init();
    }

    private void init()
    {
        routesMap = new ArrayList<Route>();

        routes = new ArrayList<String>();

        middlewares = new HashMap<String, IMiddleware>();

        routerHandler = new RouterHandler(this, logger);

    }

    public void start(int port)
    {
        this.PORT = port;

        try {

            httpServer =  HttpServer.create(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/", routerHandler);
            httpServer.setExecutor(null);
            httpServer.start();

            isRunning = true;

            logger.info(this.getClass().getName() ,"Server start at http://localhost:" + PORT);
        } catch (IOException e) {
            logger.error("SERVER_CREATE_ERROR: " + e.getMessage());
            e.printStackTrace();
        }


    }

    public void stop()
    {
        isRunning = false;
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    public RouterHandler getRouterHandler()
    {
        return routerHandler;
    }

    public void setRouterHandler(RouterHandler router)
    {
        this.routerHandler = router;
    }

    public void registerMiddlewares(Map<String, Object> instances)
    {
        //Map<String, String> instances = new HashMap<>();
        for (Map.Entry<String, Object> provider : instances.entrySet())
        {

            Provider instance = new Provider();
            if (!provider.getValue().getClass().toString().contains("class")) return;
            String className = provider.getValue().toString().split(" ")[1];

            registerMiddleware(provider.getKey(), (IMiddleware) Builder.createInstance(className));
        }

    }

    private void registerMiddleware(String key, IMiddleware middleware)
    {
        logger.debug("Register Middleware: " + key);
        middlewares.put(key, middleware);
    }

    public IMiddleware getMiddlware(String key)
    {
        logger.debug("Calling Middleware: " + key);
        if (middlewares.containsKey(key)) {
            return middlewares.get(key);
        } else {
            logger.debug("Middleware not founds: " + key);
        }
        return null;
    }


    public static boolean existRoute(String url)
    {
        if (routes.contains(url)) {
            return true;
        }
        return false;
    }

    public static IRequestHandler getHandler404() {
        return handler404;
    }
}
