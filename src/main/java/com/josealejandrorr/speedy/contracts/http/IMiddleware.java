package com.josealejandrorr.speedy.contracts.http;

import com.josealejandrorr.speedy.web.Request;
import com.josealejandrorr.speedy.web.Response;

public interface IMiddleware {

    public void handle(Request request, Response response);
}
