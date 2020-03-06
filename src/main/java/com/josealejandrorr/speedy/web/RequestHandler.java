package com.josealejandrorr.speedy.web;

import app.contracts.IProductService;
import app.services.ProductService;
import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.annotations.AutoLoad;
import com.josealejandrorr.speedy.contracts.http.IRequestHandler;
import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.providers.Provider;
import com.josealejandrorr.speedy.providers.ServiceProvider;
import com.josealejandrorr.speedy.utils.Logger;
import sun.rmi.runtime.Log;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class RequestHandler implements IRequestHandler {

    //private ServiceProvider container;

    /*protected ServiceProvider getContainer()
    {
        return Application.container();
    }*/

    /*public void setContainer(ServiceProvider container)
    {
        this.container = container;
    }*/

    protected ILogger logger;

    public RequestHandler()
    {
        logger = Logger.getLogger();
        autloadClass();
    }

    private void autloadClass()
    {
        Field[] fields = this.getClass().getDeclaredFields();
        System.out.println("AUTOLOADER = "+this.getClass().getName());
        Arrays.stream(fields).forEach(f ->{
            System.out.println("HAField = "+f.getName());
            AutoLoad autoload = f.getAnnotation(AutoLoad.class);
            if (autoload != null) {
                Object obj = null;
                if (f.getType().isInterface()) {
                    Optional<Provider> optPr = ServiceProvider.providers.values().stream().filter(p -> {
                        return Arrays.stream(p.getInstance().getClass().getInterfaces())
                                .filter(o -> o.getName().equals(f.getType().getName())).count() > 0;
                    }).findFirst();

                    if (optPr.isPresent()) {
                        System.out.println("Field HAndler " +f.getName() + " need load "+ f.getType() + " using " + f.getClass().getName());
                        obj = optPr.get().getInstance();
                    }
                } else {
                    obj = Application.container().getProvider(f.getType().getName());
                }
                if (obj != null) {
                    try {
                        System.out.println("HAField " +f.getName() + " need load "+ f.getType() + " using " + obj.toString());
                        f.setAccessible(true);
                        f.set(this,obj);
                    } catch (IllegalArgumentException | IllegalAccessException e){
                        logger.error("Error injecting "+this.getClass().getName()+" by: " +e.getMessage());
                    }
                }
            }
        });
    }

    public void serverDebug(String TAG, String msg)
    {
        System.out.println(TAG + ": "+msg);
    }

}