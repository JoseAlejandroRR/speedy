package com.josealejandrorr.speedy.web;

import com.josealejandrorr.speedy.annotations.SmartClass;
import com.josealejandrorr.speedy.contracts.http.IRequestHandler;

@SmartClass
public class PageNotFoundDefault implements IRequestHandler {

    public void index(Request request, Response response) {
        response.send("URL not found", 404);
    }
}
