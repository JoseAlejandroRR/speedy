package com.josealejandrorr.speedy.contracts.providers;

public interface ILogger {

    public void debug(String... strings);

    public void info(String... strings);

    public void error(String... strings);
}
