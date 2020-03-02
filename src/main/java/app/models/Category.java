package app.models;

import com.josealejandrorr.speedy.annotations.ModelEntity;
import com.josealejandrorr.speedy.data.entities.Model;

import java.util.ArrayList;

@ModelEntity(table = "categories", timestamps = false)
public class Category extends Model {

    public Integer id;
    public String name;

    public ArrayList<Product> products;

    public Product product;

    public ArrayList<Product> products()
    {
        if (products == null)
        {
            products =  this.hasMany(Product.class, "category_id");
        }
        return products;
    }

    public Product product()
    {
        if (product == null)
        {
            product = (Product) this.hasOne(Product.class, "category_id");
        }
        return product;
    }

}
