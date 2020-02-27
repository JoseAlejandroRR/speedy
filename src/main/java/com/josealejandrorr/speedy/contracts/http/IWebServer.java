package com.josealejandrorr.speedy.contracts.http;

public interface IWebServer {

    public static IRequestHandler getHandler404() {
        return null;
    }

    public IMiddleware getMiddlware(String key);

    public boolean isRunning();
}