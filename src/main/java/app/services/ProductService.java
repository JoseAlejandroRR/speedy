package app.services;

import com.josealejandrorr.speedy.annotations.Service;

import java.util.HashMap;

@Service
public class ProductService {

    public String name = "Product Manager Class";

    public boolean addProduc(HashMap<String, String> data)
    {
        System.out.println("Aqui hago todo lo que quiero");
        return true;
    }
}
