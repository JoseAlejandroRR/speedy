package com.josealejandrorr.speedy.web;


import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.contracts.http.IMiddleware;
import com.josealejandrorr.speedy.contracts.http.IRequestValidator;
import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.contracts.http.IRequestHandler;
import com.josealejandrorr.speedy.contracts.http.IWebServer;
import com.josealejandrorr.speedy.providers.ServiceProvider;
import com.josealejandrorr.speedy.utils.Builder;
import com.josealejandrorr.speedy.utils.Validator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import java.util.ArrayList;
import java.util.HashMap;

public class RouterHandler implements HttpHandler {

    private IWebServer server;

    private ILogger logger;

    private ServiceProvider container;

    private ArrayList<Route> routesMap;

    public ArrayList<String> routes;

    public static final String METHOD_POST = "POST";

    public static final String METHOD_GET = "GET";

    private IRequestHandler handler404;

    private long startTime, endTime, totalTime, startMemory, endMemory, totalMemory;

    private HashMap<String, String> routeParams;


    public RouterHandler(IWebServer server, ILogger logger)
    {
        this.server = server;
        this.logger = logger;
        routesMap = new ArrayList<Route>();
        routes = new ArrayList<String>();
        routes = new ArrayList<String>();
        routeParams = null;
    }

    public void setContainer(ServiceProvider container)
    {
        this.container = container;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException
    {
        if(!server.isRunning()) return;

        startTime =  System.currentTimeMillis();
        startMemory = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        routeParams = null;
        boolean prevent = false;
        boolean failValidator = false;

        Cookies.setHeaders(exchange);

        if (!Cookies.has(Server.SESSION_SERVER_NAME)) {
            Cookies.add(Server.SESSION_SERVER_NAME, String.valueOf(System.currentTimeMillis()));
        }


        //Files.get(exchange);

        Request request = new Request(exchange);
        Response response = new Response(exchange);

        for(Map.Entry<String, List<String>> item : exchange.getRequestHeaders().entrySet())
        {
            for(String str : item.getValue())
            {
                //logger.debug("HEADER", item.getKey(), str);
            }
        }

        logger.info(request.method, request.url, request.headers.keySet().toString());
        //int index = existRoute(request.url);
        int index = getRouteIndexByUrl(request.url);

        if (index >= 0) {
            //int n = routes.indexOf(request.url);
            Route route = routesMap.get(index);

            if(route.method.equals(request.method)) {

                if(route.middlewares != null && route.middlewares.length > 0){
                    //Response responseCheck = processMiddlewares(route, request, response);
                    processMiddlewares(route, request, response);
                    if (response.handled) {
                        prevent = true;
                    }
                }

                if(route.rulesGet != null && route.rulesGet.size() > 0 || route.rulesPost != null && route.rulesPost.size() > 0)
                {
                    HashMap<String, String> error = Validator.array(route.rulesGet, new HashMap<>(request.query));
                    String messageError = "";
                    if (error.size() > 0) {
                        for (HashMap.Entry<String, String> err: error.entrySet()) {
                            logger.debug("ERROR: "+err.getKey(), err.getValue());
                            if (!failValidator) {
                                messageError = err.getValue();
                            }
                        }
                        response.send(messageError, 500);
                    }

                    error = Validator.array(route.rulesPost, request.body);
                    if (error.size() > 0) {
                        for (HashMap.Entry<String, String> err: error.entrySet()) {
                            logger.debug("ERROR: "+err.getKey(), err.getValue());
                            if (!failValidator) {
                                messageError = err.getValue();
                            }
                        }
                        response.send(messageError, 500);
                    }
                }

                if (!prevent) {
                    routeParams = getVars(
                            request.url,
                            routes.get(
                                    getRouteIndexByUrl(request.url)
                            )
                    );
                    if (routeParams.size() > 0) {
                        request.query.putAll(routeParams);
                        /*for(Map.Entry<String, String> item : routeParams.entrySet())
                        {
                            //logger.debug("var", item.getKey(), item.getValue());
                            request.query.put(item.getKey(),item.getValue());
                        }*/
                    }

                    if (route.methodHandlerName==null) {
                        callMethodAtInstance(route.handlerName,"index", request, response);
                    } else {
                        callMethodAtInstance(route.handlerName,route.methodHandlerName, request, response);
                    }
                    //logger.debug("Working to here again");
                }
            } else {
                logger.info("Http Method not enabled: " + request.url,"httpCode: 505");
                response.send("Method not enabled.", 500);
            }
        } else {
            if (Server.getHandler404() == null) {
                //response.send("URL not found.", 404);
            } else {
                callMethodAtInstance(Server.getHandler404(),"index", request, response);
            }
            logger.info("URL: " + request.url +" not found","httpCode: 404");
        }

        exchange.sendResponseHeaders(response.httpCode, response.getResponse().length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getResponse().getBytes());
        os.close();

        //Files.clearTempDir();
        endTime   =  System.currentTimeMillis();
        endMemory = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        totalTime = (endTime - startTime);
        totalMemory = (endMemory - startMemory);
        logger.info(String.format("Duration %d miliseconds using %s kb of memory \n", totalTime, totalMemory / 1000));
    }


    public void setRoutes(ArrayList<Route> map)
    {
        Iterator it = map.iterator();
        while(it.hasNext())
        {
            Route route = (Route) it.next();
            routesMap.add(route);
            routes.add(route.url);
        }
    }

    public int getRouteIndexByUrl(String url)
    {

        Iterator it = routes.iterator();
        int  i = 0;
        while(it.hasNext())
        {
            String uri = (String) it.next();
            uri = uri.replaceAll("/","\\\\\\/");
            String patternString = uri.replaceAll("\\[\\w*\\]","(\\\\\\w*)");
            Pattern p = Pattern.compile(patternString);
            Matcher m = p.matcher(url);

            while(m.find()) {
                String f = m.group();
                if (f.equals(url)) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    private static HashMap<String, String> getVars(String url, String path)
    {

        HashMap<String, String> vars = new HashMap<>();

        String[] ks = path.split("/");
        String[] vs = url.split("/");

        for(int i = 0; i < ks.length; i++)
        {
            if(!ks[i].equals(vs[i])) {
                //System.out.println(ks[i].replaceFirst("\\[","").replaceFirst("\\]","")+"="+vs[i]);
                vars.put(ks[i].replaceFirst("\\[","").replaceFirst("\\]",""),vs[i]);
            }
        }
        return vars;
    }


    public RouterHandler get(String url, Object handler)
    {
        return get(url, handler, null);
    }

    public RouterHandler get(String url, Object handler, String methodName)
    {
        addRoute(METHOD_GET, url, handler, methodName);
        return this;
    }

    private boolean addRoute(String method, String url, Object handler, String methodName)
    {

        if (!handler.getClass().toString().contains("class")) return false;
        String className = handler.toString().split(" ")[1];
        logger.debug("RUN "+className);

        if (Application.container().existInstance(className)) {

            if (getRouteIndexByUrl(url) < 0) {
                routesMap.add(new Route(method, url, className, methodName));
                routes.add(url);
                return true;
            }
        } else {
            logger.error("The class "+className+" can´t be found in the Application Context");
        }
        return false;
    }


    public RouterHandler post(String url, Object handler)
    {
        post(url, handler, null);
        return this;
    }


    public RouterHandler post(String url, Object handler, String methodName)
    {
        addRoute(METHOD_POST, url, handler, methodName);
        return this;
    }

    public RouterHandler middleware(String... middles)
    {
        String[] values = new String[middles.length];
        int i = 0;
        for(String key : middles)
        {
            values[i] = key;
            i++;
        }
        Route route = routesMap.get(routes.size()-1);
        route.middlewares = values;
        return this;
    }

    public RouterHandler validator(Object validator)
    {
        IRequestValidator validatorInstance = null;
        if (!validator.getClass().toString().contains("class")) return this;
        String className = validator.toString().split(" ")[1];

        try {
            validatorInstance = (IRequestValidator) Builder.createInstance(className);
        } catch (Exception ex)
        {
            logger.debug("Handler Wrong: "+validator.getClass().getName());
        }

        if (validatorInstance != null) {
            Route route = routesMap.get(routes.size()-1);
            route.rulesGet = validatorInstance.getRulesGet();
            route.rulesPost = validatorInstance.getRulesPost();
        }
        return this;
    }

    private Response processMiddlewares(Route route, Request request, Response response)
    {
        Response res = null;
        for (String key : route.middlewares)
        {
            IMiddleware middleware = server.getMiddlware(key);

            if(middleware != null)
            {
                middleware.handle(request, response);
            } else {
                return response.send("Middleware Not Found", 500);
            }
        }

        return res;
    }

    private void callMethodAtInstance(Object obj, String methodName, Request request, Response response)
    {
        Method method = null;
        Object l = null;
        try {
            System.out.println("CLASS = "+obj.getClass().toString());
            l = Application.container().getProvider(obj.toString());
            method = l.getClass().getMethod(methodName, Request.class, Response.class);
        } catch (SecurityException e) {
            logger.debug("SecurityException: "+e.getMessage());
        }
        catch (NoSuchMethodException e) {
            logger.debug("NoSuchMethodException: "+e.getMessage());
        }

        try {

            method.invoke(l, request, response);
        } catch (IllegalArgumentException e) {
            logger.debug("IllegalArgumentException: "+e.getMessage());
        }
        catch (IllegalAccessException e) {
            logger.debug("IllegalAccessException: "+e.getMessage());

        }
        catch (InvocationTargetException e) {
            logger.debug("InvocationTargetException: "+e.toString());
        }
    }


}

