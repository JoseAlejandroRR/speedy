package com.josealejandrorr.speedy.events;

public class EventObject {

    private String actionText;

    public EventObject(String text)
    {
        actionText = text;
    }


    public String toString()
    {
        return actionText;
    }
}
