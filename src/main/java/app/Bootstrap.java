package app;

import app.bootstrap.Middlewares;
import app.bootstrap.Providers;
import app.http.Routes;
import app.http.handlers.PageNotFound;
import app.models.User;
import com.josealejandrorr.speedy.Application;
import com.josealejandrorr.speedy.database.Conexion;
import com.josealejandrorr.speedy.providers.ServiceProvider;
import com.josealejandrorr.speedy.utils.Logger;
import com.josealejandrorr.speedy.web.Server;

public class Bootstrap {

    private static String urlConfigurationFile = "./application.config";
    public static void run()
    {
        System.out.println("Init");
        Conexion conexion = new Conexion();
        conexion.modeDebug = true;

        Logger logger = new Logger(Logger.DEBUG, "./application.log");

        //Logger.getLogger().debug("App Init");

        /*User user = new User();

        user.name = "Jose";
        user.lastname = "Realza";
        user.document = "95950137";
        user.status = 1;
        user.save();

        System.out.println("END");*/
        Server server = new Server(logger, new PageNotFound());

        ServiceProvider container = new ServiceProvider();

        server.routerHandler.setContainer(container);

        container.setLogger(logger);

        Application app = new Application(server, container, urlConfigurationFile, logger);

        Routes routes = new Routes(server.getRouterHandler());

        //routes.getRouter().setHandler404(new PageNotFound());

        //server.setRouterHandler(routes.getRouter());

        server.registerMiddlewares(Middlewares.getList());

        container.registerProviders(Providers.getList());

        server.start(9000);
    }
}
