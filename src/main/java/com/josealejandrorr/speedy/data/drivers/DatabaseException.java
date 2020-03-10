package com.josealejandrorr.speedy.data.drivers;

import com.josealejandrorr.speedy.data.entities.Model;
import com.josealejandrorr.speedy.events.exceptions.EventException;

public class DatabaseException extends EventException {

    public Model entity;

    public DatabaseException(String errorMessage) {
        super(errorMessage);
    }

    public DatabaseException(String errorType, String errorMessage) {
        super(errorType, errorMessage);
    }
}
