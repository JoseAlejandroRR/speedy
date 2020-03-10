package app.bootstrap;

import com.josealejandrorr.speedy.data.drivers.MySqlDriverDatabase;
import com.josealejandrorr.speedy.database.DB;
import com.josealejandrorr.speedy.providers.Provider;
import com.josealejandrorr.speedy.providers.ServiceProvider;
import com.josealejandrorr.speedy.utils.Logger;
import org.omg.CORBA.PRIVATE_MEMBER;

import javax.rmi.PortableRemoteObject;
import java.util.HashMap;

public class Providers {

    private static HashMap<String, Object> providers;

    private Providers()
    {

    }

    private static void register()
    {
        providers = new HashMap<String, Object>();

        providers.put(ServiceProvider.LOGGER, Logger.getLogger());

        providers.put("DB", DB.class);

        MySqlDriverDatabase serviceRepository = new MySqlDriverDatabase(
                Logger.getLogger()
        );
        providers.put(ServiceProvider.SERVICE_REPOSITORY, serviceRepository);
    }

    public static HashMap getList()
    {
        register();
        return providers;
    }

}