package com.josealejandrorr.speedy.database;

import java.util.EventObject;

public class EventNotificationDB extends EventObject {

    NotificationDB notification;
    public EventNotificationDB(Object source,NotificationDB notify) {
        super(source);
        notification = notify;
    }
}
