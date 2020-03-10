package com.josealejandrorr.speedy.events.exceptions;

import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.annotations.AutoLoad;
import com.josealejandrorr.speedy.contracts.events.IEventTrigger;
import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.events.EventObject;
import com.josealejandrorr.speedy.providers.ServiceProvider;
import com.josealejandrorr.speedy.utils.Logger;

import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class EventException extends RuntimeException implements IEventTrigger {

    @AutoLoad
    private ILogger logger;

    private String message;
    private int code;

    public EventException(String message) {
        this(message, 1000);
    }

    public EventException(String errorType, String errorMessage) {
        super(errorType+": "+errorMessage);
        message = errorType+": "+errorMessage;
    }

    public EventException(String message, int code) {
        super(message);
        this.message = message;
        this.code = code;

        execute();
    }

    public String getMessage()
    {
        return this.getClass()+": "+message+" \nStackTrace:\n"+ Arrays.stream(this.getStackTrace()).map(e -> e.getClassName()+":"+e.getLineNumber()).collect(Collectors.joining("\n"));
    }

    public EventObject execute()
    {
        EventObject event = new EventObject(message);
        Application.logger.debug(event.toString());
        return  event;
    }


}
