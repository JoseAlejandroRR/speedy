package com.josealejandrorr.speedy;

import com.josealejandrorr.speedy.contracts.data.repositories.DatabaseRepository;
import com.josealejandrorr.speedy.contracts.http.IServer;
import com.josealejandrorr.speedy.contracts.providers.ILogger;
import com.josealejandrorr.speedy.data.drivers.MySqlDriverDatabase;
import com.josealejandrorr.speedy.database.Conexion;
import com.josealejandrorr.speedy.providers.Provider;
import com.josealejandrorr.speedy.providers.ServiceProvider;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application  {

    private static Application instance;

    private static ServiceProvider container;

    private static HashMap<String, Provider> providers;

    private static HashMap<String, String> dataEnv = new HashMap<>();

    public IServer server;

    public static int PORT = 9000;

    public ILogger logger;

    public static final String PATH_RESOURCES = "./src/main/resources";


    public Application(IServer server, ServiceProvider container, String fileConfiguration, ILogger logger)
    {
        providers = new HashMap<String, Provider>();

        this.container = container;

        this.server =  server;

        this.logger = logger;

        dataEnv = loadVars(fileConfiguration);

        container.setContextDefault();
    }


    public static ServiceProvider container()
    {
        return container;
    }

    private HashMap<String, String> loadVars(String file)
    {
        HashMap<String, String> data = new HashMap<>();

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            for(String line; (line = br.readLine()) != null; ) {
                String item[] = line.split("=",2);
                if(item.length>1){
                    data.put(item[0], item[1]);
                }
            }
            // line is not visible here.
        } catch (IOException e) {
            System.out.println("2Error al Leer el Archivo de Configuracion");
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
        return data;
    }

    public static String env(String key)
    {
        if (dataEnv.containsKey(key)) {
            return dataEnv.get(key);
        }
        return null;
    }


}
