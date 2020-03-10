package app;

import app.bootstrap.Middlewares;
import app.bootstrap.Providers;
import app.http.Routes;
import app.http.handlers.PageNotFound;
import app.models.User;
import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.config.ApplicationConfig;
import com.josealejandrorr.speedy.database.Conexion;
import com.josealejandrorr.speedy.providers.ServiceProvider;
import com.josealejandrorr.speedy.utils.Logger;
import com.josealejandrorr.speedy.web.Server;

public class Bootstrap {

    public static void run()
    {
        System.out.println("Init");

        ApplicationConfig config = new ApplicationConfig("./application.config");

        Logger logger = new Logger();
        logger.setMode(config.data().get("application.mode"));
        logger.setFileStorage(config.data().get("application.log"));

        Server server = new Server(logger);

        ServiceProvider container = new ServiceProvider();

        server.routerHandler.setContainer(container);

        container.setLogger(logger);

        Application app = new Application(container, config, server, logger);

        Routes routes = new Routes(server.getRouterHandler());

        //routes.getRouter().setHandler404(new PageNotFoundDefault());

        container.registerProviders(Providers.getList());

        server.registerMiddlewares(Middlewares.getList());

        server.start(9000);
    }
}
