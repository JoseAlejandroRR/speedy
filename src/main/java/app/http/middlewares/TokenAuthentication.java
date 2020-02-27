package app.http.middlewares;

import com.josealejandrorr.speedy.contracts.http.IMiddleware;
import com.josealejandrorr.speedy.web.Request;
import com.josealejandrorr.speedy.web.RequestHandler;
import com.josealejandrorr.speedy.web.Response;

public class TokenAuthentication extends RequestHandler implements IMiddleware {

    @Override
    public void handle(Request request, Response response) {
        System.out.println("CHEQUEANDO URL " + request.url);
        if (request.url.equals("/api"))
        {
            response.send("Not enabled");
        }
    }
}