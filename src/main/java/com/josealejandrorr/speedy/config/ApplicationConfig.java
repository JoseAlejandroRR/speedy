package com.josealejandrorr.speedy.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ApplicationConfig {

    private  HashMap<String, String> data = new HashMap<>();

    public ApplicationConfig(String urlFile)
    {
        loadVars(urlFile);
    }

    private HashMap<String, String> loadVars(String file)
    {
        //HashMap<String, String> data = new HashMap<>();
        data.put("application.mode","debug");
        data.put("application.log","./application.log");

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            for(String line; (line = br.readLine()) != null; ) {
                String item[] = line.split("=",2);
                if(item.length>1){
                    data.put(item[0], item[1]);
                }
            }
            // line is not visible here.
        } catch (IOException e) {
            System.out.println("Error al Leer el Archivo de Configuracion");
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
        return data;
    }

    public HashMap<String, String> data()
    {
        return data;
    }

}
