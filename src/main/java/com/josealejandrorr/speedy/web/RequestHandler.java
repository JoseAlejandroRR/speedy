package com.josealejandrorr.speedy.web;

import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.contracts.http.IRequestHandler;
import com.josealejandrorr.speedy.providers.ServiceProvider;

public class RequestHandler implements IRequestHandler {

    public ServiceProvider container;

    protected ServiceProvider getContainer()
    {
        return Application.container();
    }

    /*public void setContainer(ServiceProvider container)
    {
        this.container = container;
    }*/

    public void serverDebug(String TAG, String msg)
    {
        System.out.println(TAG + ": "+msg);
    }

}