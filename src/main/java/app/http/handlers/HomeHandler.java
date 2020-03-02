package app.http.handlers;

import app.models.*;
import com.josealejandrorr.speedy.contracts.http.IRequestHandler;
import com.josealejandrorr.speedy.database.DB;
import com.josealejandrorr.speedy.database.Model;
import com.josealejandrorr.speedy.utils.Logger;
import com.josealejandrorr.speedy.web.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Collectors;

public class HomeHandler extends RequestHandler implements IRequestHandler {

    private String message;

    public HomeHandler()
    {
        //this.message = message;
    }


    public void action(Request req, Response res)
    {
        //DB db = (DB) this.container.getProvider("DB");
        try {
            //  System.out.println(db.getMessage());
            //String value = (String) Cache.getString("prueba");
            Sessions.destoy("id_usuario");
            //Cache.add("prueba","cualquier contenido que se me pueda ocurrir");
            res.send("Probando el cache: " + 1);
        } catch(Exception ex) {
            System.out.println("ERRROR "+ex.toString());
            res.send("INTERNAL SERVER", 500);
        }
    }

    public void demo(Request req, Response res)
    {
        //DB db = (DB) this.container.getProvider("DB");
       User user = new User();
      // user.last();

        Category catPhone = new Category();
        //catPhone.find(1);

        Device phone = new Device();
        phone.id = 24;
        phone.name = "Samsung A50";
        phone.serial = "8as7d6a8s756d";
        //phone.category = 1;

        //user.device = phone;

        Device dl = new Device();

        //dl.find(2);

        //user.device().category();

        /*user.devices().stream().forEach(d -> {
            d.category();
        });*/


        //user.devices.add(phone);
        //user.devices.add(d);
        //System.out.println(user.getClass().getSuperclass().getName().contains("speedy.database.Model"));
        //System.out.println(phone.getClass().getSuperclass().getName().contains("speedy.database.Model"));

            //  System.out.println(db.getMessage());
            //String value = (String) Cache.getString("prueba");
            //Cache.add("prueba","cualquier contenido que se me pueda ocurrir");
            //Logger.getLogger().debug("RECIBO ",req.query.get("user") );
            //Sessions.add("id_usuario",req.query.get("user"));
       res.json(dl.where("id",">","0").get());
        //dl.where("id",">","0").get();
        res.send("works");
        //res.json(user);

    }

    public void getUser(Request req, Response res)
    {

        //DB db = (DB) this.getContainer().getProvider("DB2");

        //ArrayList<Map<String, String>> datos = Cache.get("info");
        /*for(int i = 1; i <= 10000; i++) {
            User user = new User();
            user.setName("User "+i);
            user.lastname = "Demo "+i;
            user.document = "V-"+i;
            user.bio = "Soy00 el registro de prueba #"+i;
            user.state = "Buenos Aires";
            user.country = "Argentina";
            user.save();
        }*/

        String content = "<table width=\"800px\">";
        User user = new User();
        ArrayList<User> users = user.where("id",">","1").get();
        content = content + users.stream().map(u -> {
            return  "<tr>"+
                    "<tr>"+
                    "<td>"+u.id+"</td>"+
                    "<td>"+u.name+"</td>"+
                    "<td>"+u.lastname+"</td>"+
                    "<td>"+u.document+"</td>"+
                    "<td>"+u.bio+"</td>"+
                    "<td>"+u.state+"</td>"+
                    "</tr>";
        }).collect(Collectors.joining(""));

        serverDebug("SERVER", "dsasdsa");
        /*Iterator it = users.iterator();
        while(it.hasNext())
        {
            User u = (User) it.next();
           /*content += String.format("<tr>"+
                    "<tr>"+
                    "<td>%s</td>"+
                            "<td>%s</td>"+
                    "<td>%s</td>"+
                    "<td>%s</td>"+
                    "<td>%s</td>"+
                    "<td>%s</td>"+
                            "</tr>",
                    u.id, u.name, u.lastname, u.document, u.bio, u.state, u.state
            );
            content += "<tr>"+
                    "<tr>"+
                    "<td>"+u.id+"</td>"+
                    "<td>"+u.name+"</td>"+
                    "<td>"+u.lastname+"</td>"+
                    "<td>"+u.document+"</td>"+
                    "<td>"+u.bio+"</td>"+
                    "<td>"+u.state+"</td>"+
                    "</tr>";

            // Logger.getLogger().debug("User "+ u.id);
        }*/
        content += "</table>";

        //user.find(1);

        res.send(content);

    }

