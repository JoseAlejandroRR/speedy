package app.services;

import app.contracts.IProductService;
import com.josealejandrorr.speedy.annotations.Service;

import java.util.HashMap;

@Service
public class ProductService implements IProductService {

    public String name = "Product Manager Class";

    public boolean addProduc(HashMap<String, Object> data)
    {
        System.out.println("Aqui hago todo lo que quiero");
        return true;
    }
}
