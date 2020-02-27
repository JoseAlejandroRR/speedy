package com.josealejandrorr.speedy.contracts.http;

public interface IServer {

    public void start(int port);

    public void stop();

    public boolean isRunning();
}