    public void upload(Request req, Response res)
    {
        /*Logger.getLogger().debug("TIENE "+req.body.size());
        for (Map.Entry<String, String> in : req.body.entrySet())
        {
            Logger.getLogger().debug("FIELD ",in.getKey(), in.getValue());
        }*/
        //String filePath = File.get("foto",null);

        /*ArrayList<Map<String, String>> datos = new ArrayList<Map<String, String>>();
        HashMap<String, String> obj = new HashMap<String, String>();
        obj.put("email",String.valueOf(req.body.get("email")));
        datos.add(obj);
        Cache.add("info", datos);*/
        res.send("Se ha creado un archivo para " + req.body.get("email") );
    }

    public void exit(Request req, Response res)
    {
        Cookies.destroy(Server.SESSION_SERVER_NAME);
        res.send("Works");
    }

    public void api(Request req, Response res)
    {
        HashMap<String, String> obj = new HashMap<String, String>();
        obj.put("id","1");
        obj.put("email","josealejandror28@gmail.com");

        res.json(obj);
    }

    public void views(Request req, Response res)
    {
        ArrayList<HashMap<String, String>> books = new ArrayList<>();

        HashMap<String, String> book = new HashMap<String, String>();

        book.put("name","La Guerra del fin del Mundo");
        book.put("author","Mario Vargas Llosa");
        book.put("editorial","Seix Barral");
        books.add(book);

        HashMap<String, String> book2 = new HashMap<String, String>();

        book2.put("name","Halcon");
        book2.put("author","Gary Gennings");
        book2.put("editorial","Planeta");
        books.add(book2);


        HashMap<String, Object> obj = new HashMap<String, Object>();
        obj.put("title","SejoFramework");
        obj.put("text","This is a simple text sample");
        obj.put("books", books);
        obj.put("current_year", 2018);

        res.json(books);

        //res.view("index",obj);
    }

    public Response q1(Request req, Response res)
    {
        return res.send("Works");
    }

    public void customer(Request req, Response res)
    {
       /*Customer customer = new Customer();

       customer.name = "Jose";
       customer.lastname = "Realza";
       customer.email = "skillcorptech@gmail.com";
       //customer.save();

       res.json(customer.where("id",">","0").get());*/
       //res.json(customer);

        Payment payment = new Payment();

        /*payment.findById(1);
        payment.id = 5;
        payment.name = "Smartphones";
        payment.description = "Categories for cellulars";
        payment.delete();*/
        //payment.save();

        /*res.json(
                payment
                        .where("id", "=", "1")
                        .orWhere("id", "=", "2")
                        //.where("name", "LIKE", "alg")
                       //.orWhere("name","LIKE","%alg%")
                        .take(10)
                        //.skip(5)
                        .get()
        );*/

        Product p1 = new Product();
        p1.name = "AMD Ryzen 9 3900";
        p1.description = "Procesador AMD de 3ra generacion";
        p1.price = 419.99;
        p1.status = 1;
        p1.stock = 100;
        p1.category_id = 1;
        p1.brand_id = 0;
        //p1.save();

        Product p2 = new Product();
        p2.name = "Intel i9 9900KS";
        p2.description = "Procesador Intel de 10ma generacion";
        p2.price = 480.50;
        p2.status = 1;
        p2.stock = 200;
        p2.category_id = 1;
        p2.brand_id = 0;
        //p2.findById(8);
        //p2.save();

        Category category = new Category();
        category.findById(2);
        category.products();
        category.product();

        res.json(category);
    }
}
