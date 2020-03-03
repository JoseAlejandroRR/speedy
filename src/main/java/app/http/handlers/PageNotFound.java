package app.http.handlers;

import com.josealejandrorr.speedy.contracts.http.IRequestHandler;
import com.josealejandrorr.speedy.web.Request;
import com.josealejandrorr.speedy.web.Response;

public class PageNotFound implements IRequestHandler {

    public void index(Request request, Response response) {
        response.send("URL not exists22", 404);
    }
}
