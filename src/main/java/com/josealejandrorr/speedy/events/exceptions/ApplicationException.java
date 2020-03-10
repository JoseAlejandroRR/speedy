package com.josealejandrorr.speedy.events.exceptions;

public class ApplicationException extends EventException  {

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String errorType, String errorMessage) {
        super(errorType, errorMessage);
    }

    public ApplicationException(String message, int code) {
        super(message, code);
    }
}
