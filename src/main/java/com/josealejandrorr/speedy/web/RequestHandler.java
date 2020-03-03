package com.josealejandrorr.speedy.web;

import app.services.ProductService;
import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.annotations.AutoLoad;
import com.josealejandrorr.speedy.contracts.http.IRequestHandler;
import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.providers.Provider;
import com.josealejandrorr.speedy.providers.ServiceProvider;
import com.josealejandrorr.speedy.utils.Logger;

import java.lang.reflect.Field;
import java.util.Arrays;

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

    protected ILogger logger = Logger.getLogger();

    public RequestHandler()
    {
        autloadClass();
    }

    private void autloadClass()
    {
        Field[] fields = this.getClass().getDeclaredFields();
        System.out.println("AUTOLOADER = "+this.getClass().getName());
        Arrays.stream(fields).forEach(f ->{
            System.out.println("Field = "+f.getName());
            AutoLoad autoload = f.getAnnotation(AutoLoad.class);
            if (autoload != null) {
                Object obj = Application.container().getProvider(f.getType().getName());
                if (obj != null) {
                    //System.out.println("Field " +f.getName() + " need load "+ f.getType() + " using " + Application.container().getProvider(f.getType().getName()).toString());
                    try {
                        f.setAccessible(true);
                        f.set(this, obj);
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