package app.bootstrap;

import com.josealejandrorr.speedy.database.DB;

import java.util.HashMap;

public class Providers {

    private static HashMap<String, Object> providers;

    private Providers()
    {

    }

    private static void register()
    {
        providers = new HashMap<String, Object>();

        providers.put("DB", DB.class);
    }

    public static HashMap getList()
    {
        register();
        return providers;
    }

}